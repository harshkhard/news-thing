package com.xpake.search.news;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsPostgresRepo extends CrudRepository<NewsPojo, String> {

}
