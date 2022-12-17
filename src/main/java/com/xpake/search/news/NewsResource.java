package com.xpake.search.news;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping(value = "news")
@CrossOrigin(origins = "*")
public class NewsResource {

    @Autowired
    private NewsApi newsApi;

    @GetMapping("")
    public List<NewsPojo> findAll() {
        return newsApi.getAll();
    }

    @GetMapping("search")
    public  List<NewsElasticPojo> searchInElastic(@RequestParam(required = false) String query) {
        return newsApi.searchInElastic(query);
    }

    @GetMapping("triggerCron")

    public void triggerCron() {
        newsApi.getDataFromApi();
    }

    @GetMapping("clearAll")
    public void clearAll() {
        newsApi.clearElasticAndRedisCache();
    }

    @GetMapping("article")
    public Optional<NewsPojo> getOne(@RequestParam(required = true) String id) {
        return newsApi.findOne(id);
    }

    @GetMapping("home")
    public List<NewsRedisPojo> getHomePage(@RequestParam(required = false) String page) {
        if (Objects.isNull(page)) {
            page = "0";
        }
        return newsApi.homePage(Integer.valueOf(page), 20);
    }
}
