package com.abdullahkahraman.exchange.parser;

import com.abdullahkahraman.exchange.dto.CurrencyConversionRequest;
import com.abdullahkahraman.exchange.enums.CurrencyCode;
import com.abdullahkahraman.exchange.exception.InvalidCsvFormatException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvConversionFileParserTest {
    private CsvConversionFileParser csvConversionFileParser;

    @BeforeEach
    void setUp() {
        csvConversionFileParser = new CsvConversionFileParser();
    }

    @Test
    public void whenParseValidCsvFile_ShouldReturnCurrencyConversionRequests() throws IOException {
        String csvData = "amount,sourceCurrency,targetCurrency\n100,USD,EUR";
        MockMultipartFile file = new MockMultipartFile(
                "file",                     // name
                "test.csv",                 // original filename
                "text/csv",                 // content type
                csvData.getBytes(StandardCharsets.UTF_8)  // content
        );

        List<CurrencyConversionRequest> result = csvConversionFileParser.parse(file);

        assertEquals(1, result.size());
        assertEquals(100, result.get(0).getAmount().intValue());
        assertEquals(CurrencyCode.USD, result.get(0).getSourceCurrency());
        assertEquals(CurrencyCode.EUR, result.get(0).getTargetCurrency());
    }

    @Test
    public void whenCsvIsEmpty_shouldThrowInvalidCsvFormatException() throws IOException {
        String csvData = "";
        MultipartFile file = Mockito.mock(MultipartFile.class);

        Mockito.when(file.getInputStream()).thenReturn(new ByteArrayInputStream(csvData.getBytes(StandardCharsets.UTF_8)));

        InvalidCsvFormatException exception = assertThrows(InvalidCsvFormatException.class, () -> csvConversionFileParser.parse(file));
        assertEquals("CSV file is empty", exception.getMessage());
    }

    @Test
    public void whenCsvHeadersAreMissing_shouldThrowInvalidCsvFormatException() throws IOException {
        String csvData = "100,USD,EUR";
        MultipartFile file = Mockito.mock(MultipartFile.class);

        Mockito.when(file.getInputStream()).thenReturn(new ByteArrayInputStream(csvData.getBytes(StandardCharsets.UTF_8)));

        InvalidCsvFormatException exception = assertThrows(InvalidCsvFormatException.class, () -> csvConversionFileParser.parse(file));
        assertEquals("CSV file is empty", exception.getMessage());
    }

    @Test
    public void whenRowHasMissingData_shouldThrowInvalidCsvFormatException() throws IOException {
        String csvData = "amount,sourceCurrency,targetCurrency\n100,USD,";
        MultipartFile file = Mockito.mock(MultipartFile.class);

        Mockito.when(file.getInputStream()).thenReturn(new ByteArrayInputStream(csvData.getBytes(StandardCharsets.UTF_8)));

        InvalidCsvFormatException exception = assertThrows(InvalidCsvFormatException.class, () -> csvConversionFileParser.parse(file));
        assertTrue(exception.getMessage().contains("CSV row contains empty values"));
    }

    @Test
    public void whenAmountIsNotNumeric_shouldThrowInvalidCsvFormatException() throws IOException {
        String csvData = "amount,sourceCurrency,targetCurrency\nabc,USD,EUR";
        MultipartFile file = Mockito.mock(MultipartFile.class);

        Mockito.when(file.getInputStream()).thenReturn(new ByteArrayInputStream(csvData.getBytes(StandardCharsets.UTF_8)));

        InvalidCsvFormatException exception = assertThrows(InvalidCsvFormatException.class, () -> csvConversionFileParser.parse(file));
        assertTrue(exception.getMessage().contains("Invalid number format"));
    }

}