package com.lynda.core;

import java.util.List;

public class LyndaSerpResponse {

    private int numberOfResults;

    private int maxPageNumber;

    private List<String> urls;

    private String keyword;

    private String seedUrl;

    public int getNumberOfResults() {
        return numberOfResults;
    }

    public void setNumberOfResults(int numberOfResults) {
        this.numberOfResults = numberOfResults;
    }

    public int getMaxPageNumber() {
        return maxPageNumber;
    }

    public void setMaxPageNumber(int maxPageNumber) {
        this.maxPageNumber = maxPageNumber;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getSeedUrl() {
        return seedUrl;
    }

    public void setSeedUrl(String seedUrl) {
        this.seedUrl = seedUrl;
    }


    @Override
    public String toString() {
        return "LyndaSerpResponse{" +
                "numberOfResults=" + numberOfResults +
                ", maxPageNumber=" + maxPageNumber +
                ", urls=" + urls +
                ", keyword='" + keyword + '\'' +
                ", seedUrl='" + seedUrl + '\'' +
                '}';
    }
}
