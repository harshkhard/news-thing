package com.xpake.search.news;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface NewsElasticRepo extends ElasticsearchRepository<NewsElasticPojo, String> {
    List<NewsElasticPojo> findByContentLike (String query);
}
