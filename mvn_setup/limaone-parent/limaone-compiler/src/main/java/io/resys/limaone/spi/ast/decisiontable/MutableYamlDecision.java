package io.resys.limaone.spi.ast.decisiontable;

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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.resys.limaone.ast.DecisionTable_CST.YamlDecision;
import io.resys.limaone.ast.DecisionTable_CST.YamlTable;
import io.resys.limaone.ast.DecisionTable_CST.YamlTableCell;
import io.resys.limaone.ast.DecisionTable_CST.YamlTableHeader;
import io.resys.limaone.ast.DecisionTable_CST.YamlTableRow;
import io.resys.limaone.ast.DecisionTable_CST.YamlValueSet;
import io.resys.limaone.model.Parameter.ValueType;
import io.resys.limaone.spi.ast.AST_Exception;
import io.resys.limaone.yaml.MutableYaml;
import jakarta.annotation.Nullable;

public class MutableYamlDecision extends MutableYaml implements YamlDecision {
  public static final long serialVersionUID = 8492235102091866790L;
  public static final String KEY_NAME = "name";
  public static final String KEY_DESC = "description";
  public static final String KEY_HIT_POLICY = "hitPolicy";
  public static final String KEY_VALUE_SETS = "valueSets";
  public static final String KEY_TABLE = "table";
  private static final List<String> qualifiedTypes = Arrays.asList(ValueType.values()).stream().map(e -> e.name()).toList();
  
  private NodeValueSets valueSets;
  private NodeTable table;
  private String value;

  public MutableYamlDecision() {
    super(null, -2, null, null, null);
  }

  @Override
  public Yaml getName() {
    return get(KEY_NAME);
  }
  @Override
  public Yaml getDescription() {
    return get(KEY_DESC);
  }
  @Override
  public Yaml getHitPolicy() {
    return get(KEY_HIT_POLICY);
  }
  @Override
  public Map<String, YamlValueSet> getValueSetNodes() {
    return valueSets == null ? Collections.emptyMap() : valueSets.getValueSets();
  }
  @Override
  public YamlTable getTable() {
    return table;
  }
  @Override
  public String getValue() {
    return value;
  }
  public MutableYamlDecision setValue(String value) {
    this.value = value;
    return this;
  }
  @Override
  public MutableYamlDecision setEnd(int value) {
    super.setEnd(value);
    if(table != null) {
      table.parse();
    }
    return this;
  }
  @Override
  public MutableYaml addChild(NodeSource source, int indent, String keyword, String value) {
    if(KEY_VALUE_SETS.equals(keyword)) {
      if(valueSets == null) {
        valueSets = new NodeValueSets(source, indent, keyword, value, this);
        addChild(valueSets);
      }
      return valueSets;
    } else if(KEY_TABLE.equals(keyword)) {
      if(table == null) {
        table = new NodeTable(source, indent, keyword, value, this);
        addChild(table);
      }
      return table;
    }
    return super.addChild(source, indent, keyword, value);
  }

  private static class NodeValueSets extends MutableYaml {
    private static final long serialVersionUID = 8989618439864849749L;
    private final Map<String, YamlValueSet> valueSets = new HashMap<>();
    
    public NodeValueSets(NodeSource source, int indent, String keyword, String value, MutableYaml parent) {
      super(source, indent, keyword, value, parent);
    }
    
    public Map<String, YamlValueSet> getValueSets() {
      return Collections.unmodifiableMap(valueSets);
    }
    
    @Override
    public MutableYaml addChild(NodeSource source, int indent, String keyword, String value) {
      MutableYamlValueSet result = new MutableYamlValueSet(source, indent, keyword, value, this);
      if(valueSets.containsKey(result.getKeyword())) {
        String message = String.format("Duplicate value set: %s!", result.getKeyword());
        throw new AST_Exception(message);
      }
      valueSets.put(result.getKeyword(), result);
      return addChild(result);
    }
  }

