package io.resys.hdes.client.spi.decision;

/*-
 * #%L
 * wrench-assets-bundle
 * %%
 * Copyright (C) 2016 - 2018 Copyright 2016 ReSys OÜ
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import io.resys.hdes.client.api.ast.AstDecision;
import io.resys.hdes.client.api.ast.AstDecision.AstDecisionRow;
import io.resys.hdes.client.api.ast.TypeDef;

public class DecisionCSVBuilder {

  public static String build(AstDecision dt) {
    StringBuilder stringBuilder = new StringBuilder();
    List<TypeDef> headers = new ArrayList<>();
    List<String> headerNames = new ArrayList<>();
    
    for(final var dataType : dt.getHeaders().getAcceptDefs()) {
      headers.add(dataType);
      headerNames.add(dataType.getName());
    }    
    for(final var dataType : dt.getHeaders().getReturnDefs()) {
      headers.add(dataType);
      headerNames.add(dataType.getName());
    }

    try {
      CSVPrinter csvPrinter = new CSVPrinter(stringBuilder, CSVFormat.DEFAULT.withDelimiter(';').withHeader(headerNames.toArray(new String[] {})));
      print(csvPrinter, dt.getRows().iterator(), headerNames);
      return stringBuilder.toString();
    } catch(IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  protected static void print(CSVPrinter csvPrinter, Iterator<AstDecisionRow> it, List<String> headerNames) throws IOException {
    if(!it.hasNext()) {
      return;
    }

    final var node = it.next();
    Map<String, Object> entries = new HashMap<>();
    node.getCells().forEach(cell -> entries.put(cell.getHeader(), cell.getValue()));
    
    List<Object> values = new ArrayList<>();
    for(String name : headerNames) {
      Object value = entries.get(name);
      if(value == null) {
        values.add("");  
      } else {
        values.add(value);
      }
    }
    csvPrinter.printRecord(values);
    print(csvPrinter, it, headerNames);
  }
}
