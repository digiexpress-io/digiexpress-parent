package io.resys.thena.tasks.dev.app;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.projects.client.api.TenantConfigClient;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfig;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfigType;
import io.resys.thena.tasks.dev.app.user.CurrentTenant;
import io.resys.thena.tasks.dev.app.user.CurrentUser;
import io.resys.userprofile.client.api.UserProfileClient;
import io.resys.userprofile.client.api.model.UserProfile;
import io.resys.userprofile.client.api.model.UserProfileCommand.CreateUserProfile;
import io.resys.userprofile.client.api.model.UserProfileCommand.UserProfileUpdateCommand;
import io.resys.userprofile.client.rest.UserProfileRestApi;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;

@Path("q/digiexpress/api")
public class UserProfileResource implements UserProfileRestApi {

  @Inject UserProfileClient userProfileClient;
  @Inject CurrentTenant currentTenant;
  @Inject CurrentUser currentUser;
  @Inject TenantConfigClient tenantClient;

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
        .createOne((CreateUserProfile) command.withTargetDate(Instant.now()).withUserId(currentUser.userId())));
  }

  @Override
  public Uni<UserProfile> updateUserProfile(String profileId, List<UserProfileUpdateCommand> commands) {
    final var modifiedCommands = commands.stream()
        .map(command -> command.withTargetDate(Instant.now()).withUserId(currentUser.userId()))
        .collect(Collectors.toList());
    
    return getUserProfileConfig().onItem().transformToUni(config -> userProfileClient.withRepoId(config.getRepoId()).updateUserProfile()
        .updateOne(modifiedCommands));
  }

  @Override
  public Uni<UserProfile> deleteUserProfile(String profileId, UserProfileUpdateCommand command) {
    return getUserProfileConfig().onItem().transformToUni(config -> userProfileClient.withRepoId(config.getRepoId()).updateUserProfile()
        .updateOne(command.withTargetDate(Instant.now()).withUserId(currentUser.userId())));
  }
  
  private Uni<TenantRepoConfig> getUserProfileConfig() {
    return tenantClient.queryActiveTenantConfig().get(currentTenant.tenantId())
        .onItem().transform(config -> {
          final var userProfileConfig = config.getRepoConfigs().stream().filter(entry -> entry.getRepoType() == TenantRepoConfigType.USER_PROFILE).findFirst().get();
          return userProfileConfig;
      });
  }
}
  



