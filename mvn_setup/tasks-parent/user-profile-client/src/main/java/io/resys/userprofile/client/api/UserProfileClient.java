package io.resys.userprofile.client.api;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import io.resys.thena.api.entities.Tenant;
import io.resys.userprofile.client.api.model.UserProfile;
import io.resys.userprofile.client.api.model.UserProfileCommand.CreateUserProfile;
import io.resys.userprofile.client.api.model.UserProfileCommand.UpsertUserProfile;
import io.resys.userprofile.client.api.model.UserProfileCommand.UserProfileUpdateCommand;
import io.smallrye.mutiny.Uni;

public interface UserProfileClient {

  RepositoryQuery repoQuery();
  Uni<Tenant> getRepo();
  UserProfileClient withRepoId(String repoId);
  
  CreateUserProfileAction createUserProfile();
  UpdateUserProfileAction updateUserProfile();
  UserProfileQuery userProfileQuery();

  interface CreateUserProfileAction {
    Uni<UserProfile> createOne(CreateUserProfile command);
    Uni<UserProfile> createOne(UpsertUserProfile command);
    Uni<List<UserProfile>> createMany(List<? extends CreateUserProfile> commands);
    Uni<List<UserProfile>> upsertMany(List<? extends UpsertUserProfile> commands);
  }

  interface UpdateUserProfileAction {
    Uni<UserProfile> updateOne(UserProfileUpdateCommand command);
    Uni<UserProfile> updateOne(List<UserProfileUpdateCommand> commands);
    Uni<List<UserProfile>> updateMany(List<UserProfileUpdateCommand> commands);
  }

  interface UserProfileQuery {
    Uni<List<UserProfile>> findAll();
    Uni<List<UserProfile>> findByIds(Collection<String> profileIds);
    Uni<UserProfile> get(String profileId);
    Uni<List<UserProfile>> deleteAll(String committerId, Instant targetDate);
  }
  
  public interface RepositoryQuery {
    RepositoryQuery repoName(String repoName);
    UserProfileClient build();

    Uni<UserProfileClient> deleteAll();
    Uni<UserProfileClient> delete();
    Uni<UserProfileClient> create();
    Uni<UserProfileClient> createIfNot();
    Uni<Optional<UserProfileClient>> get();
  } 

}
