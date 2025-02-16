package com.example.carpool.data.models;

import java.util.List;

public class PageResponse<T> {
    private int page;
    private int size;
    private int totalPages;
    private long totalElements;
    private List<T> content;

    public int getPage() { return page; }
    public int getSize() { return size; }
    public int getTotalPages() { return totalPages; }
    public long getTotalElements() { return totalElements; }
    public List<T> getContent() { return content; }
}
