package com.abdullahkahraman.exchange.parser;

import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ConversionFileParserFactoryImpl implements ConversionFileParserFactory{
    public static final String CSV_EXTENSION = ".csv";
    public static final String XLSX_EXTENSION = ".xlsx";
    public static final String XLS_EXTENSION = ".xls";

    /**
     * Returns a specific implementation of the {@link ConversionFileParser} based on the file type.
     * The method determines the parser to be used by evaluating the file extension
     * of the provided {@link MultipartFile}.
     *
     * @param file the uploaded file containing the data to be parsed. It must not be null
     *             and must have a valid file name with a supported extension (.csv, .xlsx, .xls).
     * @return an instance of {@link CsvConversionFileParser} if the file has a .csv extension,
     *         an instance of {@link ExcelConversionFileParser} if the file has a .xlsx or .xls extension.
     * @throws IllegalArgumentException if the file name is missing or if the file type is unsupported.
     */
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
