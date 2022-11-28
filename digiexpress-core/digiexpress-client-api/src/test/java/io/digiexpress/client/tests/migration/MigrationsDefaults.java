package io.digiexpress.client.tests.migration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.Data;
import lombok.RequiredArgsConstructor;

public class MigrationsDefaults {
  public static final ObjectMapper om = new ObjectMapper().registerModules(new JavaTimeModule(), new Jdk8Module(), new GuavaModule());
  public static final String folder = "src/test/resources/migration/"; 
  
  public static Summary summary(String... cols) {    
    return new Summary(cols);
  }
  
  
  public static class Summary {
    private List<String> headers;
    private List<Integer> length;
    private final List<Row> rows = new ArrayList<>();
  
    public Summary(String ...cols) {
      final List<Integer> lengths = new ArrayList<>();
      for(final var col : cols) {
        final var colLength =  col.length() < 17 ? 17 : col.length();
        lengths.add(colLength);
      } 
      this.length = lengths;
      this.headers = new ArrayList<>(Arrays.asList(cols));
    }
    
    @RequiredArgsConstructor
    @Data
    private static class Row {
      private final Serializable[] values;
    }
    
    public Summary addRow(Serializable ... values) {
      int index = 0;
      for(final var value : values) {
        final var old = this.length.get(index);
        if(value != null && old < value.toString().trim().length()) {
          this.length.set(index, value.toString().trim().length());
        }
        index++;
      }
      this.rows.add(new Row(values));
      return this;
    }
        
    public String toString() {
      StringBuilder result = new StringBuilder();
      int index = 0;
      if(result.isEmpty()) {
        result.append("| ");
        final var headSep = new StringBuilder("|");
        for(final var header : headers) {
          final var pos = length.get(index);
          result.append(toPosString(header, pos)).append(" | ");
          index++;
          
          for(var sep = 0; sep <= pos+1; sep++) {
            headSep.append("-");
          }
          headSep.append("|");
        }
        
        result.append(System.lineSeparator())
          .append(headSep.toString());
      }
      
      
      
      for(final var row : this.rows) {
        index = 0;
        result.append(System.lineSeparator()).append("| ");;
        final var values = row.values;
        for(final var header : headers) {
          final var src = values.length > index ? values[index] : null;
          final var length = this.length.get(index);
          
          result.append(toPosString(src, length)).append(" | ");
          index++;
        }

      }
      result.append(System.lineSeparator());
      
      return System.lineSeparator() + "### Summary: " + System.lineSeparator() + 
          result
          .append("### Total: ").append(rows.size())
          .append(System.lineSeparator())
          .append(System.lineSeparator());
    }
  }
  
  private static String toPosString(Serializable src, int length) {
    String value = src == null ? null : src.toString().trim();
    if(value == null || value.isEmpty()) {
      value = "null";
    }
    final var pattern = "%1$-" + length + "s";
    try {
      return String.format(pattern, value);
    } catch(Exception e) {
      throw new RuntimeException(pattern + " / '" + value + "' -> " + e.getMessage(), e);
    } 
  }
  
}
