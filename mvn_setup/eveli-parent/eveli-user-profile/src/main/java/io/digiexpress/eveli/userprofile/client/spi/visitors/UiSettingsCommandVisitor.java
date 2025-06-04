package io.digiexpress.eveli.userprofile.client.spi.visitors;

/*-
 * #%L
 * eveli-user-profile
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.digiexpress.eveli.userprofile.client.api.model.ImmutableUiSettingForConfig;
import io.digiexpress.eveli.userprofile.client.api.model.ImmutableUiSettingForVisibility;
import io.digiexpress.eveli.userprofile.client.api.model.ImmutableUiSettings;
import io.digiexpress.eveli.userprofile.client.api.model.UiSettings;
import io.digiexpress.eveli.userprofile.client.api.model.UiSettingsCommand;
import io.digiexpress.eveli.userprofile.client.api.model.UiSettingsCommand.UpsertUiSettings;
import io.resys.thena.spi.ThenaDocConfig;
import io.resys.thena.support.OidUtils;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.core.json.JsonObject;


public class UiSettingsCommandVisitor {
  @SuppressWarnings("unused")
  private final UiSettings start;
  private final List<UiSettingsCommand> visitedCommands = new ArrayList<>();
  private ImmutableUiSettings current;
  
  public UiSettingsCommandVisitor(ThenaDocConfig ctx) {
    this.start = null;
    this.current = null;
  }
  
  public UiSettingsCommandVisitor(UiSettings start, ThenaDocConfig ctx) {
    this.start = start;
    this.current = ImmutableUiSettings.builder().from(start).build(); 
  }
  
  public Tuple2<UiSettings, List<JsonObject>> visitTransaction(List<? extends UiSettingsCommand> commands) throws NoChangesException {
    for(final var command : commands) {
      visitCommand(command);
    }
    
    if(visitedCommands.isEmpty()) {
      throw new NoChangesException();
    }

    return Tuple2.of(this.current, Collections.emptyList());
  }
  
  private ImmutableUiSettings visitCommand(UiSettingsCommand command) throws NoChangesException {
    switch (command.getCommandType()) {
    
    case UpsertUiSettings:
      return visitUpsertUiSettings((UpsertUiSettings) command);
    }
    
    throw new UpdateUiSettingsVisitorException(String.format("Unsupported command type: %s, body: %s", command.getClass().getSimpleName(), command.toString())); 
  }
  private ImmutableUiSettings visitUpsertUiSettings(UpsertUiSettings command) throws NoChangesException {
    
    // deep copy, just in case of accidental json fields
    final var builder = ImmutableUiSettings.builder();
    if(this.current == null) {
      builder
        .id(OidUtils.gen())
        .settingsId(command.getSettingsId())
        .userId(command.getUserId());
    } else {
      builder.from(current);
    }
    
    final var newEntity = builder
        .config(command.getConfig().stream().map(e -> ImmutableUiSettingForConfig.builder().from(e).build()).toList())
        .visibility(command.getVisibility().stream().map(e -> ImmutableUiSettingForVisibility.builder().from(e).build()).toList())
        .build();
    
    this.current = newEntity;
    visitedCommands.add(command);
    return this.current;
  }
  
  public static class NoChangesException extends Exception {
    private static final long serialVersionUID = 5955370217897065513L;
  }

  public static class UpdateUiSettingsVisitorException extends RuntimeException {
    private static final long serialVersionUID = -1385190644836838881L;

    public UpdateUiSettingsVisitorException(String message, Throwable cause) {
      super(message, cause);
    }

    public UpdateUiSettingsVisitorException(String message) {
      super(message);
    }
  }
}
