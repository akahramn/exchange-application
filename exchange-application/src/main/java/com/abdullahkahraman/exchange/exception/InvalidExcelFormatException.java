package com.abdullahkahraman.exchange.exception;

import com.abdullahkahraman.exchange.enums.ErrorCode;

public class InvalidExcelFormatException extends BaseBusinessException{
    public InvalidExcelFormatException(String message) {
        super(message, ErrorCode.EXCEL_FORMAT_ERROR);
    }

    public InvalidExcelFormatException(String message, Throwable cause) {
        super(message, ErrorCode.EXCEL_FORMAT_ERROR);
        initCause(cause);
    }
}
