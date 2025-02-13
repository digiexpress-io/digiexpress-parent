package io.dialob.client.spi;

import io.dialob.client.api.DialobClient;
import io.dialob.client.api.DialobClient.RepoBuilder;
import io.dialob.client.api.DialobClientConfig;
import io.dialob.client.api.ImmutableDialobClientConfig;
import io.dialob.client.spi.support.DialobAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DialobRepoBuilderImpl implements RepoBuilder {
  private final DialobClientConfig config;
  
  private String repoName;
  private String headName;
  @Override
  public RepoBuilder repoName(String repoName) {
    this.repoName = repoName;
    return this;
  }
  @Override
  public RepoBuilder headName(String headName) {
    this.headName = headName;
    return this;
  }
  @Override
  public Uni<DialobClient> create() {
    DialobAssert.notNull(repoName, () -> "repoName must be defined!");
    return config.getStore().repo().repoName(repoName).headName(headName).create()
        .onItem().transform(newStore -> {
          final var newCache = config.getCache().withName(repoName);
          final var newConfig = ImmutableDialobClientConfig.builder().from(config).cache(newCache).store(newStore).build();
          return new DialobClientImpl(newConfig);
        });
  }
  @Override
  public DialobClient build() {
    DialobAssert.notNull(repoName, () -> "repoName must be defined!");
    final var newStore = config.getStore().repo().repoName(repoName).headName(headName).build();
    final var newCache = config.getCache().withName(repoName);
    final var newConfig = ImmutableDialobClientConfig.builder().from(config).cache(newCache).store(newStore).build();
    return new DialobClientImpl(newConfig);
  }
};
