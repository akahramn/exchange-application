package com.abdullahkahraman.exchange.exception;

import com.abdullahkahraman.exchange.enums.ErrorCode;

public class MissingSearchCriteriaException extends BaseBusinessException{
    public MissingSearchCriteriaException() {
        super("At least one of transactionId or transactionDate must be provided.", ErrorCode.MISSING_SEARCH_CRITERIA);
    }
}
