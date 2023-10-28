package com.lynda.model;


import com.lynda.core.LyndaPages;
import me.just4j.core.framework.parser.jsoup.JsoupSelector;

import javax.persistence.Column;
import javax.persistence.Table;

@Table(name = "lynda.course_info")
public class Course {

    @Column(name = "course_id")
    @JsoupSelector(value = "div#video-container", attribute = "data-course-id")
    private String courseId;

    @Column(name = "title")
    @JsoupSelector(value = "title")
    private String title;

    @Column(name = "description")
    @JsoupSelector(value = "meta[name=description]", attribute = "content")
    private String description;

    @Column(name = "skill_level")
    @JsoupSelector(value = "h6 > strong")
    private String skillLevel;

    @Column(name = "duration")
    @JsoupSelector(value = "div.duration > span")
    private String duration;

    @Column(name = "views")
    @JsoupSelector(value = "span#course-viewers")
    private String views;

    @Column(name = "release_date")
    @JsoupSelector(value = "span#release-date")
    private String releaseDate;

    @Column(name = "content")
    @JsoupSelector(value = "div[itemprop=description]")
    private String content;

    @Column(name = "author")
    @JsoupSelector(value = "div.author-thumb > a > cite")
    private String author;

    @Column(name = "author_profile")
    @JsoupSelector(value = "div.author-thumb > a", attribute = "href")
    private String authorProfile;

    @Column(name = "exercise_file_size")
    private String exerciseFileSize;

    @Column(name = "category")
    @JsoupSelector(value = "a[data-ga-value=0]")
    private String category;

    @Column(name = "sub_category")
    @JsoupSelector(value = "a[data-ga-value=1]")
    private String subCategory;

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSkillLevel() {
        return skillLevel;
    }

    public void setSkillLevel(String skillLevel) {
        this.skillLevel = skillLevel;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getViews() {
        return views;
    }

    public void setViews(String views) {
        this.views = views;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthorProfile() {
        return LyndaPages.HOME_PAGE + authorProfile;
    }

    public void setAuthorProfile(String authorProfile) {
        this.authorProfile = authorProfile;
    }

    public String getExerciseFileSize() {
        return exerciseFileSize;
    }

    public void setExerciseFileSize(String exerciseFileSize) {
        this.exerciseFileSize = exerciseFileSize;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }


    @Override
    public String toString() {
        return "Course{" +
                "courseId=" + courseId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", skillLevel='" + skillLevel + '\'' +
                ", duration='" + duration + '\'' +
                ", views=" + views +
                ", releaseDate='" + releaseDate + '\'' +
                ", content='" + content + '\'' +
                ", author='" + author + '\'' +
                ", authorProfile='" + authorProfile + '\'' +
                ", exerciseFileSize='" + exerciseFileSize + '\'' +
                ", category='" + category + '\'' +
                ", subCategory='" + subCategory + '\'' +
                '}';
    }
}
