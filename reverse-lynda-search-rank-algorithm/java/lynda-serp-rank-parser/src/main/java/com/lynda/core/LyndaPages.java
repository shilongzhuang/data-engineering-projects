package com.lynda.core;

import me.just4j.core.framework.crawler.Page;

public class LyndaPages {

    public static final String HOME_PAGE = "https://www.lynda.com";

    protected static final int URLS_PER_SERP = 40;

    public static String buildSearchResultPage(String keyword, int pageNumber) {
        Page page = new Page(HOME_PAGE);
        page.path("search");
        page.parameter("q", keyword)
                .parameter("f", "producttypeid:2")
                .parameter("unx", "1")
                .parameter("page", String.valueOf(pageNumber));

        return page.toUrl();
    }


    protected static int getMaxPageNumber(int num) {
        return 1 + (num - 1) / URLS_PER_SERP;
    }
}
