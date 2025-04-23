package com.abdullahkahraman.exchange.parser;

import com.abdullahkahraman.exchange.dto.CurrencyConversionRequest;
import com.abdullahkahraman.exchange.enums.CurrencyCode;
import com.abdullahkahraman.exchange.exception.InvalidExcelFormatException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExcelConversionFileParserTest {
    private ExcelConversionFileParser excelConversionFileParser;

    @BeforeEach
    void setUp() {
        excelConversionFileParser = new ExcelConversionFileParser();
    }

    private MockMultipartFile createExcelFile(Object[][] data) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        var sheet = workbook.createSheet("Sheet1");

        for (int i = 0; i < data.length; i++) {
            var row = sheet.createRow(i);
            for (int j = 0; j < data[i].length; j++) {
                Object val = data[i][j];
                var cell = row.createCell(j);
                if (val instanceof String s) {
                    cell.setCellValue(s);
                } else if (val instanceof Number n) {
                    cell.setCellValue(n.doubleValue());
                } else {
                    cell.setBlank();
                }
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return new MockMultipartFile(
                "file",
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                out.toByteArray()
        );
    }

    @Test
    void whenParseValidExcelFile_ShouldReturnCurrencyConversionRequests() throws Exception {
        MockMultipartFile file = createExcelFile(new Object[][] {
                {"amount", "sourceCurrency", "targetCurrency"},
                {100.5, "USD", "EUR"},
                {200, "GBP", "USD"}
        });

        List<CurrencyConversionRequest> result = excelConversionFileParser.parse(file);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(new BigDecimal("100.5"), result.get(0).getAmount());
        assertEquals(CurrencyCode.USD.toString(), result.get(0).getSourceCurrency());
        assertEquals(CurrencyCode.EUR.toString(), result.get(0).getTargetCurrency());
    }

    @Test
    void whenParseExcelFileWithMissingColumns_ShouldThrowInvalidExcelFormatException() throws Exception {
        MockMultipartFile file = createExcelFile(new Object[][]{
                {"amount", "sourceCurrency"},
                {100.5, "USD"}
        });

        assertThrows(InvalidExcelFormatException.class, () -> excelConversionFileParser.parse(file));
    }

    @Test
    void whenParseExcelFileWithNonNumericAmount_ShouldThrowInvalidExcelFormatException() throws Exception {
        MockMultipartFile file = createExcelFile(new Object[][]{
                {"amount", "sourceCurrency", "targetCurrency"},
                {"invalidNumber", "USD", "EUR"}
        });

        assertThrows(InvalidExcelFormatException.class, () -> excelConversionFileParser.parse(file));
    }

    @Test
    void whenParseExcelFileWithIOException_ShouldThrowInvalidExcelFormatException() throws Exception {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getInputStream()).thenThrow(new IOException("Test IOException"));

        assertThrows(InvalidExcelFormatException.class, () -> excelConversionFileParser.parse(mockFile));
    }

}