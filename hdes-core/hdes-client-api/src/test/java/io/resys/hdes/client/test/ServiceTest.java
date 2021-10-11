package io.resys.hdes.client.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.immutables.value.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.resys.hdes.client.api.ast.AstCommand;
import io.resys.hdes.client.api.ast.AstCommand.AstCommandValue;
import io.resys.hdes.client.api.ast.ImmutableAstCommand;
import io.resys.hdes.client.api.programs.ServiceData;
import io.resys.hdes.client.api.programs.ServiceProgram.ServiceResult;
import io.resys.hdes.client.spi.util.FileUtils;

public class ServiceTest {

  @Test
  public void type0() throws IOException {
    final var ast = TestUtils.client.ast()
        .commands(getCommands("service/Type1Service.txt"))
        .service();

    final var service = TestUtils.client.program().ast(ast);
    
    // map conversion
    ServiceResult result = TestUtils.client.executor()
      .inputMap(Map.of("a", 5, "b", 10))
      .service(service)
      .andGetBody();
    
    Assertions.assertEquals("{\"sum\":15}", TestUtils.objectMapper.writeValueAsString(result.getValue()));
    
    // data object conversion
    result = TestUtils.client.executor()
        .inputEntity(ImmutableTestServiceInput.builder().a(5).b(10).build())
        .service(service)
        .andGetBody();
    // object to object conversion
    Assertions.assertEquals("{\"sum\":15}", TestUtils.objectMapper.writeValueAsString(result.getValue()));
  }
  
  
  @ServiceData
  @Value.Immutable
  public interface TestServiceInput {
    Integer getA();
    Integer getB();
  }
  
  public static List<AstCommand> getCommands(String context) throws IOException {
    List<AstCommand> result = new ArrayList<>();
    InputStream inputStream = FileUtils.toInputStream(ServiceTest.class, context);
    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
    String line;
    int index = 0;
    while ((line = br.readLine()) != null) {
      result.add(ImmutableAstCommand.builder()
          .id("" + index++)
          .value(line)
          .type(AstCommandValue.ADD)
          .build());
    }
    return result;
  }
}
