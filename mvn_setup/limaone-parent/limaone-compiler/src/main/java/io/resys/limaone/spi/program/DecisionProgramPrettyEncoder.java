package io.resys.limaone.spi.program;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.resys.limaone.ast.DecisionTable_AST;
import io.resys.limaone.ast.DecisionTable_AST.DecisionRowNode;
import io.resys.limaone.ast.Simple_AST.Headers_AST;
import io.resys.limaone.model.Parameter;

public class DecisionProgramPrettyEncoder {

  public static String encodePrettily(DecisionTable_AST dt) {
    if (dt.getRows() == null || dt.getRows().isEmpty()) {
      return "Empty Decision Table: " + dt.getName();
    }

    final var result = new StringBuilder();
    result.append("Decision Table: ").append(dt.getName()).append("\n");
    result.append("Hit Policy: ").append(dt.getHitPolicy()).append("\n\n");

    final var headers = dt.getHeaders();
    final var allHeaders = new ArrayList<Parameter>();
    allHeaders.addAll(headers.getAcceptDefs());
    allHeaders.addAll(headers.getReturnDefs());

    final var columnWidths = calculateColumnWidths(dt, allHeaders);

    printHeaderRow(result, headers, columnWidths);
    printSeparator(result, columnWidths);

    final var sortedRows = new ArrayList<>(dt.getRows());
    sortedRows.sort((a, b) -> Integer.compare(a.getOrder(), b.getOrder()));

    for (var row : sortedRows) {
      printDataRow(result, row, allHeaders, columnWidths);
    }

    return result.toString();
  }

  private static int[] calculateColumnWidths(DecisionTable_AST dt, List<Parameter> allHeaders) {
    final var headers = dt.getHeaders();
    final int[] widths = new int[allHeaders.size()];
    
    // Initialize with header widths
    for (int i = 0; i < allHeaders.size(); i++) {
      final var header = allHeaders.get(i);
      final var isInput = headers.getAcceptDefs().contains(header);
      final var marker = isInput ? " IN" : " OUT";
      widths[i] = (header.getName() + marker).length();
    }
    
    // Check data row widths
    for (var row : dt.getRows()) {
      final var cellMap = new HashMap<String, String>();
      for (var cell : row.getCells()) {
        cellMap.put(cell.getHeader(), cell.getValue() != null ? cell.getValue() : "");
      }
      
      for (int i = 0; i < allHeaders.size(); i++) {
        final var id = allHeaders.get(i).getId();
        final var cellValue = cellMap.getOrDefault(id, "");
        widths[i] = Math.max(widths[i], cellValue.length());
      }
    }
    
    return widths;
  }

  private static void printHeaderRow(StringBuilder sb, Headers_AST headers, int[] widths) {
    sb.append("|");
    int i = 0;
    
    for (var header : headers.getAcceptDefs()) {
      final var name = header.getName() + " IN";
      sb.append(String.format(" %-" + widths[i++] + "s |", name));
    }
    
    for (var header : headers.getReturnDefs()) {
      final var name = header.getName() + " OUT";
      sb.append(String.format(" %-" + widths[i++] + "s |", name));
    }
    
    sb.append("\n");
  }

  private static void printSeparator(StringBuilder sb, int[] widths) {
    sb.append("|");
    for (int width : widths) {
      sb.append("-".repeat(width + 2)).append("|");
    }
    sb.append("\n");
  }

  private static void printDataRow(StringBuilder sb, DecisionRowNode row, List<Parameter> allHeaders, int[] widths) {
    final var cellMap = new HashMap<String, String>();
    for (var cell : row.getCells()) {
      cellMap.put(cell.getHeader(), cell.getValue() != null ? cell.getValue() : "");
    }
    
    sb.append("|");
    for (int i = 0; i < allHeaders.size(); i++) {
      final var id = allHeaders.get(i).getId();
      final var value = cellMap.getOrDefault(id, "");
      sb.append(String.format(" %-" + widths[i] + "s |", value));
    }
    sb.append("\n");
  }
}
