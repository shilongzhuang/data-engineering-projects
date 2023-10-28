package com.lynda.model;

import javax.persistence.Column;
import javax.persistence.Table;

@Table(name = "lynda.course_serp")
public class CourseSearchResult {

    @Column(name = "keyword")
    private String keyword;

    @Column(name = "course_id")
    private String courseId;

    @Column(name = "url")
    private String url;

    @Column(name = "rank")
    private int rank;

    @Column(name = "page_number")
    private int pageNumber;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    @Override
    public String toString() {
        return "CourseSearchResult{" +
                "keyword='" + keyword + '\'' +
                ", courseId=" + courseId +
                ", url='" + url + '\'' +
                ", rank=" + rank +
                ", pageNumber=" + pageNumber +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CourseSearchResult that = (CourseSearchResult) o;

        return courseId != null ? courseId.equals(that.courseId) : that.courseId == null;
    }

    @Override
    public int hashCode() {
        return courseId != null ? courseId.hashCode() : 0;
    }
}