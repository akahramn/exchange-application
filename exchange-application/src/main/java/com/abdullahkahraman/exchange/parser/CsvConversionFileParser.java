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
                    CurrencyCode source = CurrencyCode.valueOf(sourceCurrency.toUpperCase());
                    CurrencyCode target = CurrencyCode.valueOf(targetCurrency.toUpperCase());

                    list.add(new CurrencyConversionRequest(amount, source, target));

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
