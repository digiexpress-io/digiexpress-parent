package io.dialob.rule.parser.node;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.immutables.value.Value;

import java.io.Serializable;

@Value.Immutable
public interface Span extends Serializable {

  Span UNDEFINED_SPAN = ImmutableSpan.of(-1, -1);

  @Value.Parameter
  int getStartIndex();

  @Value.Parameter
  int getStopIndex();

  static Span of(Token token) {
    return Span.of(token.getStartIndex(), token.getStopIndex());
  }

  static Span of(int startIndex, int stopIndex) {
    assert startIndex >= 0;
    assert startIndex <= stopIndex;
    return ImmutableSpan.of(startIndex, stopIndex);
  }

  static Span undefined() {
    return UNDEFINED_SPAN;
  }

  static Span of(ParserRuleContext ctx) {
    return of(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
  }
}
