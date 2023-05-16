package io.digiexpress.client.spi.composer.commands;

import io.digiexpress.client.api.Client;
import io.digiexpress.client.api.ComposerCache;
import io.digiexpress.client.api.ComposerEntity.TagState;
import io.digiexpress.client.api.ImmutableTagState;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetProjectTags {
  
  private final Client client;
  private final ComposerCache cache;
  
  public Uni<TagState> build() {
    final var repoName = client.getConfig().getStore().getRepoName();
    
    final var cached = cache.getTagState(repoName);
    if(cached.isPresent()) {
      return Uni.createFrom().item(cached.get());
    }
    return client.getQuery().getProjectTags().onItem().transform(tags -> {
      final var state = ImmutableTagState.builder().name(repoName).value(tags).build();
      cache.save(state);
      return state;
    });
  }
}
