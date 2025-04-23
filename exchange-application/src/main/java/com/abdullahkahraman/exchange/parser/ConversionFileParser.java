package com.abdullahkahraman.exchange.parser;

import com.abdullahkahraman.exchange.dto.CurrencyConversionRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ConversionFileParser {
    List<CurrencyConversionRequest> parse(MultipartFile file);
}
