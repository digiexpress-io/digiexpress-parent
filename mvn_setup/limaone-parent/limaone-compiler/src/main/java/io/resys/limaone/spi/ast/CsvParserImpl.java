package io.resys.limaone.spi.ast;

/*-
 * #%L
 * limaone-compiler
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import io.resys.limaone.ast.AST_Parser.CsvParser;
import io.resys.limaone.ast.CSV_AST;
import io.resys.limaone.ast.CSV_AST.CSVRow;
import io.resys.limaone.ast.ImmutableCSVCell;
import io.resys.limaone.ast.ImmutableCSVRow;
import io.resys.limaone.model.ImmutableParameter;
import io.resys.limaone.model.Parameter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CsvParserImpl implements CsvParser {
  private List<Parameter> parameter;
  private String syntax;
  
  @Override
  public CsvParser csv(String syntax) {
    this.syntax = Objects.requireNonNull(syntax, () -> "syntax can't be null");
    return this;
  }

  @Override
  public CsvParser castTo(List<Parameter> parameter) {
    this.parameter = Objects.requireNonNull(parameter, () -> "parameter can't be null");
    return this;
  }

  @Override
  public CSV_AST parse() {
    Objects.requireNonNull(syntax, () -> "syntax can't be null");
    Objects.requireNonNull(parameter, () -> "parameter can't be null");
    
    try {
      final CSVParser parser = CSVParser.parse(syntax, CSVFormat.DEFAULT.builder().setDelimiter(';').setIgnoreEmptyLines(true).get());
      final List<CSVRecord> records = parser.getRecords();
      if (records.isEmpty()) {
        return new CSV_AST_IMPL(parameter, Collections.emptyList());
      }
      
      final Iterator<CSVRecord> iterator = records.iterator();
      final Map<Integer, Optional<Parameter>> headers = new HashMap<>();
      
      int headerIndex = 0;
      for (final var headerName : iterator.next()) {
        
        final var nextIndex = headerIndex;
        final var param = this.parameter.stream()
          .filter(p -> 
            p.getId().equals(headerName) || 
            p.getName().equals(headerName) || 
            headerName.equals(p.getRef()) ||
            headerName.equals(p.getExtRef())
          )
          .map(found -> {
            final Parameter mapped = ImmutableParameter.builder().from(found).order(nextIndex).build();
            return mapped;
          })
          .findAny();
        
        headers.put(nextIndex, param);
        headerIndex++;
      }
      
      
      
      final List<CSVRow> results = new ArrayList<>();
      while (iterator.hasNext()) {
        final CSVRecord row = iterator.next();
        final int transactionId = (int) row.getRecordNumber() - 1;
        results.add(ImmutableCSVRow.builder()
            .rowIndex(transactionId)
            .cells(visitProgramInput(row, headers))
            .build());
      }

      
      return new CSV_AST_IMPL(
          headers.values().stream().filter(e -> e.isPresent()).map(e -> e.get()).toList(), 
          Collections.unmodifiableList(results)
      );
    } catch(Exception e) {
      throw new CsvParserException(e.getMessage(), e);
    }
  }
  
  
  private List<CSV_AST.CSVCell> visitProgramInput(CSVRecord row, Map<Integer, Optional<Parameter>> headers) {
    final List<CSV_AST.CSVCell> inputEntity = new ArrayList<>();
    
    int columnIndex = 0;
    for (final var columnValue : row) {
      final var param = headers.get(columnIndex++).orElse(null);
      if(param == null) {
        continue;
      }
      
      final var casted = param.getDeserializer().deserialize(param, columnValue);
      inputEntity.add(ImmutableCSVCell.builder()
          .parameterName(param.getName())
          .parameterValue(casted)
          .build());
      
    }
    return inputEntity;
  }

  @Getter
  @RequiredArgsConstructor
  public static class CSV_AST_IMPL implements CSV_AST {
    private final List<Parameter> headers;
    private final List<CSVRow> rows;

    @Override
    public String forEach(Function<Map<String, Serializable>, List<Map<String, Serializable>>> mapper) {
      final Set<String> usedFields = new HashSet<>();
      final List<Map<String, Serializable>> results = new ArrayList<>();
      
      
      for (final var row : rows) {
        long transactionId = row.getRowIndex() - 1;
        try {
          final var input = row.getCells().stream().collect(Collectors.toMap(
              e -> e.getParameterName(), 
              e -> e.getParameterValue()));
          
          final var mapped = mapper.apply(input);
          
          if(!mapped.isEmpty()) {
            usedFields.addAll(mapped.getFirst().keySet());
          }
          
          for(final var mappedRow : mapped) {
            final var extended = new HashMap<>(mappedRow);
            extended.put("_id", transactionId);
            results.add(extended);
          }
        } catch (Exception e) {
          final Map<String, Serializable> failSafe = Map.of(
            "_id", transactionId,
            "_errors", e.getMessage()
          );
          results.add(failSafe);
        }
      }

      try {
        final CsvMapper csvMapper = new CsvMapper();
        final CsvSchema.Builder schema = CsvSchema.builder();
        schema.addColumn("_id");
        usedFields.forEach(name -> schema.addColumn(name));
        schema.addColumn("_errors");
        return csvMapper.writer(schema.build().withHeader()).writeValueAsString(results);
      } catch (IOException e) {
        throw new CsvParserException(e.getMessage(), e);
      }
    }
    
  }
  
  public static class CsvParserException extends RuntimeException {
    private static final long serialVersionUID = -6081528821152646981L;

    public CsvParserException(String message, Throwable cause) {
      super(message, cause);
    }

    public CsvParserException(String message) {
      super(message);
    }
  } 
}
