package io.digiexpress.client.spi.query;

import io.digiexpress.client.api.Client.ClientConfig;
import io.digiexpress.client.api.ClientQuery.FlowDocument;
import io.digiexpress.client.api.ImmutableFlowDocument;
import io.digiexpress.client.spi.support.MainBranch;
import io.digiexpress.client.spi.support.ServiceAssert;
import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetHdesFlow {
  private final ClientConfig config;
  private final String tagName;
  private final String flowId;
  
  public Uni<FlowDocument> build() {
    final var isMain = MainBranch.isMain(tagName);
    if(isMain) {
      return config.getHdes().store().query().get(flowId)
      .onItem().transform(e -> (FlowDocument) ImmutableFlowDocument.builder().id(e.getId()).data(config.getHdes().ast().commands(e.getBody()).flow()).build())
      .onFailure().recoverWithUni((_error) -> 
        
        config.getHdes().store().query().get()
        .onItem().transform(state -> {
          
          final var flow = state.getFlows().values().stream()
              .filter(e -> e.getBodyType().equals(AstBodyType.FLOW))
              .map(e -> ImmutableFlowDocument.builder().data(config.getHdes().ast().commands(e.getBody()).flow()).id(e.getId()).build())
              .filter(e -> e.getData().getName().equals(flowId) || e.getId().equals(flowId))
              .findFirst();
          
          ServiceAssert.isTrue(flow.isPresent(), () -> "Can't find hdes flow by id or name: '" + flowId + "' for tag: '" + tagName + "'");
          
          return flow.get();
        })
      );
    }
    return config.getHdes().store().query().get()
    .onItem().transform(state -> {
      
      final var findById = state
        .getTags().values().stream()
        .filter(tag -> tag.getId().equals(tagName)).map(e -> config.getHdes().ast().commands(e.getBody()).tag())
        .findFirst();
      
      final var foundTag = findById.orElseGet(() -> state.getTags().values().stream()
        .map(e -> config.getHdes().ast().commands(e.getBody()).tag())
        .filter(e -> e.getName().equals(tagName))
        .findFirst().orElse(null)
      ); 
      
      ServiceAssert.notNull(foundTag, () -> "Can't find hdes tag with id or name: '" + tagName + "'");
      
      final var flow = foundTag.getValues().stream()
        .filter(e -> e.getBodyType().equals(AstBodyType.FLOW))
        .map(e -> ImmutableFlowDocument.builder().data(config.getHdes().ast().commands(e.getCommands()).flow()).id(e.getId()).build())
        .filter(e -> e.getData().getName().equals(flowId) || e.getId().equals(flowId))
        .findFirst();
      
      ServiceAssert.isTrue(flow.isPresent(), () -> "Can't find hdes flow by id or name: '" + flowId + "' for tag: '" + tagName + "'");
      return flow.get();
    });
  }
}