  public static class NodeTable extends MutableYaml implements YamlTable {
    private static final long serialVersionUID = 2001644047832806256L;
    private final List<YamlTableHeader> headers = new ArrayList<>();
    private final List<YamlTableRow> rows = new ArrayList<>();
    private final Map<String, YamlTableHeader> inputHeaders = new HashMap<>();
    private final Map<String, YamlTableHeader> outputHeaders = new HashMap<>();
    private String markdownContent;
    
    
    public NodeTable(NodeSource source, int indent, String keyword, String value, MutableYaml parent) {
      super(source, indent, keyword, value, parent);
      this.markdownContent = value;
    }
    
    @Override
    public NodeTable addMultiline(String content) {
      this.markdownContent += content + "\n";
      return this;
    }
    
    public void parse() {
      final var lines = markdownContent.split("\n");
      if (lines.length < 2) {
        return;
      }

      // Parse header line
      final var headerLine = lines[0].trim();
      Integer outputMarkerIndex = null;
      if (headerLine.startsWith("|") && headerLine.endsWith("|")) {
        outputMarkerIndex = parseHeaders(headerLine);
      }

      // Parse data rows (skip separator line)
      for (int i = 2; i < lines.length; i++) {
        final var rowLine = lines[i].trim();
        if (rowLine.startsWith("|") && rowLine.endsWith("|")) {
          parseRow(rowLine, i - 2, outputMarkerIndex);
        }
      }
    }

    @Override
    public String getMarkdownContent() {
      return markdownContent;
    }

    @Override
    public Collection<YamlTableHeader> getHeaders() {
      return Collections.unmodifiableList(headers);
    }

    @Override
    public Collection<YamlTableRow> getRows() {
      return Collections.unmodifiableList(rows);
    }

    @Override
    public Map<String, YamlTableHeader> getInputHeaders() {
      return Collections.unmodifiableMap(inputHeaders);
    }

    @Override
    public Map<String, YamlTableHeader> getOutputHeaders() {
      return Collections.unmodifiableMap(outputHeaders);
    }

    private Integer parseHeaders(String headerLine) {
      final var columns = headerLine.substring(1, headerLine.length() - 1).split("\\|");
      boolean outputMode = false;
      Integer outputMarkerIndex = null;
      

      
      for (int index = 0; index < columns.length; index++) {
        final var column = columns[index].trim();
        if ("->".equals(column)) {
          outputMarkerIndex = index;
          outputMode = true;
          continue;
        }

        final var parts = column.split(":");
        final String name;
        final String type;
        
        if(parts.length == 1) {
          final var singleValue = parts[0].trim();
          final var isType = qualifiedTypes.contains(singleValue.toUpperCase());
          name = isType ? singleValue + "-" + index : singleValue;
          type = isType ? singleValue.toUpperCase() : ValueType.STRING.name();
        } else {
          name = parts[0].trim();
          
          final var singleValue = parts[1].trim();
          final var isType = qualifiedTypes.contains(singleValue.toUpperCase());
          type = isType ? singleValue.toUpperCase() : ValueType.STRING.name();
        }
        
        final var headerLineNumber = this.getSource().getLineNumber() + 1;
        final var node = new NodeSource(headerLine, headerLineNumber);
        final var header = new MutableYamlTableHeader(node, 0, name, type, this, index, outputMode);
        
        headers.add(header);
        if (outputMode) {
          outputHeaders.put(name, header);
        } else {
          inputHeaders.put(name, header);
        }
      }
      return outputMarkerIndex;
    }

    private void parseRow(String rowLine, int rowIndex, Integer outputMarkerIndex) {
      final var cells = rowLine.substring(1, rowLine.length() - 1).split("\\|");
      final var row = new MutableYamlTableRow(null, 0, "row_" + rowIndex, null, this, rowIndex);

      
      for (int i = 0; i < cells.length && i < headers.size(); i++) {
        final var header = headers.get(i);
        
        final var isOutputMarker = outputMarkerIndex != null && outputMarkerIndex == i;
        final var shift = isOutputMarker ? 1 : 0;
        final var cellIndex = i + shift;
        final var cellValue = cells[cellIndex].trim();

        final var cell = new MutableYamlTableCell(
            null, 0, "cell_" + rowIndex + "_" + i, cellValue, row, 
            header.getName(), i, rowIndex
        );
        row.addCell(cell);
      }
      
      rows.add(row);
    }
  }

