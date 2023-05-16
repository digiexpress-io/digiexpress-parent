package io.digiexpress.client.spi.query;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.digiexpress.client.api.Client.ClientConfig;
import io.digiexpress.client.spi.support.MainBranch;
import io.digiexpress.client.spi.support.ServiceAssert;
import io.resys.hdes.client.api.HdesStore.StoreState;
import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.hdes.client.api.ast.AstTag;
import io.resys.hdes.client.api.ast.AstTag.AstTagValue;
import io.resys.hdes.client.api.ast.ImmutableAstTag;
import io.resys.hdes.client.api.ast.ImmutableAstTagValue;
import io.resys.hdes.client.api.ast.ImmutableHeaders;
import io.smallrye.mutiny.Uni;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetHdesTag {
  private final List<AstBodyType> HDES_ASSETS = Arrays.asList(AstBodyType.DT, AstBodyType.FLOW, AstBodyType.FLOW_TASK);
  
  private final ClientConfig config;
  private final String tagName;
  
  public Uni<AstTag> build() {
    final var isMain = MainBranch.isMain(tagName);
    return config.getHdes().store().query().get().onItem().transform(state -> isMain ? getMain(state) : getRelease(state));  
  }

  private AstTag getRelease(StoreState state) {
    final var foundTag = state.getTags().values().stream()
    .map(e -> AstTagDoc.builder().id(e.getId()).data(config.getHdes().ast().commands(e.getBody()).tag()).build())
    .filter(e -> e.getData().getName().equals(tagName) || e.getId().equals(tagName))
    .map(e -> ImmutableAstTag.builder()
        .from(e.getData())
        .values(
            e.getData().getValues().stream()
            .filter(v -> HDES_ASSETS.contains(v.getBodyType()))
            .collect(Collectors.toList())
        )
        .build())
    .findFirst();

    ServiceAssert.notNull(foundTag, () -> "Can't find hdes tag with id or name: '" + tagName + "'");
    return foundTag.get(); 
  }
  
  private AstTag getMain(StoreState state) {
    final List<AstTagValue> values = new ArrayList<>();
    Stream.of(
        state.getFlows().values().stream(),
        state.getDecisions().values().stream(),
        state.getServices().values().stream()
    )
    .flatMap(e -> e)
    .forEach(entity -> {
      final var tag = ImmutableAstTagValue.builder()
          .id(entity.getId())
          .hash(entity.getHash())
          .bodyType(entity.getBodyType())
          .commands(entity.getBody())
          .build(); 
      values.add(tag);
    });
    
    return ImmutableAstTag.builder()
        .created(LocalDateTime.now())
        .bodyType(AstBodyType.TAG)
        .name(tagName).description("snapshot")
        .values(values)
        .headers(ImmutableHeaders.builder().build())
        .build();
  }
  
  @Builder @Data
  private static class AstTagDoc {
    private final String id;
    private final AstTag data;
  }
}
