package io.digiexpress.client.api;

import io.smallrye.mutiny.Uni;


public interface DigiexpressClient {
  DigiexpressExecutor.Builder executor(DigiexpressEnvir envir);
  DigiexpressEnvir.Builder envir();
  
  DigiexpressStore getStore();
  DigiexpressConfig getConfig();
  DigiexpressClientRepoBuilder repo();
  
  interface DigiexpressClientRepoBuilder {
    DigiexpressClientRepoBuilder repoName(String repoName);
    DigiexpressClientRepoBuilder headName(String headName);
    Uni<DigiexpressClient> create();
    DigiexpressClient build();
  }
}
