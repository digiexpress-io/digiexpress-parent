package io.resys.thena.tasks.dev.app;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.annotation.Nullable;

@JsonSerialize(as=CurrentUser.class)
public interface CurrentUser {
  String userId();

  default String getUserId() {
    return this.userId();
  }

  @Nullable
  String givenName();

  @Nullable
  default String getGivenName() {
    return this.givenName();
  }


  @Nullable
  String familyName();

  @Nullable
  default String getFamilyName() {
    return this.familyName();
  }

}
