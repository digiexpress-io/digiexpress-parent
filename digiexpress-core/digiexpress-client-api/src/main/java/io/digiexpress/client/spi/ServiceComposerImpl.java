package io.digiexpress.client.spi;

import java.util.List;
import java.util.stream.Collectors;

import io.digiexpress.client.api.ImmutableComposerMessage;
import io.digiexpress.client.api.ImmutableComposerState;
import io.digiexpress.client.api.ServiceClient;
import io.digiexpress.client.api.ServiceComposer;
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
    return new ServiceComposer.QueryBuilder() {
      @Override
      public Uni<ComposerState> release(String releaseId) {
        // TODO Auto-generated method stub
        return null;
      }
      @Override
      public Uni<ComposerState> head() {
        return client.getQuery().getRepos().onItem().transformToUni(repos -> isRepoCreated(repos) ? getState() : notCreatedState());
      }
    };
  }
  
  private Uni<ComposerState> getState() {
    return client.getQuery().head()
    .onItem().transform(state -> (ComposerState) ImmutableComposerState.builder()
          .name(client.getConfig().getStore().getRepoName())
          .contentType(SiteContentType.OK)
          .commit(state.getCommit())
          .commitMsg(state.getCommitMsg())
          .build())
    .onFailure(StoreException.class).recoverWithItem((e) -> {
      final StoreException ex = (StoreException) e;
      return (ComposerState) ImmutableComposerState.builder()
          .contentType(SiteContentType.ERRORS)
          .name(client.getConfig().getStore().getRepoName())
          .messages(ex.getMessages().stream().map(msg -> ImmutableComposerMessage.builder()
              .id(msg.getId())
              .value(msg.getValue())
              .args(msg.getArgs())
              .build())
              .collect(Collectors.toList()))
          .build();
    });
  }
  
  private boolean isRepoCreated(List<Repo> repos) {
    final var created = repos.stream()
        .filter(repo -> 
            repo.getName().equals(client.getConfig().getStore().getRepoName()) ||
            repo.getId().equals(client.getConfig().getStore().getRepoName()))
        .findFirst().isPresent();
      return created;
  }
  
  private Uni<ComposerState> notCreatedState() {
    final var notCreated = ImmutableComposerState.builder()
        .contentType(SiteContentType.NOT_CREATED)
        .name(client.getConfig().getStore().getRepoName())
        .build();
    return Uni.createFrom().item(notCreated);
  } 
}
