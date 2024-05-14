package io.resys.userprofile.client.spi.actions;

import io.resys.thena.support.RepoAssert;
import io.resys.userprofile.client.api.UserProfileClient.UpdateUiSettingsAction;
import io.resys.userprofile.client.api.model.UiSettings;
import io.resys.userprofile.client.api.model.UiSettingsCommand.UiSettingsUpdateCommand;
import io.resys.userprofile.client.spi.UserProfileStore;
import io.resys.userprofile.client.spi.visitors.UpdateUiSettingsVisitor;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateUiSettingsActionImpl implements UpdateUiSettingsAction {

  private final UserProfileStore ctx;

  @Override
  public Uni<UiSettings> updateOne(UiSettingsUpdateCommand command) {
    RepoAssert.notNull(command, () -> "No commands to apply!");
    
    return ctx.getConfig().accept(new UpdateUiSettingsVisitor(command, ctx))
        .onItem().transformToUni(resp -> resp)
        .onItem().transform(customers -> customers);
  }
}

