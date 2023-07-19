package io.dialob.rule.parser.node;

import com.google.common.collect.Maps;
import io.dialob.rule.parser.DialobRuleLexer;
import io.dialob.rule.parser.DialobRuleParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParseTestBase {
  protected void assertExpressionEquals(String expression, String expected) {
    assertEquals(expected, lispExpression(parseExpression(expression)));
  }

  protected String lispExpression(ParseTree parseTree) {
    ParseTreeWalker walker = new ParseTreeWalker();
    final StringBuilder expressionBuilder = new StringBuilder();
    ASTBuilderWalker builder = new ASTBuilderWalker(null, ASTBuilderWalker.DUMMY_VARIABLE_FINDER, Maps.newHashMap());
    walker.walk(builder, parseTree);
    NodeBase nodeBase = builder.getBuilder().build();
    if (nodeBase == null) {
      return "";
    }
    return nodeBase.toString();
  }

  protected ParseTree parseExpression(String expression) {
    DialobRuleLexer ffRuleLexer = new DialobRuleLexer(CharStreams.fromString(expression));
    DialobRuleParser ffRuleParser = new DialobRuleParser(new CommonTokenStream(ffRuleLexer));
    ffRuleParser.setBuildParseTree(true);
    return ffRuleParser.compileUnit();
  }

  protected NodeBase ast(String expression) {
    ParseTreeWalker walker = new ParseTreeWalker();
    final StringBuilder expressionBuilder = new StringBuilder();
    ASTBuilderWalker builder = new ASTBuilderWalker(null, ASTBuilderWalker.DUMMY_VARIABLE_FINDER, Maps.newHashMap());
    walker.walk(builder, parseExpression(expression));
    return builder.getBuilder().build();

  }

}
