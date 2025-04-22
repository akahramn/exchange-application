package com.abdullahkahraman.exchange.exception;

import com.abdullahkahraman.exchange.enums.ErrorCode;
import lombok.Data;

@Data
public class BaseBusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    public BaseBusinessException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
