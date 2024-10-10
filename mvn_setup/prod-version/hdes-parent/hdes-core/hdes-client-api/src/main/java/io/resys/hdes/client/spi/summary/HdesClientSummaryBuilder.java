package io.resys.hdes.client.spi.summary;

/*-
 * #%L
 * hdes-client-api
 * %%
 * Copyright (C) 2020 - 2023 Copyright 2020 ReSys OÜ
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


import io.resys.hdes.client.api.HdesClient.SummaryBuilder;
import io.resys.hdes.client.api.HdesStore.StoreEntity;
import io.resys.hdes.client.api.ast.AstCommand;
import io.resys.hdes.client.api.ast.AstCommand.AstCommandValue;
import io.resys.hdes.client.api.ast.AstTagSummary;
import io.resys.hdes.client.api.ast.ImmutableAstTagSummary;
import io.resys.hdes.client.api.ast.ImmutableSummaryItem;
import io.resys.hdes.client.spi.util.HdesAssert;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HdesClientSummaryBuilder implements SummaryBuilder {

  private static class SummaryException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public SummaryException(String message) {
      super(message);
    }
  }

  private Collection<StoreEntity> tags;
  private String tagId;


  @Override
  public SummaryBuilder tags(Collection<StoreEntity> tags) {
    this.tags = tags;
    return this;
  }

  @Override
  public SummaryBuilder tagId(String tagId) {
    this.tagId = tagId;
    return this;
  }

  @Override
  public AstTagSummary build() {
    HdesAssert.notNull(tags, () -> "tags can't be null!");
    HdesAssert.notNull(tagId, () -> "tagId can't be null!");

    final var tag = getTagById(tags, tagId);

    final var flows = fromCommands(tag.getBody(), AstCommandValue.SET_TAG_FL);
    final var decisions = fromCommands(tag.getBody(), AstCommandValue.SET_TAG_DT);
    final var services = fromCommands(tag.getBody(), AstCommandValue.SET_TAG_ST);

    return ImmutableAstTagSummary.builder()
        .tagName(getNameFromTag(tag))
        .flows(flows)
        .decisions(decisions)
        .services(services)
        .build();
  }

  private List<ImmutableSummaryItem> fromCommands(List<AstCommand> commands, AstCommandValue targetType) {
    return commands.stream()
        .filter(c -> c.getType().equals(targetType))
        .map(c -> ImmutableSummaryItem.builder()
            .id(requireNonNull(c.getId(), "Asset id can't be null!"))
            .name(requireNonNull(getAssetName(c), "Asset name can't be null!"))
            .body(getAssetBody(c))
            .build())
        .collect(Collectors.toUnmodifiableList());
  }

  private String getAssetBody(AstCommand command) {
    String body = requireNonNull(command.getValue(), "Asset body can't be null!");
    if (command.getType().equals(AstCommandValue.SET_TAG_DT)) {
      return String.join("\n", body.split("},\\{"));
    }
    return body;
  }

  private String requireNonNull(String value, String message) {
    if(value == null) {
      throw new SummaryException(message);
    }
    return value;
  }

  private String getNameFromTag(StoreEntity entity) {
    return entity.getBody().stream()
        .filter(c -> c.getType().equals(AstCommand.AstCommandValue.SET_TAG_NAME))
        .findFirst()
        .orElseThrow(() -> new SummaryException("Tag does not have a name!"))
        .getValue();
  }

  private String getAssetName(AstCommand value) {
    switch (value.getType()) {
      case SET_TAG_FL:
        return "flows/" + matchName(value, "id: (\\w+)");
      case SET_TAG_ST:
        return "services/" + matchName(value, "public class (\\w+)");
      case SET_TAG_DT:
        return "decisions/" + matchName(value, "\"value\":\"(\\w+)\",\"type\":\"SET_NAME\"\\},\\{");
    }
    return null;
  }

  private String matchName(AstCommand value, String regex) {
    final var body = value.getValue();
    if (body == null) {
      return null;
    }
    final var matcher = Pattern.compile(regex).matcher(body);
    if (matcher.find()) {
      return matcher.group(1);
    }
    return null;
  }

  private StoreEntity getTagById(Collection<StoreEntity> tags, String id) {
    if (tags.size() > 0) {
      return tags.stream().filter(t -> t.getId().equals(id)).findFirst().orElseThrow(() -> new SummaryException("Tag not found!"));
    } else {
      throw new SummaryException("No tags yet!");
    }
  }
}
