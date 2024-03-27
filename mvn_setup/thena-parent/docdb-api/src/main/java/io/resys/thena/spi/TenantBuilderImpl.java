package io.resys.thena.spi;

import io.resys.thena.api.actions.ImmutableTenantCommitResult;
import io.resys.thena.api.actions.TenantActions;
import io.resys.thena.api.actions.TenantActions.TenantCommitResult;
import io.resys.thena.api.actions.TenantActions.CommitStatus;
import io.resys.thena.api.entities.ImmutableTenant;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.api.exceptions.RepoException;
import io.resys.thena.support.Identifiers;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class TenantBuilderImpl implements TenantActions.TenantBuilder {

  private final DbState state;
  private String name;
  private StructureType type;
  
  public TenantBuilderImpl name(String name, StructureType type) {
    this.name = name;
    this.type = type;
    return this;
  }
  
  @Override
  public Uni<TenantCommitResult> build() {
    log.debug("Creating repository '{}' of type {}.", name, type);

    RepoAssert.notEmpty(name, () -> "repo name not defined!");
    RepoAssert.notNull(type, () -> "type name not defined!");
    RepoAssert.isName(name, () -> "repo name has invalid characters!");

    return state.tenant().getByName(name)
      .onItem().transformToUni((Tenant existing) -> {
      
      final Uni<TenantCommitResult> result;
      if(existing != null) {
        log.error("Existing repository found with name '{}'", name);
        result = Uni.createFrom().item(ImmutableTenantCommitResult.builder()
            .status(CommitStatus.CONFLICT)
            .addMessages(RepoException.builder().nameNotUnique(existing.getName(), existing.getId()))
            .build());
      } else {
        result = state.tenant().findAll()
        .collect().asList().onItem()
        .transformToUni((allRepos) -> { 
          
          final var newRepo = ImmutableTenant.builder()
              .id(Identifiers.uuid())
              .rev(Identifiers.uuid())
              .type(type)
              .name(name)
              .prefix("nested_" + (allRepos.size() + 10) + "_")
              .build();
          
          return state.tenant().insert(newRepo)
            .onItem().transform(next -> (TenantCommitResult) ImmutableTenantCommitResult.builder()
                .repo(next)
                .status(CommitStatus.OK)
                .build());
        });
      }
      return result;
    });
  }
}
