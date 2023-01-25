package io.digiexpress.client.spi;

import io.digiexpress.client.api.Client;
import io.digiexpress.client.api.Composer;
import io.digiexpress.client.api.ComposerCache;
import io.digiexpress.client.api.ComposerEntity.DefinitionState;
import io.digiexpress.client.api.ComposerEntity.HeadState;
import io.digiexpress.client.api.ComposerEntity.TagState;
import io.digiexpress.client.spi.composer.commands.GetDefState;
import io.digiexpress.client.spi.composer.commands.GetHeadState;
import io.digiexpress.client.spi.composer.commands.GetProjectTags;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ComposerImpl implements Composer {

  private final Client client;
  private final ComposerCache cache;

  @Override
  public ComposerBuilder create() {
    return new ComposerBuilderImpl(client, cache);
  }

  @Override
  public Composer.ComposerQuery query() {
    return new Composer.ComposerQuery() {
    @Override public Uni<HeadState> release(String releaseId) { return null; }
    @Override public Uni<HeadState> head() { return new GetHeadState(client, cache).build(); }
    @Override public Uni<DefinitionState> definition(String definitionId) { return new GetDefState(client, cache, definitionId).build(); }
    @Override public Uni<TagState> tags() { return new GetProjectTags(client, cache).build(); }
    };
  }
}
