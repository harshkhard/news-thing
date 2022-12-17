package com.xpake.search.news;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class NewsNetworkPojo {
    private String status;
    private long totalResults;
    private NewsPojo[] results;
    private long nextPage;
}
