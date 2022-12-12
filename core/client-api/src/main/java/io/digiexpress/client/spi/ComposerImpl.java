package io.digiexpress.client.spi;

import java.util.List;
import java.util.stream.Collectors;

import io.digiexpress.client.api.Client;
import io.digiexpress.client.api.ClientEntity.ServiceDefinition;
import io.digiexpress.client.api.ClientStore.StoreState;
import io.digiexpress.client.api.Composer;
import io.digiexpress.client.api.ComposerEntity.ComposerContentType;
import io.digiexpress.client.api.ComposerEntity.DefinitionState;
import io.digiexpress.client.api.ComposerEntity.DialobTree;
import io.digiexpress.client.api.ComposerEntity.HdesTree;
import io.digiexpress.client.api.ComposerEntity.HeadState;
import io.digiexpress.client.api.ComposerEntity.StencilTree;
import io.digiexpress.client.api.ImmutableComposerMessage;
import io.digiexpress.client.api.ImmutableDefinitionState;
import io.digiexpress.client.api.ImmutableDialobTree;
import io.digiexpress.client.api.ImmutableHdesTree;
import io.digiexpress.client.api.ImmutableHeadState;
import io.digiexpress.client.api.ImmutableStencilTree;
import io.digiexpress.client.spi.store.StoreException;
import io.resys.hdes.client.api.ImmutableComposerState;
import io.resys.hdes.client.api.ImmutableStoreEntity;
import io.resys.hdes.client.api.ast.AstTag;
import io.resys.hdes.client.spi.composer.ComposerEntityMapper;
import io.resys.thena.docdb.api.models.Repo;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ComposerImpl implements Composer {

  private final Client client;

  @Override
  public ComposerBuilder create() {
    return new ComposerBuilderImpl(client);
  }

  @Override
  public Composer.ComposerQuery query() {
    final var parent = this;
    return new Composer.ComposerQuery() {
      @Override public Uni<HeadState> release(String releaseId) { return null; }
      @Override public Uni<HeadState> head() { return client.getQuery().getRepos().onItem().transformToUni(parent::headState); }
      @Override public Uni<DefinitionState> definition(String definitionId) { return client.getQuery().getServiceDef(definitionId).onItem().transformToUni(parent::defState); }
    };
  }

  private Uni<DefinitionState> defState(ServiceDefinition definition) {
    final var dialob = dialobState(definition);
    final var hdes = hdesState(definition);
    final var stencil = stencilState(definition);
    return Uni.combine().all().unis(dialob, hdes, stencil).asTuple()
      .onItem().transform(tuple -> ImmutableDefinitionState.builder()
          .definition(definition)
          .dialob(tuple.getItem1())
          .hdes(tuple.getItem2())
          .stencil(tuple.getItem3())
          .build());
  }

  private Uni<StencilTree> stencilState(ServiceDefinition definition) {
    return client.getQuery().getStencil(definition.getStencil().getTagName())
        .onItem().transform(state -> ImmutableStencilTree.builder().sites(state.getSites()).build());
  }
  
  private Uni<HdesTree> hdesState(ServiceDefinition definition) {
    return client.getQuery().getHdes(definition.getHdes().getTagName()).onItem()
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
          client.getQuery().getFormRev(formId).onFailure().recoverWithNull(), 
          client.getQuery().getForm(formId).onFailure().recoverWithNull()).asTuple();
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
  
  private Uni<HeadState> headState(List<Repo> repos) { 
    final var created = repos.stream()
        .filter(repo -> 
            repo.getName().equals(client.getConfig().getStore().getRepoName()) ||
            repo.getId().equals(client.getConfig().getStore().getRepoName()))
        .findFirst().isPresent();  
    
    if(created) {
      return client.getQuery().head()
          .onItem().transform(this::okState)
          .onFailure(StoreException.class).recoverWithItem(this::errorState);        
    }
    final var notCreated = ImmutableHeadState.builder()
        .contentType(ComposerContentType.NOT_CREATED)
        .name(client.getConfig().getStore().getRepoName())
        .build();
    return Uni.createFrom().item(notCreated);
  }

  private HeadState okState(StoreState store) {
    final var mapper = client.getConfig().getParser();
    final var revisions = store.getProjects().values().stream()
        .collect(Collectors.toMap(e -> e.getId(), mapper::toProject));
    final var definitions = store.getDefinitions().values().stream()
        .collect(Collectors.toMap(e -> e.getId(), mapper::toDefinition));
    
    
    return ImmutableHeadState.builder()
      .name(client.getConfig().getStore().getRepoName())
      .contentType(ComposerContentType.OK)
      .commit(store.getCommit())
      .commitMsg(store.getCommitMsg())
      .projects(revisions)
      .definitions(definitions)
      .build();
  }
  
  private HeadState errorState(Throwable e) {
    final StoreException ex = (StoreException) e;
    return ImmutableHeadState.builder()
        .contentType(ComposerContentType.ERRORS)
        .name(client.getConfig().getStore().getRepoName())
        .messages(ex.getMessages().stream().map(msg -> ImmutableComposerMessage.builder()
            .id(msg.getId())
            .value(msg.getValue())
            .args(msg.getArgs())
            .build())
            .collect(Collectors.toList()))
        .build();
  }
}
