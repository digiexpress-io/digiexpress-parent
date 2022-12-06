package io.digiexpress.client.spi;

import java.util.List;
import java.util.stream.Collectors;

import io.digiexpress.client.api.ImmutableComposerMessage;
import io.digiexpress.client.api.ImmutableServiceComposerState;
import io.digiexpress.client.api.ServiceClient;
import io.digiexpress.client.api.ServiceComposer;
import io.digiexpress.client.api.ServiceComposerState;
import io.digiexpress.client.api.ServiceComposerState.SiteContentType;
import io.digiexpress.client.api.ServiceStore.StoreState;
import io.digiexpress.client.spi.composer.ComposerCreateBuilderImpl;
import io.digiexpress.client.spi.store.StoreException;
import io.resys.thena.docdb.api.models.Repo;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ServiceComposerImpl implements ServiceComposer {

  private final ServiceClient client;

  @Override
  public CreateBuilder create() {
    return new ComposerCreateBuilderImpl(client);
  }

  @Override
  public ServiceComposer.QueryBuilder query() {
    final var parent = this;
    return new ServiceComposer.QueryBuilder() {
      @Override public Uni<ServiceComposerState> release(String releaseId) { return null; }
      @Override public Uni<ServiceComposerState> head() { return client.getQuery().getRepos().onItem().transformToUni(parent::headState); }
    };
  }

  private Uni<ServiceComposerState> headState(List<Repo> repos) { 
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
    final var notCreated = ImmutableServiceComposerState.builder()
        .contentType(SiteContentType.NOT_CREATED)
        .name(client.getConfig().getStore().getRepoName())
        .build();
    return Uni.createFrom().item(notCreated);
  }

  private ServiceComposerState okState(StoreState store) {
    final var mapper = client.getConfig().getMapper();
    final var configs = store.getConfigs().values().stream()
        .collect(Collectors.toMap(e -> e.getId(), mapper::toConfig));
    final var revisions = store.getConfigs().values().stream()
        .collect(Collectors.toMap(e -> e.getId(), mapper::toRev));
    final var definitions = store.getConfigs().values().stream()
        .collect(Collectors.toMap(e -> e.getId(), mapper::toDef));
    
    
    return ImmutableServiceComposerState.builder()
      .name(client.getConfig().getStore().getRepoName())
      .contentType(SiteContentType.OK)
      .commit(store.getCommit())
      .commitMsg(store.getCommitMsg())
      .configs(configs)
      .revisions(revisions)
      .definitions(definitions)
      .build();
  }
  
  private ServiceComposerState errorState(Throwable e) {
    final StoreException ex = (StoreException) e;
    return ImmutableServiceComposerState.builder()
        .contentType(SiteContentType.ERRORS)
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
