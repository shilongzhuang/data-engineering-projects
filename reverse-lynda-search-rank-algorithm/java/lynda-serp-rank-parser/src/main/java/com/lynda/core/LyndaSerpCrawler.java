package com.lynda.core;

import com.lynda.model.Course;
import com.lynda.model.CourseSearchResult;
import me.just4j.core.framework.crawler.Crawler;
import me.just4j.core.framework.crawler.Page;
import me.just4j.core.framework.parser.HtmlParser;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

public class LyndaSerpCrawler implements Crawler {

    private String keyword;


    private HtmlParser parser;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public HtmlParser getParser() {
        return parser;
    }

    public void setParser(HtmlParser parser) {
        this.parser = parser;
    }

    public LyndaSerpCrawler() {

    }

    public LyndaSerpCrawler(HtmlParser parser) {
        this.parser = parser;
    }

    public LyndaSerpCrawler(String keyword, HtmlParser parser) {
        this.keyword = keyword;
        this.parser = parser;
    }

    public LyndaSerpResponse begin() {
        LyndaSerpResponse response = new LyndaSerpResponse();

        //1. create seed url
        String seedUrl = LyndaPages.buildSearchResultPage(this.keyword, 1);
        response.setSeedUrl(seedUrl);

        Document seedDoc = parser.getDocument(seedUrl);

        Elements elements = seedDoc.select("div.results-heading");
        int numberOfResults = Integer.valueOf(elements.get(0).text().split(" ")[0]);
        int maxPageNumber = LyndaPages.getMaxPageNumber(numberOfResults);

        response.setMaxPageNumber(maxPageNumber);
        response.setNumberOfResults(numberOfResults);

        return response;
    }

    public void crawl(int pageNumber, List<CourseSearchResult> courseSearchResultList) {

        String serpUrl = LyndaPages.buildSearchResultPage(this.keyword, pageNumber);
        Document document = parser.getDocument(serpUrl);
        Elements elements = document.select("div.search-result.course");

        for (int i = 0; i < elements.size(); i++) {
            CourseSearchResult csr = new CourseSearchResult();
            csr.setKeyword(this.keyword);
            csr.setPageNumber(pageNumber);
            Element element = elements.get(i);
            String url = element.select("a").attr("href").split("[?]")[0];
            String cid = element.attr("data-course-id");

            int rank = LyndaPages.URLS_PER_SERP * (pageNumber - 1) + i + 1;
            csr.setCourseId(cid);
            csr.setRank(rank);
            csr.setUrl(url);
            courseSearchResultList.add(csr);
        }

    }


    private String buildFileName(String keyword, int page) {
        return new StringBuilder(keyword).append("_").append(String.valueOf(page)).append(".html").toString();
    }

    @Override
    public void crawl(String courseUrl, List courses) {
        String html = parser.getContent(courseUrl);
        Course course = (Course) parser.parse(html, Course.class);
        courses.add(course);
    }

    @Override
    public void crawl() {

    }

    @Override
    public void crawl(Page page) {

    }

}
