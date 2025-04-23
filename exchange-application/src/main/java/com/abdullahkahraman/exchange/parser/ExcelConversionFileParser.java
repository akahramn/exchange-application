package com.abdullahkahraman.exchange.parser;

import com.abdullahkahraman.exchange.dto.CurrencyConversionRequest;
import com.abdullahkahraman.exchange.enums.CurrencyCode;
import com.abdullahkahraman.exchange.exception.InvalidExcelFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ExcelConversionFileParser implements ConversionFileParser {
    /**
     * Parses a provided Excel file to extract currency conversion requests.
     * The file must comply with the expected Excel format, containing rows with
     * three columns: "amount", "sourceCurrency", and "targetCurrency".
     *
     * @param file the Excel file to parse containing currency conversion data
     * @return a list of {@code CurrencyConversionRequest} objects built from the parsed Excel file
     * @throws InvalidExcelFormatException if the Excel file is invalid, missing data, or contains
     *                                     formatting errors
     */
    @Override
    public List<CurrencyConversionRequest> parse(MultipartFile file) {
        List<CurrencyConversionRequest> list = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            boolean skipHeader = true;
            for (Row row : sheet) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }

                try {
                    if (row.getPhysicalNumberOfCells() < 3) {
                        throw new InvalidExcelFormatException("Missing required columns at row index: " + row.getRowNum());
                    }

                    Cell amountCell = row.getCell(0);
                    Cell sourceCell = row.getCell(1);
                    Cell targetCell = row.getCell(2);

                    if (amountCell == null || sourceCell == null || targetCell == null) {
                        throw new InvalidExcelFormatException("Empty cell detected at row: " + row.getRowNum());
                    }

                    double amountVal = amountCell.getNumericCellValue();
                    String source = sourceCell.getStringCellValue();
                    String target = targetCell.getStringCellValue();

                    BigDecimal amount = BigDecimal.valueOf(amountVal);

                    list.add(new CurrencyConversionRequest(amount, source.toUpperCase(), target.toUpperCase()));

                } catch (IllegalArgumentException | IllegalStateException e) {
                    throw new InvalidExcelFormatException("Invalid data at row: " + row.getRowNum(), e);
                }
            }
        } catch (IOException e) {
            throw new InvalidExcelFormatException("Failed to read Excel file", e);
        }

        return list;
    }
}
