package io.resys.userprofile.client.api.model;

import java.io.Serializable;
import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable @JsonSerialize(as = ImmutableUserProfileTransaction.class) @JsonDeserialize(as = ImmutableUserProfileTransaction.class)
public interface UserProfileTransaction extends Serializable {
  String getId();
  List<UserProfileCommand> getCommands(); 
}