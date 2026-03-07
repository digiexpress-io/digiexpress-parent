package io.resys.limaone.spi.ast;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.hash.Hashing;

import io.resys.limaone.ast.AST_Parser;
import io.resys.limaone.ast.AST_Parser.Dependency_AST;
import io.resys.limaone.ast.AST_Parser.FlowParser;
import io.resys.limaone.ast.Flow_AST;
import io.resys.limaone.spi.LocalCache;
import io.resys.limaone.spi.LocalCache.Flow_AST_CacheKey;
import io.resys.limaone.spi.ast.AST_ParserImpl.AST_ParserProps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RequiredArgsConstructor
@Slf4j
public class FlowParserImpl implements AST_Parser.FlowParser {

  private final AST_ParserProps props;
  private final List<String> src = new ArrayList<>();
  private Consumer<Dependency_AST> dependency;
  
  
  @Override
  public FlowParserImpl syntax(String src) {
    if (src == null) {
      return this;
    }
    this.src.add(src);
    return this;
  }
  @Override
  public FlowParser onDependency(Consumer<Dependency_AST> dependency) {
    this.dependency = Objects.requireNonNull(dependency, () -> "dependency must be defined!");
    return this;
  }
  @Override
  public Flow_AST parse() {
    
    final var joined = String.join(System.lineSeparator(), this.src);
    final var hash = Hashing.murmur3_128().hashString(joined, StandardCharsets.UTF_8).toString();
    final var cacheKey = new Flow_AST_CacheKey(hash);
    
    final Function<Flow_AST_CacheKey, Flow_AST> mappingFunction = (k) -> new FlowParserVisitor(props, joined, hash).accept();
    final var ast = LocalCache.computeIfAbsent(cacheKey, mappingFunction);
    
    if(dependency != null) {
      ast.getDependencies().forEach(dependency);
    }
    
    return ast;
  }
}
