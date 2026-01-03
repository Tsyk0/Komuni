// src/main/java/hrc/komuni/exception/GlobalExceptionHandler.java
package hrc.komuni.exception;

import hrc.komuni.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ApiResponse<?> handleException(Exception e) {
        log.error("服务器内部错误", e);
        return ApiResponse.serverError("服务器内部错误: " + e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ApiResponse<?> handleRuntimeException(RuntimeException e) {
        log.warn("业务异常: {}", e.getMessage());
        return ApiResponse.badRequest(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("参数错误: {}", e.getMessage());
        return ApiResponse.badRequest("参数错误: " + e.getMessage());
    }
}