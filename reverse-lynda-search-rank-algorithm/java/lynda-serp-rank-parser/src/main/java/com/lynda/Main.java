package com.lynda;

import com.lynda.model.Course;
import me.just4j.core.framework.Just4j;
import me.just4j.core.framework.db.DB;
import me.just4j.core.framework.db.DBConnectionBuilder;
import me.just4j.core.framework.db.DBSession;
import me.just4j.core.framework.helper.ConfigLoader;
import me.just4j.core.framework.parser.HtmlParser;
import me.just4j.core.framework.parser.ParserStrategy;
import com.lynda.core.LyndaSerpCrawler;
import com.lynda.core.LyndaSerpResponse;
import com.lynda.model.CourseSearchResult;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Main {

    private static LyndaSerpCrawler crawler;

    private static void executeSerp() {
        long start = System.currentTimeMillis();


        LyndaSerpResponse lyndaSerpResponse = crawler.begin();

        DBSession session = DBConnectionBuilder.create()
                .database(DB.POSTGRES)
                .connectionUrl(ConfigLoader.getValue("ps.url"))
                .userName(ConfigLoader.getValue("ps.username"))
                .password(ConfigLoader.getValue("ps.password"))
                .autoCommit(false)
                .build();


        List<CourseSearchResult> courseSearchResults = new ArrayList<>();

        for (int i = 1; i <= lyndaSerpResponse.getMaxPageNumber(); i++) {
            crawler.crawl(i, courseSearchResults);
        }

        courseSearchResults = courseSearchResults.stream().distinct().collect(Collectors.toList());

        try {
            session.truncate(CourseSearchResult.class);
            session.insert(courseSearchResults);
            session.commit();
            session.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        long end = System.currentTimeMillis();
        System.out.println("It took " + (end - start) / 1000L + " seconds for whole process.");

    }

    public static void executeCourse() {
        long start = System.currentTimeMillis();

        List<String> urls = Just4j.read("src/main/resources/data/courses.txt");
        System.out.println(urls.size());

        List<List> urlLists = Just4j.splitListByStep(urls, 100);

        for (int i = 0; i < urlLists.size(); i++) {
            long startLoop = System.currentTimeMillis();

            List<Course> courseList = new ArrayList<>();

            ExecutorService executorService = Executors.newFixedThreadPool(20);

            DBSession session = DBConnectionBuilder.create()
                    .database(DB.POSTGRES)
                    .connectionUrl(ConfigLoader.getValue("ps.url"))
                    .userName(ConfigLoader.getValue("ps.username"))
                    .password(ConfigLoader.getValue("ps.password"))
                    .autoCommit(false)
                    .build();


            if (i == 0) {
                try {
                    session.truncate(Course.class);
                    System.out.println("Clearing the table!");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if ((i + 1) % 10 == 0) {
                try {
                    TimeUnit.SECONDS.sleep(4);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            for (Object url : urlLists.get(i)) {
                String urlString = url.toString();
                executorService.submit(() -> crawler.crawl(urlString, courseList));
            }


            try {
                executorService.shutdown();
                executorService.awaitTermination(1, TimeUnit.HOURS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                session.insert(courseList);
                session.commit();
                session.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            long endLoop = System.currentTimeMillis();
            System.out.println("It took " + (endLoop - startLoop) / 1000L + " seconds for this loop " + i + '.');
        }

        long end = System.currentTimeMillis();
        System.out.println("It took " + (end - start) / 1000L + " seconds for whole process.");
    }


    public static void main(String[] args) {

        ParserStrategy strategy = new ParserStrategy();
        strategy.setMaxAttempts(5);
        strategy.setTimeout(30000);
        //strategy.setIgnoreContentType(true);
        strategy.setProxy(true);

        // set up a HTML parser
        HtmlParser parser = new HtmlParser(strategy);

        String keyword = "python";
        crawler = new LyndaSerpCrawler(keyword, parser);

       // executeSerp();

        executeCourse();

    }


}
