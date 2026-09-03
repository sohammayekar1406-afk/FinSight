package com.ledgerlens.dto;

import java.util.List;

public class PagedResponseDto<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public PagedResponseDto() {}

    public PagedResponseDto(List<T> content, int page, int size, long totalElements, int totalPages) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public static <T> PagedResponseDtoBuilder<T> builder() {
        return new PagedResponseDtoBuilder<>();
    }

    public List<T> getContent() { return content; }
    public void setContent(List<T> content) { this.content = content; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public static class PagedResponseDtoBuilder<T> {
        private List<T> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;

        public PagedResponseDtoBuilder<T> content(List<T> content) { this.content = content; return this; }
        public PagedResponseDtoBuilder<T> page(int page) { this.page = page; return this; }
        public PagedResponseDtoBuilder<T> size(int size) { this.size = size; return this; }
        public PagedResponseDtoBuilder<T> totalElements(long totalElements) { this.totalElements = totalElements; return this; }
        public PagedResponseDtoBuilder<T> totalPages(int totalPages) { this.totalPages = totalPages; return this; }

        public PagedResponseDto<T> build() {
            return new PagedResponseDto<>(content, page, size, totalElements, totalPages);
        }
    }
}
