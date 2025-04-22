package com.abdullahkahraman.exchange.parser;

import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ConversionFileParserFactoryImpl implements ConversionFileParserFactory{
    public static final String CSV_EXTENSION = ".csv";
    public static final String XLSX_EXTENSION = ".xlsx";
    public static final String XLS_EXTENSION = ".xls";

    @Override
    public ConversionFileParser getParser(MultipartFile file) {
        String filename = file.getOriginalFilename();

        if (ObjectUtils.isEmpty(filename)) throw new IllegalArgumentException("File name is missing");

        if (filename.endsWith(CSV_EXTENSION)) {
            return new CsvConversionFileParser();
        } else if (filename.endsWith(XLSX_EXTENSION) || filename.endsWith(XLS_EXTENSION)) {
            return new ExcelConversionFileParser();
        } else {
            throw new IllegalArgumentException("Unsupported file type: " + filename);
        }
    }
}
