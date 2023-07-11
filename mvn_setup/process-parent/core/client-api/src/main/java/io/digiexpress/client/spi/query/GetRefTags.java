package io.digiexpress.client.spi.query;

import java.util.HashMap;
import java.util.stream.Collectors;

import io.digiexpress.client.api.Client.ClientConfig;
import io.digiexpress.client.api.ClientEntity.ConfigType;
import io.digiexpress.client.api.ImmutableProjectDialobTags;
import io.digiexpress.client.api.ImmutableProjectHdesTags;
import io.digiexpress.client.api.ImmutableProjectStencilTags;
import io.digiexpress.client.api.ImmutableProjectTagName;
import io.digiexpress.client.api.ImmutableProjectTags;
import io.digiexpress.client.api.ProjectTags;
import io.digiexpress.client.api.ProjectTags.ProjectDialobTags;
import io.digiexpress.client.api.ProjectTags.ProjectHdesTags;
import io.digiexpress.client.api.ProjectTags.ProjectStencilTags;
import io.digiexpress.client.api.ProjectTags.ProjectTagName;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetRefTags {
  private final ClientConfig config;

  public Uni<ProjectTags> build() {
    return Uni.combine().all().unis(getStencilTags(), getHdesTags(), getDialogTags())
        .asTuple().onItem().transform(tuple3 -> ImmutableProjectTags.builder()
            .dialob(tuple3.getItem3())
            .hdes(tuple3.getItem2())
            .stencil(tuple3.getItem1())
            .build());
  }

  public Uni<ProjectStencilTags> getStencilTags() {
    return config.getStencil().getStore().query().head()
      .onItem().transform(state -> {
        final var byId = state.getReleases().values().stream()
          .map(rel -> ImmutableProjectTagName.builder()
            .created(rel.getBody().getCreated())
            .id(rel.getId())
            .value(rel.getBody().getName())
            .build())
          .collect(Collectors.toMap(rel -> rel.getId(), rel -> rel));
        return ImmutableProjectStencilTags.builder()
            .byId(byId)
            .type(ConfigType.STENCIL)
            .build();
      });
  }

  public Uni<ProjectHdesTags> getHdesTags() {
    return config.getHdes().store().query().get()
    .onItem().transform(state -> {
      final var byId = state.getTags().values().stream().map(rel -> {
        final var ast = config.getHdes().ast().commands(rel.getBody()).tag();
        return ImmutableProjectTagName.builder()
          .created(ast.getCreated())
          .id(rel.getId())
          .value(ast.getName())
          .build();
      })
      .collect(Collectors.toMap(rel -> rel.getId(), rel -> rel));
  
      return ImmutableProjectHdesTags.builder()
          .byId(byId)
          .type(ConfigType.HDES)
          .build();
    });
  }

  public Uni<ProjectDialobTags> getDialogTags() {
    return config.getDialob().getConfig().getStore().query().get()
    .onItem().transform(state -> {
      final var byId = new HashMap<String, ProjectTagName>();
      for(final var entity : state.getRevs().values()) {
        final var rev = config.getDialob().getConfig().getMapper().toFormRevDoc(entity);
        
        rev.getEntries().stream()
          .map(e -> ImmutableProjectTagName.builder()
              .created(e.getCreated())
              .id(e.getFormId())
              .value(e.getRevisionName())
              .parentId(rev.getId())
              .build())
          .forEach(e -> byId.put(e.getId(), e));
      }
      return ImmutableProjectDialobTags.builder()
          .byId(byId)
          .type(ConfigType.DIALOB)
          .build();
    });
  }

}
