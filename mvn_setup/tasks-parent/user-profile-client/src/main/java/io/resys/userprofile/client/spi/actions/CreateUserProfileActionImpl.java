package io.resys.userprofile.client.spi.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.resys.userprofile.client.api.UserProfileClient.CreateUserProfileAction;
import io.resys.userprofile.client.api.model.UserProfile;
import io.resys.userprofile.client.api.model.UserProfileCommand.CreateUserProfile;
import io.resys.userprofile.client.api.model.UserProfileCommand.UpsertUserProfile;
import io.resys.userprofile.client.spi.store.DocumentStore;
import io.resys.userprofile.client.spi.visitors.CreateUserProfileVisitor;
import io.resys.userprofile.client.spi.visitors.UpdateUserProfileVisitor;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CreateUserProfileActionImpl implements CreateUserProfileAction {
  private final DocumentStore ctx;
  
  @Override
  public Uni<UserProfile> createOne(CreateUserProfile command) {
    return this.createMany(Arrays.asList(command))
        .onItem().transform(items -> items.get(0)) ;
  }
  
  @Override
  public Uni<UserProfile> createOne(UpsertUserProfile command) {
    return ctx.getConfig().accept(new UpdateUserProfileVisitor(Arrays.asList(command), ctx)).onItem()
        .transformToUni(item -> item).onItem().transform(items -> items.get(0));
  }

  @Override
  public Uni<List<UserProfile>> createMany(List<? extends CreateUserProfile> commands) {
    return ctx.getConfig().accept(new CreateUserProfileVisitor(commands));
  }

  @Override
  public Uni<List<UserProfile>> upsertMany(List<? extends UpsertUserProfile> commands) {
    return ctx.getConfig().accept(new UpdateUserProfileVisitor(new ArrayList<>(commands), ctx)).onItem()
        .transformToUni(item -> item).onItem().transform(items -> items);
  }



}
