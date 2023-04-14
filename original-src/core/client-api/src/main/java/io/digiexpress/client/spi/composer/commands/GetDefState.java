package io.digiexpress.client.spi.composer.commands;

import java.util.stream.Collectors;

import io.digiexpress.client.api.Client;
import io.digiexpress.client.api.ClientEntity.ServiceDefinition;
import io.digiexpress.client.api.ComposerCache;
import io.digiexpress.client.api.ComposerEntity.DefinitionState;
import io.digiexpress.client.api.ComposerEntity.DialobTree;
import io.digiexpress.client.api.ComposerEntity.HdesTree;
import io.digiexpress.client.api.ComposerEntity.StencilTree;
import io.digiexpress.client.api.ImmutableDefinitionState;
import io.digiexpress.client.api.ImmutableDialobTree;
import io.digiexpress.client.api.ImmutableHdesTree;
import io.digiexpress.client.api.ImmutableStencilTree;
import io.resys.hdes.client.api.ImmutableComposerState;
import io.resys.hdes.client.api.ImmutableStoreEntity;
import io.resys.hdes.client.api.ast.AstTag;
import io.resys.hdes.client.spi.composer.ComposerEntityMapper;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetDefState {
  
  private final Client client;
  private final ComposerCache cache;
  private final String definitionId;
  
  public Uni<DefinitionState> build() {
    return client.getQuery().getProjectServiceDef(definitionId).onItem().transformToUni(this::defState);
  }

  private Uni<DefinitionState> defState(ServiceDefinition definition) {
    final var cached = cache.getDefinitionState(definition.getId());
    if(cached.isPresent()) {
      return Uni.createFrom().item(cached.get());
    }
    
    final var dialob = dialobState(definition);
    final var hdes = hdesState(definition);
    final var stencil = stencilState(definition);
    return Uni.combine().all().unis(dialob, hdes, stencil).asTuple()
      .onItem().transform(tuple -> {
        final var defState = ImmutableDefinitionState.builder()
          .definition(definition)
          .dialob(tuple.getItem1())
          .hdes(tuple.getItem2())
          .stencil(tuple.getItem3())
          .build();
        cache.save(defState);
        return defState;
      });
  }
  
  private Uni<StencilTree> stencilState(ServiceDefinition definition) {
    return client.getQuery().getStencilTag(definition.getStencil().getTagName())
        .onItem().transform(state -> ImmutableStencilTree.builder().sites(state.getSites()).build());
  }
  
  private Uni<HdesTree> hdesState(ServiceDefinition definition) {
    return client.getQuery().getHdesTag(definition.getHdes().getTagName()).onItem()
    .transform((AstTag tag) -> {
      final var envir = client.getConfig().getHdes().envir();
      
      tag.getValues().forEach(value -> {
        final var body = ImmutableStoreEntity.builder()
            .id(value.getId())
            .hash(value.getHash())
            .bodyType(value.getBodyType())
            .body(value.getCommands())
            .build();
        
        switch (value.getBodyType()) {
        case FLOW: { envir.addCommand().id(value.getId()).flow(body).build(); break; }
        case DT: { envir.addCommand().id(value.getId()).decision(body).build(); break; }
        case FLOW_TASK: { envir.addCommand().id(value.getId()).service(body).build(); break; }
        default: return;
        }
      });
      
      final var stateBuilder = ImmutableComposerState.builder();
      envir.build().getValues().values().forEach(v -> ComposerEntityMapper.toComposer(stateBuilder, v));
      final var state = stateBuilder.build();
      return ImmutableHdesTree.builder()
          .decisions(state.getDecisions())
          .services(state.getServices())
          .flows(state.getFlows())
          .build();
    });
    
  }  
  private Uni<DialobTree> dialobState(ServiceDefinition definition) {
    return Multi.createFrom().items(definition.getDescriptors().stream()).onItem().transformToUni(process -> {
      final var formId = process.getFormId();
      return Uni.combine().all().unis(
          client.getQuery().getDialobFormRev(formId).onFailure().recoverWithNull(), 
          client.getQuery().getDialobForm(formId).onFailure().recoverWithNull()).asTuple();
    }).concatenate().collect().asList()
      .onItem().transform(formAndRev -> {
        final var usedRevs = formAndRev.stream().filter(rev -> rev.getItem1() != null)
            .collect(Collectors.toMap(rev -> rev.getItem1().getId(), rev -> rev.getItem1(), (a, b) -> b));
        
        final var usedForms = formAndRev.stream().filter(form -> form.getItem2() != null)
            .collect(Collectors.toMap(rev -> rev.getItem2().getId(), rev -> rev.getItem2(), (a, b) -> b));        
        
        return ImmutableDialobTree.builder()
            .putAllRevs(usedRevs)
            .putAllForms(usedForms)
            .build(); 
      }); 
  }
}
