package com.abdullahkahraman.exchange.parser;

import org.springframework.web.multipart.MultipartFile;

public interface ConversionFileParserFactory {
    /**
     * Determines and returns a specific implementation of the {@link ConversionFileParser}
     * based on the file type of the provided {@link MultipartFile}.
     *
     * @param file the uploaded file to be parsed. The file must not be null
     *             and its name must have a supported extension (.csv, .xlsx, .xls).
     * @return an instance of {@link ConversionFileParser} suitable for parsing the provided file,
     *         such as {@link CsvConversionFileParser} for CSV files or {@link ExcelConversionFileParser}
     *         for Excel files (.xlsx or .xls).
     * @throws IllegalArgumentException if the file name is missing or the file type is unsupported.
     */
    ConversionFileParser getParser(MultipartFile file);
}
