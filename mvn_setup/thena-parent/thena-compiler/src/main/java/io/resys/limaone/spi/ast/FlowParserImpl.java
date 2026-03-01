package io.resys.limaone.spi.ast;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import com.google.common.hash.Hashing;

import io.resys.limaone.ast.AST_Parser;
import io.resys.limaone.ast.AST_Parser.FlowParser;
import io.resys.limaone.ast.Flow_AST;
import io.resys.limaone.ast.Flow_CST.Yaml;
import io.resys.limaone.ast.ImmutableFlow_AST;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.spi.LocalCache;
import io.resys.limaone.spi.LocalCache.Flow_AST_CacheKey;
import io.resys.limaone.spi.ast.AST_ParserImpl.AST_ParserProps;
import io.resys.limaone.spi.ast.flow.YamlMapper;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class FlowParserImpl implements AST_Parser.FlowParser {

  private final AST_ParserProps props;
  private final List<ModelError> messages = new ArrayList<>();
  private final List<String> src = new ArrayList<>();
  private String id;
  
  @Override
  public FlowParserImpl syntax(String src) {
    if (src == null) {
      return this;
    }
    this.src.add(src);
    return this;
  }
  @Override
  public FlowParser id(String id) {
    this.id = id;
    return this;
  }
  @Override
  public Flow_AST parse() {
    Objects.requireNonNull(id, () -> "id must be defined!");
    
    final var joined = String.join(System.lineSeparator(), this.src);
    final var hash = Hashing.murmur3_128().hashString(joined, StandardCharsets.UTF_8).toString();
    final var cacheKey = new Flow_AST_CacheKey(hash);
    final Function<Flow_AST_CacheKey, Flow_AST> mappingFunction = (k) -> {
      
      final var cst = new FlowParserCST(props).parseCST(joined);
      final Yaml id = cst.getItem1().getId();
      
      messages.addAll(cst.getItem2());      

      final var ast = ImmutableFlow_AST.builder();
      
      return ast
          .id(this.id)
          .bodyType(Model.BodyType.FLOW)
          .hash(hash)
          .errors(messages)
          .name(id == null ? "": id.getValue())
          .parseTree(cst.getItem1())
          .headers(YamlMapper.headers().build(cst.getItem1()))
          .build();
    };
    return LocalCache.computeIfAbsent(cacheKey, mappingFunction);

  }
}
