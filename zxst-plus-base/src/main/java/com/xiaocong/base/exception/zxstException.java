package com.xiaocong.base.exception;

import lombok.Data;

@Data
public class zxstException extends RuntimeException {
    private String errMessage;

    public zxstException() {

    }

    public zxstException(String message) {
        super(message);
        this.errMessage = message;
    }

    public static void cast(String message) {
        throw new RuntimeException(message);
    }

    public static void cast(CommonError error) {
        throw new RuntimeException(error.getErrMessage());
    }

}
