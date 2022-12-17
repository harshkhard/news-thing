package com.xpake.search.news;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsRedisRepo extends CrudRepository<NewsRedisPojo, String> {
    List<NewsRedisPojo> findAll(Pageable pageable);
}
