package io.resys.userprofile.client.spi.actions;

import java.util.List;

import io.resys.userprofile.client.api.UserProfileClient.UiSettingsQuery;
import io.resys.userprofile.client.api.model.UiSettings;
import io.resys.userprofile.client.spi.UserProfileStore;
import io.resys.userprofile.client.spi.visitors.FindAllUserUiSettingsVisitor;
import io.resys.userprofile.client.spi.visitors.GetUserUiSettingsVisitor;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class UiSettingsQueryImpl implements UiSettingsQuery {
  private final UserProfileStore ctx;
  
  @Override
  public Uni<List<UiSettings>> findAll(String profileId) {
    return ctx.getConfig().accept(new FindAllUserUiSettingsVisitor(profileId));
  }
  
  @Override
  public Uni<UiSettings> get(String profileId, String settingsId) {
    return ctx.getConfig().accept(new GetUserUiSettingsVisitor(profileId, settingsId));
  }

}
