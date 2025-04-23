package com.abdullahkahraman.exchange.parser;

import com.abdullahkahraman.exchange.dto.CurrencyConversionRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ConversionFileParser {
    /**
     * Parses the given file and extracts a list of currency conversion requests.
     * The file format and parsing behavior depend on the implementation of this method.
     *
     * @param file the input file containing currency conversion data; must not be null
     * @return a list of {@code CurrencyConversionRequest} objects parsed from the provided file
     * @throws IllegalArgumentException if the file is invalid, unsupported, or data within the file is improperly formatted
     */
    List<CurrencyConversionRequest> parse(MultipartFile file);
}
