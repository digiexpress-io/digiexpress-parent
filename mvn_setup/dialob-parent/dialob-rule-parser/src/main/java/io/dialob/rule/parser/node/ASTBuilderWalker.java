package io.dialob.rule.parser.node;

import com.google.common.base.CaseFormat;
import io.dialob.rule.parser.DialobRuleBaseListener;
import io.dialob.rule.parser.DialobRuleParser;
import io.dialob.rule.parser.ParserUtil;
import io.dialob.rule.parser.api.*;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class ASTBuilderWalker extends DialobRuleBaseListener {

  private final String namespace;

  private final VariableFinder variableFinder;

  private ASTBuilder builder;

  private final Map<NodeBase,String> asyncFunctionVariables;

  private ErrorLogger errorLogger = new ErrorLogger() {
    @Override
    public void logError(String errorCode, Span span) {
      LOGGER.error("Compiler error {} at {}", errorCode, span);
    }

    @Override
    public void logError(String errorCode, Object[] args, Span span) {
      LOGGER.error("Compiler error {} : {} at {}", errorCode, args, span);
    }
  };

  public static final VariableFinder DUMMY_VARIABLE_FINDER = new VariableFinder() {

    @Override
    public @Nullable String getScope() {
      return null;
    }

    @Override
    public ValueType typeOf(String variableName) {
      return null;
    }

    @Override
    public ValueType returnTypeOf(String functionName, ValueType... argTypes) {
      return null;
    }

    @Override
    public boolean isAsync(String functionName) {
      return false;
    }

  };

  public ASTBuilderWalker(@NotNull VariableFinder variableFinder, Map<NodeBase, String> asyncFunctionVariables) {
    this(null, variableFinder, asyncFunctionVariables);
  }

  ASTBuilderWalker(String namespace, @NotNull VariableFinder variableFinder, Map<NodeBase, String> asyncFunctionVariables) {
    this.namespace = namespace;
    this.variableFinder = variableFinder;
    this.builder = new ASTBuilder();
    this.asyncFunctionVariables = asyncFunctionVariables;
  }

  public void setErrorLogger(ErrorLogger errorLogger) {
    this.errorLogger = errorLogger;
  }

  private void pop() {
    builder = builder.closeExpr();
  }

  public ASTBuilder getBuilder() {
    return builder;
  }

  protected List<ValueType> getLhsAndRhsValueTypes() {
    return builder
      .getTopNode()
      .getSubnodes()
      .stream()
      .map(NodeBase::getValueType)
      .collect(Collectors.toList());
  }

  protected NodeBase getLhs() {
    NodeBase node = builder.getTopNode();
    if (node instanceof CallExprNode) {
      CallExprNode callExprNode = (CallExprNode) node;
      return callExprNode.getLhs();
    }
    return null;
  }

  protected NodeBase getRhs() {
    NodeBase node = builder.getTopNode();
    if (node instanceof CallExprNode) {
      CallExprNode callExprNode = (CallExprNode) node;
      return callExprNode.getRhs();
    }
    return null;
  }


  @Override
  public void enterNotExpr(DialobRuleParser.NotExprContext ctx) {
    builder = builder
      .notExprNode(Span.of(ctx))
      .setValueType(ValueType.BOOLEAN);

  }

  @Override
  public void exitNotExpr(DialobRuleParser.NotExprContext ctx) {
    List<ValueType> valueTypes = getLhsAndRhsValueTypes();
    try {
      if (valueTypes.size() != 1) {
        errorLogger.logError(CompilerErrorCode.ONLY_ONE_ARGUMENT_FOR_NOT, Span.of(ctx));
        return;
      }
      ValueType valueType = valueTypes.get(0);
      if (valueType == null) {
        reportTypeAnalysisProblem(ctx);
        return;
      }
      if (valueType != ValueType.BOOLEAN) {
        errorLogger.logError(CompilerErrorCode.CANNOT_EVAL_NOT_FOR_NON_BOOLEAN_TYPE, new Object[]{valueType}, Span.of(ctx));
      }
    } finally {
      pop();

    }
  }

  @Override
  public void enterInOperExpr(DialobRuleParser.InOperExprContext ctx) {
    boolean not = ctx.NOT() != null;
    builder = builder.logicExprNode(not ? "notIn" : "in", Span.of(ctx))
      .setValueType(ValueType.BOOLEAN);
  }

  @Override
  public void exitInOperExpr(DialobRuleParser.InOperExprContext ctx) {
    NodeBase rhs = getRhs();
    if (rhs instanceof IdExprNode) {
      IdExprNode idExprNode = (IdExprNode) rhs;
      if (idExprNode.getValueType() == null || !idExprNode.getValueType().isArray()) {
        errorLogger.logError(CompilerErrorCode.ARRAY_TYPE_EXPECTED, new Object[]{rhs}, rhs.getSpan());
      }
    }
    NodeBase lhs = getLhs();
    if (lhs.getValueType() != null && lhs.getValueType().isArray()) {
      errorLogger.logError(CompilerErrorCode.ARRAY_TYPE_UNEXPECTED, new Object[]{lhs}, lhs.getSpan());
    }
    pop();
  }

  @Override
  public void enterLogicExpr(DialobRuleParser.LogicExprContext ctx) {
    builder = builder.logicExprNode(ctx.op.getText(), Span.of(ctx))
      .setValueType(ValueType.BOOLEAN);
  }

  @Override
  public void exitLogicExpr(DialobRuleParser.LogicExprContext ctx) {
    List<ValueType> valueTypes = getLhsAndRhsValueTypes();
    try {
      final String operator = ctx.op.getText();
      if (valueTypes.size() != 2) {
        errorLogger.logError(CompilerErrorCode.OPERATOR_REQUIRES_2_OPERANDS, new Object[]{operator}, Span.of(ctx));
        return;
      }
      ValueType lhs = valueTypes.get(0);
      ValueType rhs = valueTypes.get(1);
      if (lhs == null || rhs == null) {
        reportTypeAnalysisProblem(ctx);
        return;
      }
      if (lhs != ValueType.BOOLEAN) {
        NodeBase node = builder.getTopNode().getSubnodes().get(0);
        errorLogger.logError(CompilerErrorCode.BOOLEAN_VALUE_EXPECTED, new Object[]{lhs, rhs}, node.getSpan());
      }
      if (rhs != ValueType.BOOLEAN) {
        NodeBase node = builder.getTopNode().getSubnodes().get(1);
        errorLogger.logError(CompilerErrorCode.BOOLEAN_VALUE_EXPECTED, new Object[]{lhs, rhs}, node.getSpan());
      }
    } finally {
      pop();
    }
  }

  @Override
  public void enterMatchesExpr(DialobRuleParser.MatchesExprContext ctx) {
    boolean not = ctx.NOT() != null;
    builder = builder.infixExprNode(not ? "notMatches" : "matches", Span.of(ctx))
      .setValueType(ValueType.BOOLEAN);
  }

  @Override
  public void exitMatchesExpr(DialobRuleParser.MatchesExprContext ctx) {
    List<ValueType> valueTypes = getLhsAndRhsValueTypes();
    try {
      final String operator = ctx.op.getText();
      if (valueTypes.size() != 2) {
        errorLogger.logError(CompilerErrorCode.OPERATOR_REQUIRES_2_OPERANDS, new Object[]{operator}, Span.of(ctx));
        return;
      }
      ValueType lhs = valueTypes.get(0);
      ValueType rhs = valueTypes.get(1);
      if (lhs == null || rhs == null) {
        reportTypeAnalysisProblem(ctx);
        return;
      }
      if (lhs != ValueType.STRING) {
        NodeBase node = builder.getTopNode().getSubnodes().get(0);
        errorLogger.logError(CompilerErrorCode.ONLY_STRINGS_CAN_BE_MATCHED, new Object[]{lhs}, node.getSpan());
      }
      if (rhs != ValueType.STRING) {
        NodeBase node = builder.getTopNode().getSubnodes().get(1);
        errorLogger.logError(CompilerErrorCode.MATCHER_DEFINITION_NEEDS_TO_BE_STRING, new Object[]{rhs}, node.getSpan());
      }
    } finally {
      pop();
    }
  }

  @Override
  public void enterRelationExpr(DialobRuleParser.RelationExprContext ctx) {
    builder = builder.relationExprNode(ctx.op.getText(), Span.of(ctx))
      .setValueType(ValueType.BOOLEAN);
  }

  @Override
  public void exitRelationExpr(DialobRuleParser.RelationExprContext ctx) {
    final NodeBase lhs = getLhs();
    final NodeBase rhs = getRhs();
    try {
      final String operator = ctx.op.getText();
      if (rhs == null) {
        errorLogger.logError(CompilerErrorCode.OPERATOR_REQUIRES_2_OPERANDS, new Object[]{operator}, Span.of(ctx));
        return;
      }
      if (lhs.getValueType() == null || rhs.getValueType() == null) {
        reportTypeAnalysisProblem(ctx);
        return;
      }

      ConstExprNode stringConstant = null;
      ValueType coercionType = null;
      if (isConstantString(lhs) && !rhs.isConstant()) {
        stringConstant = (ConstExprNode) lhs;
        coercionType = rhs.getValueType();
      } else if (!lhs.isConstant() && isConstantString(rhs)) {
        stringConstant = (ConstExprNode) rhs;
        coercionType = lhs.getValueType();
      }
      if (stringConstant != null && coercionType != null && coercionType != ValueType.STRING) {
        coerce(stringConstant, coercionType);
      }

      String code;
      boolean ok;
      switch (operator) {
        case "=":
        case "!=":
          ok = lhs.getValueType().canEqualWith(rhs.getValueType());
          code = CompilerErrorCode.NO_EQUALITY_RELATION_BETWEEN_TYPES;
          break;
        default:
          ok = lhs.getValueType().canOrderWith(rhs.getValueType());
          code = CompilerErrorCode.NO_ORDER_RELATION_BETWEEN_TYPES;
          break;
      }
      if (!ok) {
        errorLogger.logError(code, new Object[]{lhs.getValueType(), rhs.getValueType()}, Span.of(ctx));
      }
    } finally {
      pop();
    }
  }

  private void coerce(ConstExprNode stringConstant, ValueType coercionType) {
    if (coercionType == ValueType.DATE) {
      if (stringConstant.getValue().matches("\\d{4}-\\d{2}-\\d{2}")) {
        stringConstant.setValueType(ValueType.DATE);
        return;
      }
    }
    if (coercionType == ValueType.TIME) {
      if (stringConstant.getValue().matches("\\d{2}:\\d{2}(:\\d{2}(\\.\\d{1,6})?)?")) {
        stringConstant.setValueType(ValueType.TIME);
        return;
      }
    }
  }

  protected boolean isConstantString(NodeBase lhs) {
    return lhs.isConstant() && lhs.getValueType() == ValueType.STRING;
  }

  @Override
  public void enterNegateExpr(DialobRuleParser.NegateExprContext ctx) {
    builder = builder.unaryExprNode("neg", Span.of(ctx));
  }

  @Override
  public void exitNegateExpr(DialobRuleParser.NegateExprContext ctx) {
    List<ValueType> valueTypes = getLhsAndRhsValueTypes();
    try {
      if (valueTypes.size() != 1) {
        errorLogger.logError(CompilerErrorCode.ONLY_ONE_ARGUMENT_FOR_NEGATE, Span.of(ctx));
        return;
      }

      final ValueType valueType = valueTypes.get(0);

      if (valueType == null) {
        reportTypeAnalysisProblem(ctx);
        return;
      }

      if (!valueType.isNegateable()) {
        errorLogger.logError(CompilerErrorCode.CANNOT_NEGATE_TYPE, new Object[]{valueType}, Span.of(ctx));
      }
    } finally {
      pop();
    }
  }


  @Override
  public void enterInfixExpr(DialobRuleParser.InfixExprContext ctx) {
    builder = builder.infixExprNode(ctx.op.getText(), Span.of(ctx));
  }

  @Override
  public void exitInfixExpr(DialobRuleParser.InfixExprContext ctx) {
    final List<ValueType> valueTypes = getLhsAndRhsValueTypes();
    try {
      final String operator = ctx.op.getText();
      if (valueTypes.size() != 2) {
        errorLogger.logError(CompilerErrorCode.OPERATOR_REQUIRES_2_OPERANDS, new Object[]{operator}, Span.of(ctx));
        return;
      }
      ValueType lhs = valueTypes.get(0);
      ValueType rhs = valueTypes.get(1);

      String code = null;

      if (getLhs().isConstant() && operator.equals("-") && lhs == ValueType.STRING) {
        coerce((ConstExprNode) getLhs(), rhs);
        lhs = getLhs().getValueType();
      }

      if (getRhs().isConstant() && operator.equals("-") && rhs == ValueType.STRING) {
        coerce((ConstExprNode) getRhs(), lhs);
        rhs = getRhs().getValueType();
      }

      if (getLhs().isConstant() && operator.equals("+") && lhs == ValueType.STRING && (rhs == ValueType.PERIOD || rhs == ValueType.DURATION)) {
        coerce((ConstExprNode) getLhs(), ValueType.DATE);
        lhs = getLhs().getValueType();
      }

      if (lhs == null || rhs == null) {
        reportTypeAnalysisProblem(ctx);
        return;
      }

      ValueType resultType;
      switch (operator) {
        case "+":
          resultType = lhs.plusType(rhs);
          code = CompilerErrorCode.CANNOT_ADD_TYPES;
          break;
        case "-":
          resultType = lhs.minusType(rhs);
          code = CompilerErrorCode.CANNOT_SUBTRACT_TYPES;
          break;
        case "*":
          resultType = lhs.multiplyType(rhs);
          code = CompilerErrorCode.CANNOT_MULTIPLY_TYPES;
          break;
        case "/":
          resultType = lhs.divideByType(rhs);
          code = CompilerErrorCode.CANNOT_DIVIDE_TYPES;
          break;
        default:
          resultType = null;
          break;
      }
      if (resultType == null) {
        errorLogger.logError(code, new Object[]{lhs, rhs}, Span.of(ctx));
      } else {
        builder.setValueType(resultType);
      }
    } finally {
      pop();
    }
  }

  private void reportTypeAnalysisProblem(ParserRuleContext ctx) {
    if (variableFinder != DUMMY_VARIABLE_FINDER) {
      errorLogger.logError(CompilerErrorCode.COULD_NOT_DEDUCE_TYPE, Span.of(ctx));
    }
  }

  @Override
  public void enterCallExpr(DialobRuleParser.CallExprContext ctx) {
    final String function = ctx.func.getText();
    if (variableFinder.isAsync(function) ) {
      builder = new ASTBuilder(builder);
    }
    builder = builder.callExprNode(function, Span.of(ctx));
  }

  @Override
  public void exitCallExpr(DialobRuleParser.CallExprContext ctx) {
    final String function = ctx.func.getText();
    try {
      final List<ValueType> valueTypes = getLhsAndRhsValueTypes();
      final ValueType returnValueType = variableFinder.returnTypeOf(function, valueTypes.toArray(new ValueType[0]));
      if (returnValueType != null) {
        builder.setValueType(returnValueType);
      }
      if (variableFinder.isAsync(function) ) {
        String replacementVariable = getOrCreateAsyncFunctionVariable(function, builder);
        builder = builder.getParentScopeBuilder();
        builder = builder.idExprNode(namespace, replacementVariable, returnValueType, Span.of(ctx));
      }
    } catch (VariableNotDefinedException e) {
      errorLogger.logError(CompilerErrorCode.UNKNOWN_FUNCTION, new Object[]{function}, Span.of(ctx));
    }
    pop();
  }

  @Override
  public void enterOfExpr(DialobRuleParser.OfExprContext ctx) {
    String reducer = ctx.left.getText();
    String op = reducer + "Of";
    builder = builder
      .reducerExprNode(op, Span.of(ctx));
  }

  @Override
  public void exitOfExpr(final DialobRuleParser.OfExprContext ctx) {
    final String oper = getBuilder().getTopNode().getNodeOperator().getOperator();
    final NodeBase lhs = getLhs();
    if (lhs == null || !lhs.isIdentifier()) {
      errorLogger.logError(CompilerErrorCode.REDUCER_TARGET_MUST_BE_REFERENCE, new Object[]{oper}, Span.of(ctx));
    }
    if (variableFinder.getScope() != null) {
      errorLogger.logError(CompilerErrorCode.CANNOT_USE_REDUCER_INSIDE_SCOPE, new Object[]{}, Span.of(ctx));
    } else if (!ParserUtil.isReducerOperator(oper)) {
      errorLogger.logError(CompilerErrorCode.UNKNOWN_REDUCER_OPERATOR, new Object[]{oper}, Span.of(ctx));
    } else {
      try {
        if (lhs != null) {
          ValueType valueType = variableFinder.returnTypeOf(oper, lhs.getValueType());
          if (valueType != null) {
            builder.setValueType(valueType);
          }
        }
      } catch (VariableNotDefinedException e) {
        errorLogger.logError(CompilerErrorCode.UNKNOWN_REDUCER_OPERATOR, new Object[]{oper}, Span.of(ctx));
      }
    }
    pop();
  }

  private String getOrCreateAsyncFunctionVariable(String function, ASTBuilder builder) {
    NodeBase asyncFunctionAst = builder.getTopNode();
    return asyncFunctionVariables.computeIfAbsent(asyncFunctionAst, nodeBase -> {
      String prefix = "$$" + function;
      int index = 0;
      String name;
      do {
        index++;
        name = prefix + "_" + index;
      } while (asyncFunctionVariables.containsValue(name));
      return name;
    });
  }


  @Override
  public void enterIdExprRule(DialobRuleParser.IdExprRuleContext ctx) {
    final Token var = ctx.var;
    final String id = var.getText();
    final String variableId = variableFinder.mapAlias(id);
    try {
      final ValueType valueType = variableFinder.typeOf(variableId);
      builder = variableFinder.findVariableScope(variableId)
        .map(scopeId -> builder.idExprNode(namespace, scopeId, variableId, valueType, Span.of(var)))
        .orElseGet(() -> builder.idExprNode(namespace, variableId, valueType, Span.of(var)));
    } catch (VariableNotDefinedException e) {
      errorLogger.logError(CompilerErrorCode.UNKNOWN_VARIABLE, new Object[]{e.getMessage()}, Span.of(var));
      builder.idExprNode(namespace, variableId, null, Span.of(var));
    }
  }

  @Override
  public void exitIdExprRule(DialobRuleParser.IdExprRuleContext ctx) {
    pop();
  }

  @Override
  public void enterConstExprRule(DialobRuleParser.ConstExprRuleContext ctx) {
    String text = ctx.value.getText();
    if (ctx.type == ValueType.STRING
      && isQuoted(text)) {
      text = text.substring(1, text.length() - 1);
    }
    builder = builder.constExprNode(text, ctx.unit != null ? ctx.unit.getText() : null, ctx.type, Span.of(ctx));
  }

  protected boolean isQuoted(@NotNull String text) {
    return text.startsWith("'") && text.endsWith("'")
     || text.startsWith("\"") && text.endsWith("\"");
  }

  @Override
  public void exitConstExprRule(DialobRuleParser.ConstExprRuleContext ctx) {
    pop();
  }

  @Override
  public void visitErrorNode(ErrorNode errorNode) {
    errorLogger.logError(CompilerErrorCode.COMPILER_ERROR, Span.of(errorNode.getSymbol()));
  }

  @Override
  public void enterIsExpr(DialobRuleParser.IsExprContext ctx) {
    String status = ctx.status.getText();
    String op;
    if (ctx.not == null) {
      op = "is-" + status;
    } else {
      op = "is-not-" + status;
    }
    op = CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, op);
    final String variableId = variableFinder.mapAlias(ctx.questionId.getText());
    ValueType valueType = null;
    try {
      valueType = variableFinder.typeOf(variableId);
    } catch (VariableNotDefinedException e) {
      errorLogger.logError(CompilerErrorCode.UNKNOWN_VARIABLE, new Object[]{e.getMessage()}, Span.of(ctx.questionId));
    }
    if (valueType != ValueType.STRING && "blank".equalsIgnoreCase(status)) {
      errorLogger.logError(CompilerErrorCode.STRING_VALUE_EXPECTED, new Object[0], Span.of(ctx.questionId));
    }
    builder = builder
      .callExprNode(op, ValueType.BOOLEAN, Span.of(ctx))
      .idExprNode(namespace, variableId, valueType, Span.of(ctx.questionId)).closeExpr();
  }

  @Override
  public void exitIsExpr(DialobRuleParser.IsExprContext ctx) {
    pop();
  }

  @Override
  public String toString() {
    return builder.toString();
  }

}
