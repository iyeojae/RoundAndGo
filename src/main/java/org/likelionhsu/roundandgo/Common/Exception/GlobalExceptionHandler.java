package org.likelionhsu.roundandgo.Common.Exception;

import org.likelionhsu.roundandgo.Common.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<CommonResponse<Object>> handleCustomException(CustomException ex) {
        return ResponseEntity.badRequest().body(
                CommonResponse.builder()
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .msg(ex.getMessage())
                        .data(null)
                        .build()
        );
    }
}