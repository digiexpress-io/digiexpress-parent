package io.resys.avatar.client.tests.config;

import java.util.Arrays;
import java.util.List;

import io.resys.avatar.client.api.Avatar;
import io.resys.avatar.client.api.AvatarRestApi;
import io.resys.avatar.client.api.ImmutableAvatar;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Path;

@Path("q/digiexpress/api")
@ApplicationScoped
public class AvatarTestResource implements AvatarRestApi {

  private final ImmutableAvatar mockData = getProfile();


  @Override
  public Uni<List<Avatar>> findAllAvatars() {
    return Uni.createFrom().item(Arrays.asList(mockData, mockData));
  }
  @Override
  public Uni<Avatar> getOrCreateAvatar(String externalId) {
    return Uni.createFrom().item(mockData);
  }
  
  private static ImmutableAvatar getProfile() {
    return ImmutableAvatar.builder()
    .id("id-1234")
    .version("v1.0")
    .externalId("ext-id")
    .avatarType("XX")
    .created(AvatarTestCase.getTargetDate())
    .updated(AvatarTestCase.getTargetDate())
    .colorCode("color")
    .displayName("xxx")
    .letterCode("letter code")
    .build();
  }



}
