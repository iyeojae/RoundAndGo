package org.likelionhsu.roundandgo.Common.Exception;

public enum ErrorCode {
    GOLF_COURSE_NOT_FOUND("GOLF_404", "골프장 정보를 찾을 수 없습니다.");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
}
