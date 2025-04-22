package com.abdullahkahraman.exchange.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import com.abdullahkahraman.exchange.model.Currency;


@Repository
public interface CurrencyRepository extends JpaRepository<Currency, String> ,
        JpaSpecificationExecutor<Currency> {
}
