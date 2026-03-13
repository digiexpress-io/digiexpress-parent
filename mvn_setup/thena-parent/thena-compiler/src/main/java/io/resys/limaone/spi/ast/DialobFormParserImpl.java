package io.resys.limaone.spi.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import io.resys.limaone.ast.AST_Parser.DialobFormParser;
import io.resys.limaone.ast.DialobForm_AST;
import io.resys.limaone.ast.ImmutableDialobForm_AST;
import io.resys.limaone.ast.ImmutableHeaders_AST;
import io.resys.limaone.model.DialobForm;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.spi.LocalCache;
import io.resys.limaone.spi.LocalCache.DialobForm_AST_CacheKey;
import io.resys.limaone.spi.ast.AST_ParserImpl.AST_ParserProps;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
public class DialobFormParserImpl implements DialobFormParser {
  @SuppressWarnings("unused")
  private final AST_ParserProps props;
  private Model<DialobForm> model;
  
  @Override
  public DialobFormParserImpl model(Model<DialobForm> workflow) {
    this.model = Objects.requireNonNull(workflow, () -> "workflow must be defined");
    return this;
  }

  @Override
  public DialobForm_AST parse() {
    Objects.requireNonNull(model, () -> "workflow must be defined");

    final var hash = model.getBodyHash();
    final var cacheKey = new DialobForm_AST_CacheKey(hash);
    
    final Function<DialobForm_AST_CacheKey, DialobForm_AST> mappingFunction = (k) -> createAst(model);
    final var ast = LocalCache.computeIfAbsent(cacheKey, mappingFunction);
    
    return ast;
  }

  private DialobForm_AST createAst(Model<DialobForm> model) {
    final List<ModelError> errors = new ArrayList<>();
    return ImmutableDialobForm_AST.builder()
      .addAllErrors(errors)
      .bodyType(BodyType.DIALOB_FORM)
      .headers(ImmutableHeaders_AST.builder().build())
      .name(getFormDep(model.getBody().getFormName(), model.getBody().getFormTagName()))
      .hash(model.getBodyHash())
      .form(model.getBody().getForm())
      .build();
  }

  public static String getFormDep(String formName, String tagName) {
    return formName + "/" + tagName;
  }
}
