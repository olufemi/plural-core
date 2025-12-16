package com.finacial.wealth.backoffice.reports;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.Writer;
import java.util.List;

public class CsvWriter {

  public static void write(Writer out, List<String> headers, List<List<Object>> rows) throws Exception {
    try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(headers.toArray(String[]::new)))) {
      for (List<Object> row : rows) {
        printer.printRecord(row);
      }
    }
  }
}
