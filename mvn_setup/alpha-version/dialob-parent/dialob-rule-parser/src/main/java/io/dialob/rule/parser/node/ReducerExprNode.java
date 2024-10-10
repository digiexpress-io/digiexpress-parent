package io.dialob.rule.parser.node;

import io.dialob.rule.parser.api.ValueType;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true, of = {"nodeOperator", "arguments"})
public class ReducerExprNode extends CallExprNode {

  private static final long serialVersionUID = -5417814591284802033L;

  public ReducerExprNode(NodeBase parent, @NotNull NodeOperator nodeOperator, Span span) {
    super(parent, nodeOperator, null, span);
  }


}
