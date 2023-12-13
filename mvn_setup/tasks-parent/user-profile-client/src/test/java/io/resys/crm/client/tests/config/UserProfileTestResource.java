package io.resys.crm.client.tests.config;

import java.util.Arrays;
import java.util.List;

import io.resys.userprofile.client.api.model.Document.DocumentType;
import io.resys.userprofile.client.api.model.ImmutableCreateUserProfile;
import io.resys.userprofile.client.api.model.ImmutableNotificationSetting;
import io.resys.userprofile.client.api.model.ImmutableUserDetails;
import io.resys.userprofile.client.api.model.ImmutableUserProfile;
import io.resys.userprofile.client.api.model.ImmutableUserProfileTransaction;
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

  private final ImmutableUserProfile mockUserProfile = ImmutableUserProfile.builder()
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
      
      .addTransactions(
          ImmutableUserProfileTransaction.builder()
          .id("transation-1")
          .addCommands(ImmutableCreateUserProfile
              .builder()
              .id("id-1234")
              .userId("userDonald")
              .details(ImmutableUserDetails.builder()
                  .firstName("Donald")
                  .lastName("Trum")
                  .email("donald@gmail.com")
                  .username("donaldtrump")
                  .build())
              .build())
          .build())
      .documentType(DocumentType.USER_PROFILE)
      .build();

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
  
 
}
