package com.abdullahkahraman.exchange.specification;

import com.abdullahkahraman.exchange.model.Transaction;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ConversionTransactionSpecification {
    public static final String TRANSACTION_ID = "transactionId";
    public static final String TRANSACTION_DATE = "transactionDate";

    /**
     * Builds a specification to filter Currency entities based on transaction ID and transaction date.
     *
     * @param transactionId the unique identifier of the transaction to filter by
     * @param date the date of the transaction to filter by; filters entities with a transaction date
     *             within the start and end of the provided date
     * @return a Specification for filtering Currency entities based on the provided parameters
     */
    public static Specification<Transaction> filterBy(String transactionId, LocalDate date) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasLength(transactionId)) {
                predicates.add(cb.equal(root.get(TRANSACTION_ID), transactionId));
            }

            if (ObjectUtils.isEmpty(date)) {
                predicates.add(cb.between(
                        root.get(TRANSACTION_DATE),
                        date.atStartOfDay(),
                        date.plusDays(1).atStartOfDay()
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
