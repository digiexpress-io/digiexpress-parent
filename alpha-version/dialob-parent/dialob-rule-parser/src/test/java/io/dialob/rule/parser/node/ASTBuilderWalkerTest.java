package io.dialob.rule.parser.node;

import com.google.common.collect.Maps;
import io.dialob.rule.parser.DialobRuleParser;
import io.dialob.rule.parser.api.CompilerErrorCode;
import io.dialob.rule.parser.api.VariableFinder;
import io.dialob.rule.parser.api.VariableNotDefinedException;
import org.antlr.v4.runtime.Token;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class ASTBuilderWalkerTest {

  @Test
  public void shouldLogUnknownVariableAndIdWhenVariableIsNotFound() throws Exception {
    VariableFinder variableFinder = mock(VariableFinder.class);
    ErrorLogger errorLogger = mock(ErrorLogger.class);

    DialobRuleParser.IdExprRuleContext ctx = mock(DialobRuleParser.IdExprRuleContext.class);
    ctx.var = mock(Token.class);
    when(ctx.var.getText()).thenReturn("x");
    when(ctx.var.getStartIndex()).thenReturn(0);
    when(ctx.var.getStopIndex()).thenReturn(1);

    when(variableFinder.mapAlias("x")).thenReturn("x");
    when(variableFinder.typeOf("x")).thenThrow(new VariableNotDefinedException("x"));

    ASTBuilderWalker astBuilderWalker = new ASTBuilderWalker(variableFinder, Maps.newHashMap());
    astBuilderWalker.setErrorLogger(errorLogger);
    astBuilderWalker.enterIdExprRule(ctx);

    verify(variableFinder).mapAlias("x");
    verify(variableFinder).typeOf("x");
    verify(ctx.var, atLeastOnce()).getStartIndex();
    verify(ctx.var, atLeastOnce()).getStopIndex();
    verify(ctx.var).getText();
    verify(errorLogger).logError(CompilerErrorCode.UNKNOWN_VARIABLE, new Object[]{"x"}, Span.of(0, 1));

    verifyNoMoreInteractions(variableFinder, ctx, ctx.var, errorLogger);
  }

}
