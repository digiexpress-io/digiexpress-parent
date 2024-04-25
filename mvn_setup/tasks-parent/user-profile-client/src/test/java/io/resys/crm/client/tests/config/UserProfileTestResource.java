package io.resys.crm.client.tests.config;

import java.util.Arrays;
import java.util.List;

import io.resys.userprofile.client.api.model.ImmutableNotificationSetting;
import io.resys.userprofile.client.api.model.ImmutableUserDetails;
import io.resys.userprofile.client.api.model.ImmutableUserProfile;
import io.resys.userprofile.client.api.model.UserProfile;
import io.resys.userprofile.client.api.model.UserProfileCommand.CreateUserProfile;
import io.resys.userprofile.client.api.model.UserProfileCommand.UserProfileUpdateCommand;
import io.resys.userprofile.client.rest.UserProfileRestApi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Path;

@Path("q/digiexpress/api")
@ApplicationScoped
public class UserProfileTestResource implements UserProfileRestApi {

  private final ImmutableUserProfile mockUserProfile = getProfile();

  @Override
  public Uni<List<UserProfile>> findAllUserProfiles() {
    return Uni.createFrom().item(Arrays.asList(mockUserProfile));
  }
  
  @Override
  public Uni<UserProfile> createUserProfile(CreateUserProfile command) {
    return Uni.createFrom().item(mockUserProfile);
  }

  @Override
  public Uni<UserProfile> getUserProfileById(String profileId) {
    return Uni.createFrom().item(mockUserProfile);
  }

  @Override
  public Uni<UserProfile> updateUserProfile(String profileId, List<UserProfileUpdateCommand> commands) {
    return Uni.createFrom().item(mockUserProfile);
  }

  @Override
  public Uni<UserProfile> deleteUserProfile(String profileId, UserProfileUpdateCommand command) {
    return Uni.createFrom().item(mockUserProfile);
  }
  
  private static ImmutableUserProfile getProfile() {
    return ImmutableUserProfile.builder()
    .id("id-1234")
    .version("v1.0")
    .created(UserProfileTestCase.getTargetDate())
    .updated(UserProfileTestCase.getTargetDate())
    .details(ImmutableUserDetails.builder()
        .firstName("Ron")
        .lastName("Howard")
        .email("ron_howard@gmail.com")
        .username("ronhoward")
        .build())

    .notificationSettings(Arrays.asList(ImmutableNotificationSetting.builder()
        .type("TASK_ASSIGNED")
        .enabled(true)
        .build()))
    .build();
  }
}
