/*
 * Copyright © 2015 - 2021 ReSys (info@dialob.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dialob.rule.parser;

import com.google.common.collect.Maps;
import io.dialob.rule.parser.api.CompilerErrorCode;
import io.dialob.rule.parser.api.ImmutableRuleExpressionCompilerError;
import io.dialob.rule.parser.api.RuleExpressionCompilerError;
import io.dialob.rule.parser.api.VariableFinder;
import io.dialob.rule.parser.node.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Expression implements ErrorLogger {

  private final String expression;

  private NodeBase ast;

  private List<RuleExpressionCompilerError> errors = new ArrayList<>();

  private Expression(@NotNull String expression) {
    this(ASTBuilderWalker.DUMMY_VARIABLE_FINDER, Maps.newHashMap(), expression);
  }

  private Expression(@NotNull VariableFinder variableFinder, Map<NodeBase, String> asyncFunctionVariables, @NotNull String expression) {
    this.expression = Objects.requireNonNull(expression, "expression may not be null");
    ParseTree parseTree = parseExpression(expression);
    if (!hasErrors()) {
      ParseTreeWalker walker = new ParseTreeWalker();
      ASTBuilderWalker astBuilder = new ASTBuilderWalker(variableFinder, asyncFunctionVariables);
      astBuilder.setErrorLogger(this);
      walker.walk(astBuilder, parseTree);
      ast = astBuilder.getBuilder().build();
    }
  }

  @NotNull
  public static Expression createExpression(@NotNull String expression) {
    return new Expression(expression);
  }

  @NotNull
  public static Expression createExpression(VariableFinder variableFinder, Map<NodeBase, String> asyncFunctionVariables, @NotNull String expression) {
    return new Expression(variableFinder, asyncFunctionVariables, expression);
  }

  @NotNull
  private ParseTree parseExpression(@NotNull String expression) {
    DialobRuleLexer dialobRuleLexer = new DialobRuleLexer(CharStreams.fromString(expression));
    final ErrorListener errorListener = new ErrorListener(this);
    dialobRuleLexer.addErrorListener(errorListener);
    final DialobRuleParser dialobRuleParser = new DialobRuleParser(new CommonTokenStream(dialobRuleLexer));
    dialobRuleParser.addErrorListener(errorListener);
    dialobRuleParser.setBuildParseTree(true);
    return dialobRuleParser.compileUnit();
  }

  public List<RuleExpressionCompilerError> getErrors() {
    return Collections.unmodifiableList(errors);
  }

  public boolean hasErrors() {
    return !errors.isEmpty();
  }

  @Override
  public String toString() {
    return expression;
  }

  @Override
  public void logError(String errorCode, Span span) {
    errors.add(ImmutableRuleExpressionCompilerError.builder().errorCode(errorCode).span(span).build());
  }

  @Override
  public void logError(String errorCode, Object[] args, Span span) {
    errors.add(ImmutableRuleExpressionCompilerError.builder().errorCode(errorCode).args(args).span(span).build());
  }

  static class StringOper implements Comparable<StringOper> {
    final Span span;

    StringOper(Span span) {
      this.span = span;
    }


    @Override
    public int compareTo(@NotNull StringOper o) {
      // We want reverse order
      return o.span.getStartIndex() - span.getStartIndex();
    }
  }

  public Set<String> getAllIds() {
    final Set<String> ids = new HashSet<>();
    ast.accept(new ASTVisitor() {
      @Override
      @NotNull
      public NodeBase visitIdExpr(@NotNull IdExprNode node) {
        ids.add(node.getId());
        return node;
      }
    });
    return ids;
  }

  public Expression renameId(final String from, final String to) {
    List<StringOper> opes = new ArrayList<>();
    ast.accept(new ASTVisitor() {
      @Override
      @NotNull
      public NodeBase visitIdExpr(@NotNull IdExprNode node) {
        if (node.getId().equals(from)) {
          opes.add(new StringOper(node.getSpan()));
        }
        return node;
      }
    });
    Collections.sort(opes);
    String updatedExpression = expression;
    for (StringOper op : opes) {
      updatedExpression = updatedExpression.substring(0, op.span.getStartIndex()) + to + updatedExpression.substring(op.span.getStopIndex() + 1);
    }
    return createExpression(updatedExpression);
  }

  public void accept(ASTVisitor visitor) {
    ast = ast.accept(visitor);
  }

  public NodeBase getAst() {
    return ast;
  }

  private static class ErrorListener implements ANTLRErrorListener {

    private final ErrorLogger errorLogger;

    private ErrorListener(ErrorLogger errorLogger) {
      this.errorLogger = errorLogger;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object o, int row, int column, String s, RecognitionException e) {
      errorLogger.logError(CompilerErrorCode.SYNTAX_ERROR, Span.of(column,column));
    }

    @Override
    public void reportAmbiguity(Parser parser, DFA dfa, int row, int column, boolean b, BitSet bitSet, ATNConfigSet atnConfigSet) {
      errorLogger.logError(CompilerErrorCode.AMBIGUOUS_INPUT, Span.of(column,column));
    }

    @Override
    public void reportAttemptingFullContext(Parser parser, DFA dfa, int row, int column, BitSet bitSet, ATNConfigSet atnConfigSet) {
      errorLogger.logError(CompilerErrorCode.FULL_CONTEXT_ERROR, Span.of(column,column));
    }

    @Override
    public void reportContextSensitivity(Parser parser, DFA dfa, int i, int row, int column, ATNConfigSet atnConfigSet) {
      errorLogger.logError(CompilerErrorCode.CONTEXT_SENSITIVE_ERROR, Span.of(column,column));
    }
  }
}
