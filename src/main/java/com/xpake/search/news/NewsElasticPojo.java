package com.xpake.search.news;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "news_articles")
@Getter
@Setter
@Builder
public class NewsElasticPojo {
    @Id
    public String id;

    @NonNull
    public String title;

    @NonNull
    public String content;

    @NonNull
    public String postgresId;
}
