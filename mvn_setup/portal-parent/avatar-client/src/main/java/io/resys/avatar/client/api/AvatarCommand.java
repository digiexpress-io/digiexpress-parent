package io.resys.avatar.client.api;

/*-
 * #%L
 * thena-tasks-client
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.Serializable;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;



@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "commandType")
@JsonSubTypes({
  @Type(value = ImmutableCreateAvatar.class, name = "CREATE_AVATAR"),  
  @Type(value = ImmutableChangeAvatarColorCode.class, name = "CHANGE_COLOR_CODE"),  
  @Type(value = ImmutableChangeAvatarLetterCode.class, name = "CHANGE_LETTER_CODE"),  
  @Type(value = ImmutableChangeAvatarDisplayName.class, name = "CHANGE_DISPLAY_NAME"),
})
public interface AvatarCommand extends Serializable {
  String getId();
  AvatarCommandType getCommandType();
  
  enum AvatarCommandType {
    CREATE_AVATAR,
    CHANGE_COLOR_CODE,
    CHANGE_LETTER_CODE,
    CHANGE_DISPLAY_NAME;
  }

  @Value.Immutable @JsonSerialize(as = ImmutableCreateAvatar.class) @JsonDeserialize(as = ImmutableCreateAvatar.class)
  interface CreateAvatar extends AvatarCommand {
    String getExternalId();
    String getAvatarType();
    String getSeedData();
    @Nullable String getColorCode();
    @Nullable String getLetterCode();
    @Nullable String getDisplayName();
    @Override default AvatarCommandType getCommandType() { return AvatarCommandType.CREATE_AVATAR; } 
  }
  
  @JsonTypeInfo(
      use = JsonTypeInfo.Id.NAME,
      include = JsonTypeInfo.As.PROPERTY,
      property = "commandType")
  @JsonSubTypes({
    @Type(value = ImmutableChangeAvatarColorCode.class, name = "CHANGE_COLOR_CODE"),  
    @Type(value = ImmutableChangeAvatarLetterCode.class, name = "CHANGE_LETTER_CODE"),  
    @Type(value = ImmutableChangeAvatarDisplayName.class, name = "CHANGE_DISPLAY_NAME"),  
  })
  interface AvatarUpdateCommand extends AvatarCommand {
    String getId();
  }
  @Value.Immutable @JsonSerialize(as = ImmutableChangeAvatarColorCode.class) @JsonDeserialize(as = ImmutableChangeAvatarColorCode.class)
  interface ChangeAvatarColorCode extends AvatarUpdateCommand {
    String getColorCode();
    @Override default AvatarCommandType getCommandType() { return AvatarCommandType.CHANGE_COLOR_CODE; }
  }
  @Value.Immutable @JsonSerialize(as = ImmutableChangeAvatarLetterCode.class) @JsonDeserialize(as = ImmutableChangeAvatarLetterCode.class)
  interface ChangeAvatarLetterCode extends AvatarUpdateCommand {
    String getLetterCode();
    @Override default AvatarCommandType getCommandType() { return AvatarCommandType.CHANGE_LETTER_CODE; }
  }
  @Value.Immutable @JsonSerialize(as = ImmutableChangeAvatarDisplayName.class) @JsonDeserialize(as = ImmutableChangeAvatarDisplayName.class)
  interface ChangeAvatarDisplayName extends AvatarUpdateCommand {
    String getDisplayName();
    @Override default AvatarCommandType getCommandType() { return AvatarCommandType.CHANGE_DISPLAY_NAME; }
  }
}
