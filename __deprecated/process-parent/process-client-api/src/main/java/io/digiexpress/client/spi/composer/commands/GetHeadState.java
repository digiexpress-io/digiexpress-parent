package io.digiexpress.client.spi.composer.commands;

import java.util.List;
import java.util.stream.Collectors;

import io.digiexpress.client.api.Client;
import io.digiexpress.client.api.ClientStore.StoreState;
import io.digiexpress.client.api.ComposerCache;
import io.digiexpress.client.api.ComposerEntity.ComposerContentType;
import io.digiexpress.client.api.ComposerEntity.HeadState;
import io.digiexpress.client.api.ImmutableComposerMessage;
import io.digiexpress.client.api.ImmutableHeadState;
import io.digiexpress.client.spi.store.StoreException;
import io.resys.thena.docdb.api.models.Repo;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetHeadState {
  private final Client client;
  private final ComposerCache cache;
  
  public Uni<HeadState> build() {
    return client.getQuery().getRepos().onItem().transformToUni(this::headState);
  }

  private Uni<HeadState> headState(List<Repo> repos) {
    final var repoName = client.getConfig().getStore().getRepoName();
    final var cached = cache.getHeadState(repoName);
    if(cached.isPresent()) {
      return Uni.createFrom().item(cached.get());
    }

    final var created = repos.stream()
        .filter(repo -> repo.getName().equals(repoName) || repo.getId().equals(repoName))
        .findFirst().isPresent();  
    
    if(created) {      
      return client.getQuery().getProjectHead()
      .onItem().transform(this::okState)
      .onItem().transform(headState -> {
        cache.save(headState);
        return headState;
      })
      .onFailure(StoreException.class).recoverWithItem(this::errorState);        
    }
    
    final var notCreated = ImmutableHeadState.builder()
        .contentType(ComposerContentType.NOT_CREATED)
        .name(repoName)
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
