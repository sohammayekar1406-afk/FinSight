package com.ledgerlens.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDto {
    private OffsetDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private List<String> details;

    public ErrorResponseDto() {}

    public ErrorResponseDto(OffsetDateTime timestamp, int status, String error, String message, String path, List<String> details) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.details = details;
    }

    public static ErrorResponseDtoBuilder builder() { return new ErrorResponseDtoBuilder(); }

    public OffsetDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(OffsetDateTime timestamp) { this.timestamp = timestamp; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public List<String> getDetails() { return details; }
    public void setDetails(List<String> details) { this.details = details; }

    public static class ErrorResponseDtoBuilder {
        private OffsetDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private String path;
        private List<String> details;

        public ErrorResponseDtoBuilder timestamp(OffsetDateTime timestamp) { this.timestamp = timestamp; return this; }
        public ErrorResponseDtoBuilder status(int status) { this.status = status; return this; }
        public ErrorResponseDtoBuilder error(String error) { this.error = error; return this; }
        public ErrorResponseDtoBuilder message(String message) { this.message = message; return this; }
        public ErrorResponseDtoBuilder path(String path) { this.path = path; return this; }
        public ErrorResponseDtoBuilder details(List<String> details) { this.details = details; return this; }

        public ErrorResponseDto build() {
            return new ErrorResponseDto(timestamp, status, error, message, path, details);
        }
    }
}
