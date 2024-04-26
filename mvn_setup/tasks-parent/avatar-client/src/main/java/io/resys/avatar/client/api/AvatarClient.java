package io.resys.avatar.client.api;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import io.resys.avatar.client.api.AvatarCommand.AvatarUpdateCommand;
import io.resys.avatar.client.api.AvatarCommand.CreateAvatar;
import io.resys.thena.api.entities.Tenant;
import io.smallrye.mutiny.Uni;

public interface AvatarClient {
  Uni<Tenant> getTenant();
  AvatarClient withTenantId(String tenantId);
  
  CreateAvatarAction createAvatar();
  UpdateAvatarAction updateAvatar();
  AvatarQuery queryAvatars();
  RepositoryQuery repoQuery();
  
  
  interface CreateAvatarAction {
    Uni<Avatar> createOne(CreateAvatar command);
    Uni<List<Avatar>> createMany(List<? extends CreateAvatar> commands);
  }

  interface UpdateAvatarAction {
    Uni<Avatar> updateOne(AvatarUpdateCommand command);
    Uni<Avatar> updateOne(List<AvatarUpdateCommand> commands);
    Uni<List<Avatar>> updateMany(List<AvatarUpdateCommand> commands);
  }

  interface AvatarQuery {
    Uni<List<Avatar>> findAll();
    Uni<List<Avatar>> findByIds(Collection<String> avatarIds);
    Uni<Avatar> get(String avatarId);
  }
  public interface RepositoryQuery {
    RepositoryQuery repoName(String repoName);
    AvatarClient build();

    Uni<AvatarClient> deleteAll();
    Uni<AvatarClient> delete();
    Uni<AvatarClient> create();
    Uni<AvatarClient> createIfNot();
    Uni<Optional<AvatarClient>> get();
  }

}
