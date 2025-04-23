package com.abdullahkahraman.exchange.parser;

import com.abdullahkahraman.exchange.dto.CurrencyConversionRequest;
import com.abdullahkahraman.exchange.enums.CurrencyCode;
import com.abdullahkahraman.exchange.exception.InvalidCsvFormatException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CsvConversionFileParser implements ConversionFileParser{
    /**
     * Parses a provided CSV file to extract currency conversion requests.
     * The file must comply with the expected CSV format, containing the headers:
     * "amount", "sourceCurrency", and "targetCurrency".
     *
     * @param file the CSV file to parse containing currency conversion data
     * @return a list of {@code CurrencyConversionRequest} objects built from the parsed CSV file
     * @throws InvalidCsvFormatException if the CSV file is empty, the format is incorrect,
     *                                   required headers are missing, or data is invalid
     */
    @Override
    public List<CurrencyConversionRequest> parse(MultipartFile file) {
        List<CurrencyConversionRequest> list = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            List<CSVRecord> records = csvParser.getRecords();
            if (records.isEmpty()) {
                throw new InvalidCsvFormatException("CSV file is empty");
            }

            for (CSVRecord record : records) {
                try {
                    if (!record.isMapped("amount") || !record.isMapped("sourceCurrency") || !record.isMapped("targetCurrency")) {
                        throw new InvalidCsvFormatException("CSV headers must include: amount, sourceCurrency, targetCurrency");
                    }

                    String amountStr = record.get("amount");
                    String sourceCurrency = record.get("sourceCurrency");
                    String targetCurrency = record.get("targetCurrency");

                    if (ObjectUtils.isEmpty(amountStr) || ObjectUtils.isEmpty(sourceCurrency) || ObjectUtils.isEmpty(targetCurrency)) {
                        throw new InvalidCsvFormatException("CSV row contains empty values: " + record.toString());
                    }

                    BigDecimal amount = new BigDecimal(amountStr);

                    list.add(new CurrencyConversionRequest(amount, sourceCurrency.toUpperCase(), targetCurrency.toUpperCase()));

                } catch (NumberFormatException e) {
                    throw new InvalidCsvFormatException("Invalid number format at row: " + record.toString());
                } catch (IllegalArgumentException e) {
                    throw new InvalidCsvFormatException("Invalid currency code at row: " + record.toString());
                }
            }

        } catch (IOException e) {
            throw new InvalidCsvFormatException("Error reading CSV file");
        }

        return list;
    }
}
