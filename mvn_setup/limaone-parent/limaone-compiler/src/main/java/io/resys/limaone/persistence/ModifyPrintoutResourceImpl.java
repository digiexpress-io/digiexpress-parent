package io.resys.limaone.persistence;

/*-
 * #%L
 * limaone-compiler
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import java.util.Base64;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.resys.limaone.authoring.ImmutableModifyPrintoutResourceProps;
import io.resys.limaone.authoring.ImmutableModifyPrintoutResourceProps.Builder;
import io.resys.limaone.authoring.ModifyPrintoutResource;
import io.resys.limaone.spi.printout.PrintoutImageStorage;
import io.resys.limaone.spi.printout.PrintoutImageStorage.OperationStatus;
import io.resys.limaone.model.ImmutablePrintoutResource;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.model.PrintoutResource;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.smallrye.mutiny.Uni;


public class ModifyPrintoutResourceImpl extends AuthoringTemplate<ModifyPrintoutResourceImpl, Model<PrintoutResource>> implements ModifyPrintoutResource {

  private ModifyPrintoutResourceProps props;

  public ModifyPrintoutResourceImpl(AuthoringConfig config) {
    super(config);
  }

  @Override
  public ModifyPrintoutResource props(ModifyPrintoutResourceProps props) {
    this.props = props;
    return this;
  }

  @Override
  public ModifyPrintoutResource props(Consumer<Builder> props) {
    final var builder = ImmutableModifyPrintoutResourceProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @Override
  public Uni<Model<PrintoutResource>> build() {
    Objects.requireNonNull(props, () -> "props must be defined");

    final var existingContentType = props.getContentType();

    if(props.getUploadBody() == null || "text/*".equals(existingContentType)) {
      return buildScriptResource();
    }

    if("image/*".equals(existingContentType)) {
      return buildImageResource();
    }

    return buildScriptResource();
  }

  private Uni<Model<PrintoutResource>> buildScriptResource() {
    return config.getPersistence().worldBuilder()
      .createdAt(getCreatedAt())
      .author(getAuthor())
      .docs(BodyType.PRINTOUT_RESOURCE)
      .build(nextWorld -> {
        final var body = internalBuild(nextWorld.getCurrentWorld(), null);
        return nextWorld.mergeModel(props.getResourceId(), body.getResourceName(), body);
      });
  }

  private Uni<Model<PrintoutResource>> buildImageResource() {
    final var imageStorage = config.getEnvir().getBean(PrintoutImageStorage.class);
    final byte[] imageBytes = Base64.getDecoder().decode(props.getUploadBody());

    return imageStorage.write(imageBytes)
      .onItem().transform(envelope -> {
        if(envelope.getOperationStatus() != OperationStatus.OK) {
          final var errorMsg = envelope.getOperationLogs().stream()
            .map(message -> message.getText())
            .collect(Collectors.joining(", "));
          throw new AuthoringException(props, "Failed to store image: " + errorMsg);
        }
        return envelope.getObject();
      })
      .onItem().transformToUni(image ->
        config.getPersistence().worldBuilder()
          .createdAt(getCreatedAt())
          .author(getAuthor())
          .docs(BodyType.PRINTOUT_RESOURCE)
          .build(nextWorld -> {
            final var body = internalBuild(nextWorld.getCurrentWorld(), image.getId());
            return nextWorld.mergeModel(props.getResourceId(), body.getResourceName(), body);
          })
      );
  }

  private PrintoutResource internalBuild(ModelWorld world, String imageExternalLocation) {
    final var start = world.getPrintoutResources().get(props.getResourceId());
    if(start == null) {
      throw new AuthoringException(props, "Can't find printout resource: '" + props.getResourceId() + "' to update!");
    }

    if(props.getPrintoutPageIds() != null) {
      for(final var printoutPageId : props.getPrintoutPageIds()) {
        if(!world.getPrintoutPages().containsKey(printoutPageId)) {
          throw new AuthoringException(props,
              "PrintoutResource printoutPageId: '" + printoutPageId + "' does not exist in: '" + String.join(",", world.getPrintoutPages().keySet()) + "'!");
        }
      }
    }

    if(props.getResourceName() != null) {
      final var duplicate = world.getPrintoutResources().values().stream()
          .filter(p -> !p.getId().equals(props.getResourceId()))
          .filter(p -> p.getBody().getResourceName().equals(props.getResourceName()))
          .findFirst();

      if(duplicate.isPresent()) {
        throw new AuthoringException(props, "PrintoutResource with name: '" + props.getResourceName() + "' already exists!");
      }
    }

    return ImmutablePrintoutResource.builder()
        .from(start.getBody())
        .contentType(props.getContentType() == null ? start.getBody().getContentType() : props.getContentType())
        .resourceName(props.getResourceName() == null ? start.getBody().getResourceName() : props.getResourceName())
        .externalLocation(imageExternalLocation == null ? start.getBody().getExternalLocation() : imageExternalLocation)
        .content(props.getUploadBody() == null ? start.getBody().getContent() : props.getUploadBody())
        .printoutPageIds(props.getPrintoutPageIds() == null ? start.getBody().getPrintoutPageIds() : props.getPrintoutPageIds())
        .build();
  }
}
