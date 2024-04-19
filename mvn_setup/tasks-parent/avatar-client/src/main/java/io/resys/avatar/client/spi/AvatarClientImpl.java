package io.resys.avatar.client.spi;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import io.resys.avatar.client.api.Avatar;
import io.resys.avatar.client.api.AvatarClient;
import io.resys.avatar.client.api.AvatarCommand.AvatarUpdateCommand;
import io.resys.avatar.client.api.AvatarCommand.CreateAvatar;
import io.resys.avatar.client.spi.store.AvatarStore;
import io.resys.avatar.client.spi.store.AvatarStoreConfig;
import io.resys.avatar.client.spi.visitors.CreateAvatarVisitor;
import io.resys.avatar.client.spi.visitors.FindAllAvatarsVisitor;
import io.resys.avatar.client.spi.visitors.GetAvatarVisitor;
import io.resys.avatar.client.spi.visitors.GetAvatarsByIdsVisitor;
import io.resys.avatar.client.spi.visitors.UpdateAvatarVisitor;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AvatarClientImpl implements AvatarClient {
  private final AvatarStore ctx;
  
  public AvatarStore getCtx() { return ctx; }
  
  @Override
  public AvatarClient withTenantId(String repoId) {
    return new AvatarClientImpl(ctx.withTenantId(repoId));
  }

  @Override
  public Uni<Tenant> getTenant() {
    return ctx.getTenant();
  }
  @Override
  public CreateAvatarAction createAvatar() {
    return new CreateAvatarAction() {
      @Override
      public Uni<Avatar> createOne(CreateAvatar command) {
        return this.createMany(Arrays.asList(command))
            .onItem().transform(items -> items.get(0));
      }
      @Override
      public Uni<List<Avatar>> createMany(List<? extends CreateAvatar> commands) {
        return ctx.getConfig().accept(new FindAllAvatarsVisitor()).onItem()
            .transformToUni(allProfiles -> ctx.getConfig().accept(new CreateAvatarVisitor(commands, allProfiles)));
      }
    };
  }
  @Override
  public UpdateAvatarAction updateAvatar() {
    return new UpdateAvatarAction() {
      @Override
      public Uni<Avatar> updateOne(AvatarUpdateCommand command) {        
        return updateOne(Arrays.asList(command));
      }
      @Override
      public Uni<Avatar> updateOne(List<AvatarUpdateCommand> commands) {
        RepoAssert.notNull(commands, () -> "commands must be defined!");
        RepoAssert.isTrue(commands.size() > 0, () -> "No commands to apply!");
        
        return ctx.getConfig().accept(new FindAllAvatarsVisitor()).onItem() 
        .transformToUni(allProfiles -> {
          final var applyCommands = new UpdateAvatarVisitor(commands, ctx, allProfiles);
          return ctx.getConfig().accept(applyCommands);
        })
        .onItem().transformToUni(resp -> resp)
        .onItem().transform(profiles -> profiles.get(0));
      }

      @Override
      public Uni<List<Avatar>> updateMany(List<AvatarUpdateCommand> commands) {
        RepoAssert.notNull(commands, () -> "commands must be defined!");
        RepoAssert.isTrue(commands.size() > 0, () -> "No commands to apply!");
        
        return ctx.getConfig().accept(new FindAllAvatarsVisitor()).onItem() 
        .transformToUni(allProfiles -> {
          final var applyCommands = new UpdateAvatarVisitor(commands, ctx, allProfiles);
          return ctx.getConfig().accept(applyCommands);
        })
        .onItem().transformToUni(item -> item);
      }
    };
  }
  @Override
  public AvatarQuery queryAvatars() {
    return new AvatarQuery() {
      @Override public Uni<Avatar> get(String profileId) {
        return ctx.getConfig().accept(new GetAvatarVisitor(profileId));
      }
      @Override public Uni<List<Avatar>> findAll() {
        return ctx.getConfig().accept(new FindAllAvatarsVisitor());
      }
      @Override public Uni<List<Avatar>> findByIds(Collection<String> profileIds) {
        return ctx.getConfig().accept(new GetAvatarsByIdsVisitor(profileIds));
      }
    };
  }
  @Override
  public AvatarTenantQuery queryTenants() {
    AvatarStore.InternalAvatarTenantQuery repo = ctx.query();
    return new AvatarTenantQuery() {
      private String tenantName;
      
      @Override public Uni<AvatarClient> createIfNot() { return repo.createIfNot().onItem().transform(doc -> new AvatarClientImpl(doc)); }
      @Override public Uni<AvatarClient> create() { return repo.create().onItem().transform(doc -> new AvatarClientImpl(doc)); }
      @Override public AvatarClient build() { return new AvatarClientImpl(repo.build()); }
      @Override public Uni<AvatarClient> delete() { return repo.delete().onItem().transform(doc -> new AvatarClientImpl(doc)); }
      @Override public Uni<AvatarClient> deleteAll() { return repo.deleteAll().onItem().transform(doc -> new AvatarClientImpl(ctx)); }
      @Override
      public AvatarTenantQuery tenantName(String tenantName) {
        this.tenantName = tenantName;
        repo.repoName(tenantName).headName(AvatarStoreConfig.HEAD_NAME);
        return this;
      }
      @Override
      public Uni<Optional<AvatarClient>> get() {
        RepoAssert.notEmpty(tenantName, () -> "tenantName must be defined!");
        
        final var client = ctx.getConfig().getClient();
        return client.tenants().find().id(tenantName)
            .get().onItem().transform(existing -> {
              if(existing == null) {
                final Optional<AvatarClient> result = Optional.empty();
                return result;
              }
              return Optional.of(new AvatarClientImpl(repo.build()));
            });
        
      }
    };
  }
}