  private static class MutableYamlValueSet extends MutableYaml implements YamlValueSet {
    private static final long serialVersionUID = 8910489078429824772L;
    private final Collection<String> values;
    
    public MutableYamlValueSet(NodeSource source, int indent, String keyword, String value, MutableYaml parent) {
      super(source, indent, keyword, value, parent);
      this.values = parseValues(value);
    }
    
    private Collection<String> parseValues(String value) {
      List<String> result = new ArrayList<>();
      if (value != null) {
        String[] parts = value.split(",");
        for (String part : parts) {
          result.add(part.trim());
        }
      }
      return result;
    }

    @Override
    public String getName() {
      return getKeyword();
    }

    @Override
    public Collection<String> getValues() {
      return Collections.unmodifiableCollection(values);
    }
  }

  private static class MutableYamlTableHeader extends MutableYaml implements YamlTableHeader {
    private static final long serialVersionUID = 8910489078429824772L;
    private final String name;
    private final String type;
    private final boolean isOutput;
    private final int columnIndex;

    public MutableYamlTableHeader(NodeSource source, int indent, String keyword, String value, 
                                 MutableYaml parent, int columnIndex, boolean isOutput) {
      super(source, indent, keyword, value, parent);
      this.name = keyword;
      this.type = value != null ? value : "STRING";
      this.isOutput = isOutput;
      this.columnIndex = columnIndex;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public String getType() {
      return type;
    }

    @Override
    public boolean isOutput() {
      return isOutput;
    }

    @Override
    public int getColumnIndex() {
      return columnIndex;
    }
  }

  private static class MutableYamlTableRow extends MutableYaml implements YamlTableRow {
    private static final long serialVersionUID = 8910489078429824772L;
    private final List<YamlTableCell> cells = new ArrayList<>();
    private final Map<String, YamlTableCell> cellsByHeader = new HashMap<>();
    private final int rowIndex;

    public MutableYamlTableRow(NodeSource source, int indent, String keyword, String value, 
                              MutableYaml parent, int rowIndex) {
      super(source, indent, keyword, value, parent);
      this.rowIndex = rowIndex;
    }

    @Override
    public int getRowIndex() {
      return rowIndex;
    }

    @Override
    public Collection<YamlTableCell> getCells() {
      return Collections.unmodifiableList(cells);
    }

    @Override
    public Map<String, YamlTableCell> getCellsByHeader() {
      return Collections.unmodifiableMap(cellsByHeader);
    }

    public void addCell(YamlTableCell cell) {
      cells.add(cell);
      cellsByHeader.put(cell.getHeaderName(), cell);
    }
  }

  private static class MutableYamlTableCell extends MutableYaml implements YamlTableCell {
    private static final long serialVersionUID = 8910489078429824772L;
    private final String headerName;
    private final String expression;
    private final int columnIndex;
    private final int rowIndex;

    public MutableYamlTableCell(NodeSource source, int indent, String keyword, String value, 
                               MutableYaml parent, String headerName, int columnIndex, int rowIndex) {
      super(source, indent, keyword, value, parent);
      this.headerName = headerName;
      this.expression = value;
      this.columnIndex = columnIndex;
      this.rowIndex = rowIndex;
    }

    @Override
    public String getHeaderName() {
      return headerName;
    }

    @Override
    public String getExpression() {
      return expression;
    }

    @Override
    public int getColumnIndex() {
      return columnIndex;
    }

    @Override
    public int getRowIndex() {
      return rowIndex;
    }

    @Override
    @Nullable
    public String getValue() {
      return expression;
    }
  }
}
