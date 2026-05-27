package io.resys.limaone.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.resys.limaone.ast.DecisionTable_AST;
import io.resys.limaone.ast.DecisionTable_CST.YamlDecision;
import io.resys.limaone.model.DecisionTable.DecisionStatement;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.spi.ast.AST_ParserImpl;
import io.resys.limaone.spi.ast.AST_ParserImpl.AST_ParserProps;
import io.resys.limaone.spi.ast.CST_YamlParser;
import io.resys.limaone.spi.ast.decisiontable.DecisionCSTToCommands;
import io.resys.limaone.spi.ast.decisiontable.MutableYamlDecision;
import io.resys.limaone.spi.compiler.CompilerImpl;
import io.resys.limaone.spi.runtime.DefaultEnvironmentProperties;
import io.resys.limaone.spi.runtime.DefaultEnvironmentProperties.ModelDbConfig;
import io.smallrye.mutiny.tuples.Tuple2;

public class DecisionCST_3_Test {
  private final AST_ParserProps props = AST_ParserImpl.builder().props();
  private static final CompilerImpl compiler = new CompilerImpl(DefaultEnvironmentProperties.builder()
      .dbConfig(ModelDbConfig.external(() -> { throw new RuntimeException(); }))
      .defaultTenantName("in-memory")
      .build());

  @Test
  public void testBasicCSTToCommands() {
    final String yaml = """
        name: Risk Assessment
        description: Customer risk evaluation
        hitPolicy: FIRST
        valueSets:
          riskLevel: low, medium, high
        table: |
          | age:INTEGER    | income: INTEGER | -> | riskLevel:STRING   |
          |----------------|-----------------|----|--------------------|
          | < 25           | < 30000         |    | high               |
          | >= 25          | >= 50000        |    | low                |
        """;

    final var parser = new CST_YamlParser<MutableYamlDecision>(props, new MutableYamlDecision());
    final Tuple2<MutableYamlDecision, List<ModelError>> result = parser.parseCST(yaml);
    final YamlDecision parseTree = result.getItem1();
    
    assertTrue(result.getItem2().isEmpty(), "Should have no parsing errors");
    
    final List<DecisionStatement> commands = new DecisionCSTToCommands().convert(parseTree);
    DecisionTable_AST ast = compiler.getParser().parseDecisionTable().nodes(commands).parse();
    assertTrue(ast != null, "Failed to parse decision table");    
  }


}
