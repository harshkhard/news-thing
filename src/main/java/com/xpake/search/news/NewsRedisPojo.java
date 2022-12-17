package com.xpake.search.news;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.time.Instant;

@RedisHash("news_article")
@Getter
@Setter
@ToString
@Builder
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class NewsRedisPojo implements Serializable {
    @org.springframework.data.annotation.Id
    private String id;
    private String title;

    private String imageUrl;

    private Instant createdDate;

    private String postgresId;
}
