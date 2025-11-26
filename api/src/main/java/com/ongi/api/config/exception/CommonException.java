package com.ongi.api.config.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CommonException extends RuntimeException {

    public HttpStatus status;

    public String code;

    public String message;

    public Object data;

    public CommonException() {
        super();
        this.status = ExceptionCode.INTERNAL_SERVER_ERROR.getStatus();
        this.code = ExceptionCode.INTERNAL_SERVER_ERROR.getCode();
        this.message = ExceptionCode.INTERNAL_SERVER_ERROR.getMessage();
        this.data = null;
    }

    public CommonException(ExceptionCode exceptionCode){
        super();
        this.status = exceptionCode.getStatus();
        this.code = exceptionCode.getCode();
        this.message = exceptionCode.getMessage();
        this.data = null;
    }

    public CommonException(ExceptionCode exceptionCode, Object data){
        super();
        this.status = exceptionCode.getStatus();
        this.code = exceptionCode.getCode();
        this.message = exceptionCode.getMessage();
        this.data = data;
    }

    public CommonException(HttpStatus status, String message) {
        super();
        this.status = status;
        this.code = String.valueOf(status.value());
        this.message = message;
        this.data = null;
    }

    public CommonException(String code, String message) {
        super();
        this.status = HttpStatus.OK;
        this.code = code;
        this.message = message;
        this.data = null;
    }
}
