package com.abdullahkahraman.exchange.parser;

import org.springframework.web.multipart.MultipartFile;

public interface ConversionFileParserFactory {
    ConversionFileParser getParser(MultipartFile file);
}
