package io.resys.thena.matching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.immutables.value.Value;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;

import io.vertx.core.json.JsonObject;

// Quick and dirty matcher: for every visited object, JSON-serializes it (so Optional/@JsonIgnore/
// @JsonProperty are honored the same way as everywhere else in this codebase) and records the
// top-level fields that satisfy any configured MatchExpression - no per-entity-type wiring needed.
// Everything - property name, property value, match pattern - is trimmed and compared as text,
// like a plain text search, rather than trying to preserve original Java types.
@RequiredArgsConstructor
public class MatchAnyProperty implements Consumer<Object> {

  private final List<Class<?>> ignoreTypes;
  private final List<Class<?>> onlyTypes;
  private final List<MatchExpression> expressions;

  private final Map<Class<?>, List<MatchedObject>> result = new HashMap<>();

  @Override
  public void accept(Object entity) {
    if (entity == null || isIgnored(entity)) {
      return;
    }

    final JsonObject json;
    try {
      json = JsonObject.mapFrom(entity);
    } catch (Exception e) {
      return; // not JSON-serializable - skip rather than blow up a batch scan
    }

    final var matchedProps = new ArrayList<MatchedProperty>();
    for (final var propertyName : json.fieldNames()) {
      final var value = json.getValue(propertyName);
      if (matchesAnyExpression(propertyName, value)) {
        matchedProps.add(ImmutableMatchedProperty.builder()
          .propertyName(propertyName)
          .matchedValue(value)
          .build());
      }
    }

    if (!matchedProps.isEmpty()) {
      result.computeIfAbsent(entity.getClass(), key -> new ArrayList<>())
        .add(ImmutableMatchedObject.builder()
          .entity(entity)
          .addAllProps(matchedProps)
          .build());
    }
  }

  private boolean isIgnored(Object entity) {
    if (!onlyTypes.isEmpty() && onlyTypes.stream().noneMatch(type -> type.isInstance(entity))) {
      return true;
    }
    return ignoreTypes.stream().anyMatch(type -> type.isInstance(entity));
  }

  // No expressions configured -> quick-and-dirty default: record every non-null property.
  private boolean matchesAnyExpression(String propertyName, Object value) {
    if (expressions.isEmpty()) {
      return value != null;
    }
    final var propertyNameText = text(propertyName);
    for (final var expr : expressions) {
      if (expr.propertyNamePattern() != null && !expr.propertyNamePattern().matcher(propertyNameText).matches()) {
        continue;
      }
      if (matchesOp(expr.op(), value, expr.value())) {
        return true;
      }
    }
    return false;
  }

  private boolean matchesOp(MatchOp op, Object actual, Object expected) {
    if (actual == null || expected == null) {
      return false;
    }
    final var actualText = text(actual);
    final var expectedText = text(expected);
    return switch (op) {
      case EQUALS -> actualText.equalsIgnoreCase(expectedText);
      case LIKE -> likeToPattern(expectedText).matcher(actualText).matches();
    };
  }

  private static String text(Object value) {
    return String.valueOf(value).trim();
  }

  // Translates a SQL-LIKE pattern (% = any sequence, _ = any single char) into a case-insensitive regex.
  private static Pattern likeToPattern(String like) {
    final var regex = new StringBuilder();
    for (final var ch : text(like).toCharArray()) {
      switch (ch) {
        case '%' -> regex.append(".*");
        case '_' -> regex.append(".");
        default -> {
          if ("\\.^$|?*+()[]{}".indexOf(ch) >= 0) {
            regex.append('\\');
          }
          regex.append(ch);
        }
      }
    }
    return Pattern.compile(regex.toString(), Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
  }

  public List<MatchedObject> close() {
    return result.values().stream().flatMap(e -> e.stream()).toList();
  }

  // Quick and dirty YAML-ish rows, one line per list entry - not escaped, just readable at a glance.
  public List<String> prettyPrint() {
    final var rows = new ArrayList<String>();
    for (final var matched : close()) {
      rows.add("- entity: " + matched.getEntity().getClass().getSimpleName());
      rows.add("  matches:");
      for (final var prop : matched.getProps()) {
        rows.add("  - " + prop.getPropertyName() + ": " + prop.getMatchedValue());
      }
    }
    return rows;
  }

  @Value.Immutable
  public interface MatchedObject {
    Object getEntity();
    List<MatchedProperty> getProps();
  }

  @Value.Immutable
  public interface MatchedProperty {
    String getPropertyName();
    @Nullable Object getMatchedValue();
  }

  public enum MatchOp {
    EQUALS,
    LIKE
  }

  // propertyNamePattern == null means "any property name". A pattern with no % / _ degrades to an exact match.
  public record MatchExpression(@Nullable Pattern propertyNamePattern, MatchOp op, Object value) {}

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private final List<Class<?>> ignoreTypes = new ArrayList<>();
    private final List<Class<?>> onlyTypes = new ArrayList<>();
    private final List<MatchExpression> expressions = new ArrayList<>();

    public Builder ignoreType(Class<?> type) {
      this.ignoreTypes.add(type);
      return this;
    }

    public Builder onlyType(Class<?> type) {
      this.onlyTypes.add(type);
      return this;
    }

    public Builder wherePropertyEquals(String propertyNameLike, Object value) {
      this.expressions.add(new MatchExpression(likeToPattern(propertyNameLike), MatchOp.EQUALS, value));
      return this;
    }

    public Builder wherePropertyLike(String propertyNameLike, Object valueLike) {
      this.expressions.add(new MatchExpression(likeToPattern(propertyNameLike), MatchOp.LIKE, valueLike));
      return this;
    }

    public Builder whereAnyPropertyEquals(Object value) {
      this.expressions.add(new MatchExpression(null, MatchOp.EQUALS, value));
      return this;
    }

    public Builder whereAnyPropertyLike(Object valueLike) {
      this.expressions.add(new MatchExpression(null, MatchOp.LIKE, valueLike));
      return this;
    }

    public MatchAnyProperty build() {
      return new MatchAnyProperty(List.copyOf(ignoreTypes), List.copyOf(onlyTypes), List.copyOf(expressions));
    }
  }
}
