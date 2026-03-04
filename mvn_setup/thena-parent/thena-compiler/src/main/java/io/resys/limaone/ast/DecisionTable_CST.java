package io.resys.limaone.ast;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.immutables.value.Value;

import io.resys.limaone.ast.Yaml_CST.Yaml;
import jakarta.annotation.Nullable;

public interface DecisionTable_CST extends Serializable {

  @Value.Immutable
  interface YamlValueSetType extends DecisionTable_CST {
    String getName();
    Collection<String> getValues();
  }

  interface YamlParseTree extends Yaml {
    Yaml getName();
    Yaml getDescription();
    Yaml getHitPolicy();
    Map<String, YamlValueSet> getValueSetNodes();
    YamlTable getTable();
  }

  interface YamlValueSet extends Yaml {
    String getName();
    Collection<String> getValues();
  }

  interface YamlTable extends Yaml {
    String getMarkdownContent();
    Collection<YamlTableHeader> getHeaders();
    Collection<YamlTableRow> getRows();
    Map<String, YamlTableHeader> getInputHeaders();
    Map<String, YamlTableHeader> getOutputHeaders();
  }

  interface YamlTableHeader extends Yaml {
    String getName();
    String getType();
    boolean isOutput();
    int getColumnIndex();
  }

  interface YamlTableRow extends Yaml {
    int getRowIndex();
    Collection<YamlTableCell> getCells();
    Map<String, YamlTableCell> getCellsByHeader();
  }

  interface YamlTableCell extends Yaml {
    String getHeaderName();
    String getExpression();
    int getColumnIndex();
    int getRowIndex();
    @Nullable String getValue();
  }
}