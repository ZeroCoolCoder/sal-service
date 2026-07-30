package com.sal.dto;

import java.util.List;

/**
 * Response DTO for search results.
 */
public class SearchResponse {

    private List<ObjectInfoResponse> results;
    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;
    private Boolean hasNext;
    private Boolean hasPrevious;

    public static SearchResponse of(List<ObjectInfoResponse> results, Integer page, 
                                    Integer size, Long totalElements) {
        SearchResponse response = new SearchResponse();
        response.results = results;
        response.page = page;
        response.size = size;
        response.totalElements = totalElements;
        response.totalPages = (int) Math.ceil((double) totalElements / size);
        response.hasNext = (page + 1) < response.totalPages;
        response.hasPrevious = page > 0;
        return response;
    }

    // Getters and Setters
    public List<ObjectInfoResponse> getResults() {
        return results;
    }

    public void setResults(List<ObjectInfoResponse> results) {
        this.results = results;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Boolean getHasNext() {
        return hasNext;
    }

    public void setHasNext(Boolean hasNext) {
        this.hasNext = hasNext;
    }

    public Boolean getHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(Boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }
}
