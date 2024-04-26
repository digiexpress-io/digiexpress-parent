package io.resys.thena.tasks.dev.app;

import java.util.List;

import io.resys.thena.projects.client.api.ProjectClient;
import io.resys.thena.projects.client.api.TenantConfig.TenantRepoConfig;
import io.resys.thena.projects.client.api.TenantConfig.TenantRepoConfigType;
import io.resys.thena.tasks.dev.app.user.CurrentTenant;
import io.resys.thena.tasks.dev.app.user.CurrentUser;
import io.resys.userprofile.client.api.UserProfileClient;
import io.resys.userprofile.client.api.UserProfileRestApi;
import io.resys.userprofile.client.api.model.UserProfile;
import io.resys.userprofile.client.api.model.UserProfileCommand.CreateUserProfile;
import io.resys.userprofile.client.api.model.UserProfileCommand.UserProfileUpdateCommand;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Path;

@Singleton
@Path("q/digiexpress/api")
public class UserProfileResource implements UserProfileRestApi {

  @Inject private UserProfileClient userProfileClient;
  @Inject private CurrentTenant currentTenant;
  @Inject private CurrentUser currentUser;
  @Inject private ProjectClient tenantClient;

  @Override
  public Uni<List<UserProfile>> findAllUserProfiles() {
    return getUserProfileConfig().onItem().transformToUni(config -> userProfileClient.withRepoId(config.getRepoId()).userProfileQuery().findAll());
  }
  
  @Override
  public Uni<UserProfile> getUserProfileById(String profileId) {
    if("current".equals(profileId)) {
      return getUserProfileConfig().onItem().transformToUni(config -> userProfileClient.withRepoId(config.getRepoId()).userProfileQuery().get(currentUser.getUserId()));
    }
    return getUserProfileConfig().onItem().transformToUni(config -> userProfileClient.withRepoId(config.getRepoId()).userProfileQuery().get(profileId));
  }
  
  @Override
  public Uni<UserProfile> createUserProfile(CreateUserProfile command) {
    return getUserProfileConfig().onItem().transformToUni(config -> userProfileClient.withRepoId(config.getRepoId()).createUserProfile()
        .createOne(command));
  }

  @Override
  public Uni<UserProfile> updateUserProfile(String profileId, List<UserProfileUpdateCommand> commands) {
    
    return getUserProfileConfig().onItem().transformToUni(config -> userProfileClient.withRepoId(config.getRepoId()).updateUserProfile()
        .updateOne(commands));
  }

  @Override
  public Uni<UserProfile> deleteUserProfile(String profileId, UserProfileUpdateCommand command) {
    return getUserProfileConfig().onItem().transformToUni(config -> userProfileClient.withRepoId(config.getRepoId()).updateUserProfile().updateOne(command));
  }
  
  private Uni<TenantRepoConfig> getUserProfileConfig() {
    return tenantClient.queryActiveTenantConfig().get(currentTenant.tenantId())
        .onItem().transform(config -> {
          final var userProfileConfig = config.getRepoConfigs().stream().filter(entry -> entry.getRepoType() == TenantRepoConfigType.USER_PROFILE).findFirst().get();
          return userProfileConfig;
      });
  }
}
  



