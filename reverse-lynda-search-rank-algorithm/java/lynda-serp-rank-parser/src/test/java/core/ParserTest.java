package core;

import me.just4j.core.framework.helper.FileIOUtil;
import me.just4j.core.framework.parser.HtmlParser;
import me.just4j.core.framework.parser.ParserStrategy;
import com.lynda.model.Course;
import com.lynda.model.CourseSearchResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParserTest {

    private String testSerpUrl = "https://www.lynda.com/search?q=python&f=producttypeid%3a2&unx=1&page=1";
    private String testCourseUrl = "https://www.lynda.com/Python-tutorials/Advanced-Python/699337-2.html";

    private HtmlParser parser;

    @Before
    public void init() {
        ParserStrategy strategy = new ParserStrategy();
        strategy.setMaxAttempts(5);
        strategy.setTimeout(30000);
        //strategy.setIgnoreContentType(true);
        strategy.setProxy(true);

        // set up a HTML parser
        parser = new HtmlParser(strategy);
    }

    @Test
    public void test() {
        String courseUrl = "https://www.lynda.com/iOS-tutorials/iOS-9-App-Extensions/423367-2.html";
        String html = parser.getContent(courseUrl);
        // Course course = (Course) parser.parse(html, Course.class);
        Document document = parser.getDocumentByHtml(html);
        Course course = new Course();

        Course course2 = (Course) parser.parse(html, Course.class);


        String courseId = document.select("div#video-container").attr("data-course-id");
        course.setCourseId(courseId);
        System.out.println(course.toString());

        System.out.println(course2.getCourseId());
        System.out.println(course2.toString());
    }

    @Test
    public void test02() {
        File file = new File("src/main/resources/data/serp/python_1.html");
        Document serpDoc = Jsoup.parse(FileIOUtil.loadFromFile(file));
        Elements elements = serpDoc.select("div.results-heading");
        System.out.println(Integer.valueOf(elements.get(0).text().split(" ")[0]));

    }


    @Test
    public void testParserSerp() {
        String dir = "src/main/resources/data/serp/";

        File[] files = new File(dir).listFiles();

        assert files != null;
        Arrays.stream(files).forEach(System.out::println);
    }

    @Test
    public void testParseSerp01() {

        String dir = "src/main/resources/data/serp/";
        File[] files = new File(dir).listFiles();
        assert files != null;


        List<CourseSearchResult> courseSearchResultList = new ArrayList<CourseSearchResult>();

        for (File file : files) {
            String fileName = file.getName();
            String[] info = fileName.replace(".html", "").split("_");
            String keyword = info[0];
            int pageNumber = Integer.valueOf(info[1]);

            Document document = parser.getDocumentByHtml(FileIOUtil.loadFromFile(file));
            Elements elements = document.select("div.search-result.courses");

            System.out.println(elements.size());

            for (int i = 0; i < elements.size(); i++) {
                CourseSearchResult csr = new CourseSearchResult();
                csr.setKeyword(keyword);
                csr.setPageNumber(pageNumber);
                Element element = elements.get(i);
                String url = element.select("a").attr("href").split("[?]")[0];

                String cid = element.attr("data-courses-id");

                int rank = 40 * (pageNumber - 1) + i + 1;

                csr.setCourseId(cid);
                csr.setRank(rank);
                csr.setUrl(url);

                System.out.println(csr.toString());

                courseSearchResultList.add(csr);
            }
        }


        System.out.println(courseSearchResultList.size());

    }


    @Test
    public void testCourse02() {
        String courseDir = "src/main/resources/data/courses/";

        File[] files = new File(courseDir).listFiles();

        List<Course> courses = new ArrayList<>();


        assert files != null;
        for (File file : files) {

            String html = FileIOUtil.loadFromFile(file);
            Course course = (Course) parser.parse(html, Course.class);

            System.out.println(course.toString());
            courses.add(course);
        }

        System.out.println(courses.size());


    }

    @Test
    public void testCourse() {

        String courseDir = "src/main/resources/data/courses/";

        File[] files = new File(courseDir).listFiles();

        List<Course> courses = new ArrayList<>();

        for (File file : files) {

            Course course = new Course();
            String name = file.getName();
            System.out.println(name);
            Document document = parser.getDocumentByHtml(FileIOUtil.loadFromFile(file));

            String courseId = document.select("div#video-container").attr("data-course-id");
            course.setCourseId(courseId);

            String title = document.select("title").text();
            course.setTitle(title);

            String description = document.select("meta[name=description]").attr("content");
            course.setDescription(description);

            String author = document.select("div.author-thumb > a > cite").text();
            course.setAuthor(author);

            String authorProfile = document.select("div.author-thumb > a").attr("href");
            course.setAuthorProfile(authorProfile);

            String releaseDate = document.select("span#release-date").text();
            course.setReleaseDate(releaseDate);

            String content = document.select("div[itemprop=description]").text();
            course.setContent(content);

            String skillLevel = document.select("h6 > strong").text();
            course.setSkillLevel(skillLevel);

            String duration = document.select("div.duration > span").text();
            course.setDuration(duration);

            String views = document.select("span#course-viewers").text();
            course.setViews(views);

            String category = document.select("a[data-ga-value=0]").text();
            course.setCategory(category);

            String subCategory = document.select("a[data-ga-value=1]").text();
            course.setSubCategory(subCategory);

            System.out.println(document.select("span.file-size").size());
            //course.setExerciseFileSize(sizeFile);


            System.out.println(course.toString());
        }

    }

}
