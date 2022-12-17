package com.xpake.search.news;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.erhlc.NativeSearchQuery;
import org.springframework.data.elasticsearch.client.erhlc.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Lazy(value = false)
@Service
@NoArgsConstructor
@Slf4j
public class NewsApi {

    final String uri = "https://newsdata.io/api/1/news?apikey=API_KEY&country=in,us,gb&language=en&page={page}";

    @Autowired
    private NewsPostgresRepo newsPostgresRepo;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private NewsElasticRepo newsElasticRepo;

    @Autowired
    private NewsRedisRepo newsRedisRepo;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Scheduled(cron = "0 11 * * * *")
    public void getDataFromApi() {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(0));
        List<NewsPojo> newsPojos = new ArrayList<>();
        for (int i = 1; i < 50; i++) {
            NewsNetworkPojo res = restTemplate.getForObject(uri, NewsNetworkPojo.class, params);
            params.replace("page", String.valueOf(i));
            if (Objects.nonNull(res) && Objects.nonNull(res.getResults())) {
                newsPojos.addAll(Arrays.stream(res.getResults()).filter(newsPojo -> Objects.nonNull(newsPojo.getContent()) && Objects.nonNull(newsPojo.getDescription())).collect(Collectors.toList()));
            }
        }
        newsPostgresRepo.saveAll(newsPojos);
        newsPojos.forEach(newsPojo -> {
            applicationEventPublisher.publishEvent(newsPojo);
        });
        log.info("Event Published");
    }

    public List<NewsPojo> getAll() {
        return StreamSupport.stream(newsPostgresRepo.findAll().spliterator(), false).collect(Collectors.toList());
    }

    @EventListener
    @Async
    public void saveToElastic(NewsPojo newsPojo) {
        log.info("Event Received");

        NewsElasticPojo newsElasticPojo = NewsElasticPojo.builder()
                .title(newsPojo.getTitle())
                .content(newsPojo.getContent())
                .postgresId(newsPojo.getId())
                .build();
        newsElasticRepo.save(newsElasticPojo);
        log.info("Saved to elastic");
    }

    public List<NewsElasticPojo> searchInElastic(String query) {
        Criteria criteria = new Criteria("content").expression("*" + query + "*");
        Query query1 = new CriteriaQuery(criteria);
        SearchHits<NewsElasticPojo> newsElasticPojoSearchHits = elasticsearchTemplate.search(query1, NewsElasticPojo.class);
        List<NewsElasticPojo> newsElasticPojos = new ArrayList<>();
        for (SearchHit<NewsElasticPojo> searchHit : newsElasticPojoSearchHits.getSearchHits()) {
            newsElasticPojos.add(searchHit.getContent());
        }
        return newsElasticPojos;
    }

    @Async
    @EventListener
    public void saveToRedis(NewsPojo newsPojo) {
        log.info("Redis Event Received");
        newsRedisRepo.save(NewsRedisPojo.builder()
                .title(newsPojo.getTitle())
                .postgresId(newsPojo.getId())
                .imageUrl(newsPojo.getImageUrl())
                .createdDate(newsPojo.getCreatedDate())
                .build());
        log.info("Saved to Redis");
    }

    public void clearElasticAndRedisCache() {
        newsRedisRepo.deleteAll();
        newsElasticRepo.deleteAll();
        newsPostgresRepo.deleteAll();
    }

    public Optional<NewsPojo> findOne(String id) {
        return newsPostgresRepo.findById(id);
    }

    public List<NewsRedisPojo> homePage(int pageNumber, int pageSize) {
        Pageable currentPage = PageRequest.of(pageNumber, pageSize, Sort.by("createdDate").descending());
        return newsRedisRepo.findAll(currentPage);
    }
}
