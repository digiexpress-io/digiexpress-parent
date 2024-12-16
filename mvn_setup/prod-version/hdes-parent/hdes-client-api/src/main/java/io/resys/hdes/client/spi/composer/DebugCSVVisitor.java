package io.resys.hdes.client.spi.composer;

/*-
 * #%L
 * hdes-client-api
 * %%
 * Copyright (C) 2020 - 2021 Copyright 2020 ReSys OÜ
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

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.ast.AstBody;
import io.resys.hdes.client.api.ast.TypeDef;
import io.resys.hdes.client.api.ast.TypeDef.Direction;
import io.resys.hdes.client.api.exceptions.HdesBadRequestException;
import io.resys.hdes.client.api.programs.DecisionProgram;
import io.resys.hdes.client.api.programs.FlowProgram;
import io.resys.hdes.client.api.programs.Program.ProgramResult;
import io.resys.hdes.client.api.programs.ProgramEnvir;
import io.resys.hdes.client.api.programs.ProgramEnvir.ProgramWrapper;
import io.resys.hdes.client.api.programs.ServiceProgram;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class DebugCSVVisitor {
  private final HdesClient client;
  private final ProgramEnvir envir;
  private final ProgramWrapper<?, ?> wrapper;
  private final Map<String, TypeDef> wrapperInputs;
  private final CsvMapper csvMapper = new CsvMapper();
  private final Set<String> usedFields = new HashSet<>();

  public DebugCSVVisitor(HdesClient client, ProgramWrapper<?, ?> wrapper, ProgramEnvir envir) {
    super();
    this.client = client;
    this.wrapper = wrapper;
    this.envir = envir;
    this.wrapperInputs = getWrapperInputs(wrapper);
  }

  public ProgramResult visit(String input) {
    try {
      final CSVParser parser = CSVParser.parse(input, CSVFormat.DEFAULT.withDelimiter(';').withIgnoreEmptyLines());
      final List<CSVRecord> records = parser.getRecords();
      if (records.isEmpty()) {
        return null;
      }

      // records
      final Iterator<CSVRecord> iterator = records.iterator();

      // header names
      final var headers = visitHeaders(iterator);

      // data row
      final CSVRecord row = iterator.next();
      final var result = visitRow(row, headers);
      return result;
    } catch (IOException e) {
      throw new HdesBadRequestException(e.getMessage(), e);
    }
  }

  public String visitMultiple(String input) {
    try {
      final CSVParser parser = CSVParser.parse(input, CSVFormat.DEFAULT.withDelimiter(';').withIgnoreEmptyLines());
      final List<CSVRecord> records = parser.getRecords();
      if (records.isEmpty()) {
        return null;
      }

      // records
      final Iterator<CSVRecord> iterator = records.iterator();

      // header names
      final var headers = visitHeaders(iterator);

      // data rows
      final List<Map<String, Serializable>> results = new ArrayList<>();
      while (iterator.hasNext()) {
        final CSVRecord row = iterator.next();
        final var rows = visitRowMultiple(row, headers);
        results.addAll(rows);
      }

      final CsvSchema.Builder schema = CsvSchema.builder();
      schema.addColumn("_id");
      usedFields.forEach(name -> schema.addColumn(name));
      schema.addColumn("_errors");

      return csvMapper.writer(schema.build().withHeader()).writeValueAsString(results);
    } catch (IOException e) {
      throw new HdesBadRequestException(e.getMessage(), e);
    }
  }

  private Map<Integer, String> visitHeaders(Iterator<CSVRecord> iterator) {
    final Map<Integer, String> headers = new HashMap<>();
    int headerIndex = 0;
    for (String header : iterator.next()) {
      headers.put(headerIndex++, header);
    }
    return headers;
  }

  public ProgramResult visitRow(CSVRecord row, Map<Integer, String> headers) throws IOException {
    long transactionId = row.getRecordNumber() - 1;
    final var input = visitProgramInput(row, headers);
    return visitProgram(input, transactionId);
  }

  public List<Map<String, Serializable>> visitRowMultiple(CSVRecord row, Map<Integer, String> headers) {
    long transactionId = row.getRecordNumber() - 1;
    try {
      final var input = visitProgramInput(row, headers);
      return visitProgramMultiple(input, transactionId);
    } catch (Exception e) {
      return Arrays.asList(Map.of(
              "_id", transactionId,
              "_errors", e.getMessage()
      ));
    }
  }

  public Map<String, Serializable> visitProgramInput(CSVRecord row, Map<Integer, String> headers) {
    final Map<String, Serializable> inputEntity = new HashMap<>();
    int columnIndex = 0;
    for (final var columnValue : row) {
      final var columnName = headers.get(columnIndex++);
      final var typeDef = this.wrapperInputs.get(columnName);
      if (typeDef == null) {
        continue;
      }
      inputEntity.put(columnName, typeDef.getDeserializer().deserialize(typeDef, columnValue));
    }
    return inputEntity;
  }

  private ProgramResult visitProgram(Map<String, Serializable> input, long transactionId) throws IOException {
    switch (wrapper.getType()) {
      case FLOW:
        return visitFlow(input, transactionId);
      case FLOW_TASK:
        return visitFlowTask(input, transactionId);
      case DT:
        return visitDecision(input, transactionId);
      default:
        throw new HdesBadRequestException("Can't debug: '" + wrapper.getType() + "'!");
    }
  }

  private List<Map<String, Serializable>> visitProgramMultiple(Map<String, Serializable> input, long transactionId) throws IOException {
    switch (wrapper.getType()) {
      case FLOW:
        return visitFlowMultiple(input, transactionId);
      case FLOW_TASK:
        return visitFlowTaskMultiple(input, transactionId);
      case DT:
        return visitDecisionMultiple(input, transactionId);
      default:
        throw new HdesBadRequestException("Can't debug: '" + wrapper.getType() + "'!");
    }
  }


  private ProgramResult visitFlow(Map<String, Serializable> input, long transactionId) {
    final var body = client.executor(envir).inputMap(input).flow(wrapper.getId()).andGetBody();
    final var last = body.getReturns();
    this.usedFields.addAll(last.keySet());
    return body;
  }

  private List<Map<String, Serializable>> visitFlowMultiple(Map<String, Serializable> input, long transactionId) {
    final var body = client.executor(envir).inputMap(input).flow(wrapper.getId()).andGetBody();
    final var last = body.getReturns();
    this.usedFields.addAll(last.keySet());
    return visitProgramBody(last, transactionId);
  }

  private List<Map<String, Serializable>> visitFlowTaskMultiple(Map<String, Serializable> input, long transactionId) {
    final var body = client.executor(envir).inputMap(input).service(wrapper.getId()).andGetBody();
    final var values = body.getValue();
    final var result = new HashMap<String, Serializable>(client.mapper().toMap(body.getValue()));
    this.usedFields.addAll(result.keySet());
    return visitProgramBody(result, transactionId);
  }

  private ProgramResult visitFlowTask(Map<String, Serializable> input, long transactionId) {
    final var body = client.executor(envir).inputMap(input).service(wrapper.getId()).andGetBody();
    return body;
  }

  private ProgramResult visitDecision(Map<String, Serializable> input, long transactionId) {
    final var body = client.executor(envir).inputMap(input).decision(wrapper.getId()).andGetBody();
    return body;
  }

  private List<Map<String, Serializable>> visitDecisionMultiple(Map<String, Serializable> input, long transactionId) {
    final var body = client.executor(envir).inputMap(input).decision(wrapper.getId()).andGetBody();
    final var matches = body.getMatches().stream().map(match -> {
      final var result = new HashMap<String, Serializable>(client.mapper().toMap(match.getReturns().stream().collect(Collectors.toMap(
              returnValue -> returnValue.getHeaderType().getName(),
              returnValue -> returnValue.getUsedValue()
      ))));
      this.usedFields.addAll(result.keySet());
      return result;
    }).collect(Collectors.toList());
    this.usedFields.addAll(matches.get(0).keySet());
    return visitProgramBody(matches.get(0), transactionId);
  }

  private List<Map<String, Serializable>> visitProgramBody(Map<String, Serializable> last, long transactionId) {
    if (last.size() == 1) {
      final var firstValue = last.values().iterator().next();
      if (firstValue instanceof Collection) {
        final var rows = new ArrayList<Map<String, Serializable>>();
        for (final var entry : ((Collection<?>) firstValue)) {
          final var result = new HashMap<String, Serializable>();
          result.put("_id", transactionId);
          result.putAll(client.mapper().toMap(entry));
          rows.add(result);
        }
        return rows;
      }
    }

    final var results = new HashMap<String, Serializable>(last);
    results.put("_id", transactionId);
    return Arrays.asList(results);
  }

  private Map<String, TypeDef> getWrapperInputs(ProgramWrapper<?, ?> wrapper) {
    if (wrapper.getType() == AstBody.AstBodyType.FLOW) {
      FlowProgram flowProgram = (FlowProgram) wrapper.getProgram().get();
      return flowProgram.getAcceptDefs().stream()
              .filter(h -> h.getDirection() == Direction.IN)
              .collect(Collectors.toMap(h -> h.getName(), h -> h));
    } else if (wrapper.getType() == AstBody.AstBodyType.FLOW_TASK) {
      ServiceProgram serviceProgram = (ServiceProgram) wrapper.getProgram().get();
      return serviceProgram.getTypeDef0().getProperties().stream()
              .filter(h -> h.getDirection() == Direction.IN)
              .collect(Collectors.toMap(h -> h.getName(), h -> h));
    } else if (wrapper.getType() == AstBody.AstBodyType.DT) {
      DecisionProgram decisionProgram = (DecisionProgram) wrapper.getProgram().get();
      List<DecisionProgram.DecisionRow> decisionRows = decisionProgram.getRows();
      Map<String, TypeDef> decisionInputs = new HashMap<>();
      for (DecisionProgram.DecisionRow decisionRow : decisionRows) {
        decisionRow.getAccepts().stream()
                .filter(h -> h.getKey().getDirection() == Direction.IN)
                .forEach(h -> decisionInputs.put(h.getKey().getName(), h.getKey()));
      }
      return decisionInputs;
    } else {
      throw new IllegalArgumentException("Unsupported wrapper type: " + wrapper.getType());
    }
  }

}
