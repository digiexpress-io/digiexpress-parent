package io.digiexpress.tagomi.spi;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import io.digiexpress.tagomi.api.TagomiStore;
import io.digiexpress.tagomi.api.entities.ImmutableTag;
import io.digiexpress.tagomi.api.entities.TagomiContainer;
import io.digiexpress.tagomi.spi.builders.UpsertBuilderImpl;
import io.digiexpress.tagomi.spi.support.RepoException;
import io.digiexpress.tagomi.spi.support.StoreException;
import io.digiexpress.tagomi.spi.support.StoreException.StoreExceptionMsg;
import io.resys.thena.api.actions.TenantActions.CommitStatus;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.api.entities.git.Branch;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class TagomiStoreImpl implements TagomiStore {

  private final TagomiStoreConfig config;
  
  @Override
  public TagomiStore withTenant(String tenantName, String headName) {
    return new TagomiStoreImpl(ImmutableTagomiStoreConfig.builder()
        .from(config)
        .tenantName(tenantName)
        .headName(headName)
        .build());
  }

  @Override
  public TagomiStore.BranchQuery queryBranches() {
    return new TagomiStore.BranchQuery() {
      @Override
      public Uni<List<TagomiContainer.Tag>> findAllTags() {
        return config.getClient().git(config.getTenantName()).tenants()
            .get().onItem().transform(objects -> {
              if(objects.getStatus() != QueryEnvelopeStatus.OK) {
                throw new StoreException("TAGOMI_BRANCH_QUERY_FAIL", null, 
                    StoreExceptionMsg.builder()
                    .id(objects.getRepo().getId())
                    .value(objects.getRepo().getName())
                    .args(objects.getMessages().stream().map(message->message.getText()).collect(Collectors.toList()))
                    .build()); 
              }
              
              return objects.getObjects().getBranches().values().stream()
                  .map(branch -> {
                    final TagomiContainer.Tag result = ImmutableTag.builder()
                        .commitId(branch.getCommit())
                        .name(branch.getName()).build();
                    return result;
                  })
                  .toList();
            });
      }

      @Override
      public Uni<Optional<Branch>> findOneBranch() {
        // TODO Auto-generated method stub
        return null;
      }
    };
  }
  

  @Override
  public TagomiStore.TenantBuilder tenantBuilder() {
    return new TagomiStore.TenantBuilder() {
      private String repoName = config.getTenantName();
      private String headName = config.getHeadName();
      @Override
      public TagomiStore.TenantBuilder tenantName(String repoName) {
        this.repoName = repoName;
        return this;
      }
      @Override
      public TagomiStore.TenantBuilder headName(String headName) {
        this.headName = headName;
        return this;
      }
      @Override
      public Uni<TagomiStore> create() {
        Objects.requireNonNull(repoName, () -> "tenantName must be defined!");
        final var client = config.getClient();
        final var newRepo = client.tenants().commit().name(repoName, StructureType.git).build();
        return newRepo.onItem().transform((repoResult) -> {
          if(repoResult.getStatus() != CommitStatus.OK) {
            throw new RepoException("Can't create repository with name: '"  + repoName + "'!", repoResult); 
          }
          return build();
        });
      }
      @Override
      public TagomiStore build() {
        Objects.requireNonNull(repoName, () -> "tenantName must be defined!");
        return createWithNewConfig(ImmutableTagomiStoreConfig.builder()
            .from(config)
            .tenantName(repoName)
            .headName(headName == null ? config.getHeadName() : headName)
            .build());
      }
      @Override
      public Uni<Tuple2<Boolean, TagomiStore>> createIfNot() {
        final var client = config.getClient();
        
        return client.git(config.getTenantName()).tenants().get().onItem().transformToUni(repo -> {
          if(repo.getRepo() == null) {
            return client.tenants().commit()
                .name(config.getTenantName(), StructureType.git).build().onItem().transform(newRepo -> Tuple2.of(true, build())); 
          }
          return Uni.createFrom().item(Tuple2.of(false, build()));
        });
      }
    };
  }
  
  @Override
  public UpsertBuilder upsertBuilder() {
    return new UpsertBuilderImpl(config);
  }

  @Override
  public StateQuery stateQuery() {
    // TODO Auto-generated method stub
    return null;
  }
  
  protected TagomiStoreImpl createWithNewConfig(TagomiStoreConfig config) {
    return new TagomiStoreImpl(config);
  }
}
