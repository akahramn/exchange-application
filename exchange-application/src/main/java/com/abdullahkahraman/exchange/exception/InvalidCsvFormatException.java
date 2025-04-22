package com.abdullahkahraman.exchange.exception;

import com.abdullahkahraman.exchange.enums.ErrorCode;

public class InvalidCsvFormatException extends BaseBusinessException{
    public InvalidCsvFormatException(String message) {
        super(message, ErrorCode.CSV_FORMAT_ERROR);
    }
}
