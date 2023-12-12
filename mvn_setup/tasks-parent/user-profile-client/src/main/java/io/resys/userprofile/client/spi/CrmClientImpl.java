package io.resys.userprofile.client.spi;

import java.util.Optional;

import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.support.RepoAssert;
import io.resys.userprofile.client.api.UserProfileClient;
import io.resys.userprofile.client.spi.actions.CreateUserProfileActionImpl;
import io.resys.userprofile.client.spi.actions.UserProfileQueryImpl;
import io.resys.userprofile.client.spi.actions.UpdateUserProfileActionImpl;
import io.resys.userprofile.client.spi.store.DocumentStore;
import io.resys.userprofile.client.spi.store.MainBranch;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CrmClientImpl implements UserProfileClient {
  private final DocumentStore ctx;
  
  public DocumentStore getCtx() { return ctx; }
  
  @Override
  public UserProfileClient withRepoId(String repoId) {
    return new CrmClientImpl(ctx.withRepoId(repoId));
  }

  @Override
  public Uni<Repo> getRepo() {
    return ctx.getRepo();
  }
  
  @Override
  public CreateUserProfileAction createUserProfile(){
    return new CreateUserProfileActionImpl(ctx);
  }

  @Override
  public UpdateUserProfileAction updateUserProfile() {
    return new UpdateUserProfileActionImpl(ctx);
  }

  @Override
  public UserProfileQuery userProfileQuery() {
    return new UserProfileQueryImpl(ctx);
  }
  
  @Override
  public RepositoryQuery repoQuery() {
    DocumentStore.DocumentRepositoryQuery repo = ctx.query();
    return new RepositoryQuery() {
      private String repoName;
      
      @Override public Uni<UserProfileClient> createIfNot() { return repo.createIfNot().onItem().transform(doc -> new CrmClientImpl(doc)); }
      @Override public Uni<UserProfileClient> create() { return repo.create().onItem().transform(doc -> new CrmClientImpl(doc)); }
      @Override public UserProfileClient build() { return new CrmClientImpl(repo.build()); }
      @Override public Uni<UserProfileClient> delete() { return repo.delete().onItem().transform(doc -> new CrmClientImpl(doc)); }
      @Override public Uni<UserProfileClient> deleteAll() { return repo.deleteAll().onItem().transform(doc -> new CrmClientImpl(ctx)); }
      @Override
      public RepositoryQuery repoName(String repoName) {
        this.repoName = repoName;
        repo.repoName(repoName).headName(MainBranch.HEAD_NAME);
        return this;
      }
      @Override
      public Uni<Optional<UserProfileClient>> get(String customerId) {
        RepoAssert.notEmpty(customerId, () -> "customerId must be defined!");
        RepoAssert.notEmpty(repoName, () -> "repoName must be defined!");
        
        final var client = ctx.getConfig().getClient();
        return client.repo().projectsQuery().id(repoName)
            .get().onItem().transform(existing -> {
              if(existing == null) {
                final Optional<UserProfileClient> result = Optional.empty();
                return result;
              }
              return Optional.of(new CrmClientImpl(repo.build()));
            });
        
      }
    };
  }
}
