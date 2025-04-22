package com.abdullahkahraman.exchange.exception;

import com.abdullahkahraman.exchange.enums.ErrorCode;

public class TransactionNotFoundException extends BaseBusinessException{
    public TransactionNotFoundException(String transactionId) {
        super("Conversion not found for transaction ID: " + transactionId, ErrorCode.TRANSACTION_NOT_FOUND);
    }
}
