package io.resys.thena.tasks.dev.app;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.annotation.Nullable;

@JsonSerialize(as=CurrentUser.class)
public interface CurrentUser {
  String userId();
  @Nullable String email();
  @Nullable String givenName();
  @Nullable String familyName();
  
  default String getUserId() { return this.userId(); }
  @Nullable default String getGivenName() { return this.givenName(); }
  @Nullable default String getFamilyName() { return this.familyName(); }
  @Nullable default String getEmail() { return this.email(); }

}
