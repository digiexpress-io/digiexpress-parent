package io.resys.thena.spi;

import io.resys.thena.api.actions.ImmutableRepoResult;
import io.resys.thena.api.actions.TenantActions;
import io.resys.thena.api.actions.TenantActions.RepoResult;
import io.resys.thena.api.actions.TenantActions.RepoStatus;
import io.resys.thena.api.entities.ImmutableTenant;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.RepoType;
import io.resys.thena.api.exceptions.RepoException;
import io.resys.thena.support.Identifiers;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class RepoBuilderImpl implements TenantActions.RepoBuilder {

  private final DbState state;
  private String name;
  private RepoType type;
  
  public RepoBuilderImpl name(String name, RepoType type) {
    this.name = name;
    this.type = type;
    return this;
  }
  
  @Override
  public Uni<RepoResult> build() {
    log.debug("Creating repository '{}' of type {}.", name, type);

    RepoAssert.notEmpty(name, () -> "repo name not defined!");
    RepoAssert.notNull(type, () -> "type name not defined!");
    RepoAssert.isName(name, () -> "repo name has invalid characters!");

    return state.project().getByName(name)
      .onItem().transformToUni((Tenant existing) -> {
      
      final Uni<RepoResult> result;
      if(existing != null) {
        log.error("Existing repository found with name '{}'", name);
        result = Uni.createFrom().item(ImmutableRepoResult.builder()
            .status(RepoStatus.CONFLICT)
            .addMessages(RepoException.builder().nameNotUnique(existing.getName(), existing.getId()))
            .build());
      } else {
        result = state.project().findAll()
        .collect().asList().onItem()
        .transformToUni((allRepos) -> { 
          
          final var newRepo = ImmutableTenant.builder()
              .id(Identifiers.uuid())
              .rev(Identifiers.uuid())
              .type(type)
              .name(name)
              .prefix("nested_" + (allRepos.size() + 10) + "_")
              .build();
          
          return state.project().insert(newRepo)
            .onItem().transform(next -> (RepoResult) ImmutableRepoResult.builder()
                .repo(next)
                .status(RepoStatus.OK)
                .build());
        });
      }
      return result;
    });
  }
}
