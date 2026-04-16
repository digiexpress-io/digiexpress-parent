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

import io.resys.limaone.authoring.ImmutableNewPrintoutResourceProps;
import io.resys.limaone.authoring.ImmutableNewPrintoutResourceProps.Builder;
import io.resys.limaone.authoring.NewPrintoutResource;
import io.resys.limaone.spi.printout.PrintoutImageStorage;
import io.resys.limaone.spi.printout.PrintoutImageStorage.OperationStatus;
import io.resys.limaone.model.ImmutablePrintoutResource;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.model.PrintoutResource;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.smallrye.mutiny.Uni;



public class NewPrintoutResourceImpl extends AuthoringTemplate<NewPrintoutResourceImpl, Model<PrintoutResource>> implements NewPrintoutResource {

  private NewPrintoutResourceProps props;

  public NewPrintoutResourceImpl(AuthoringConfig config) {
    super(config);
  }

  @Override
  public NewPrintoutResource props(NewPrintoutResourceProps props) {
    this.props = props;
    return this;
  }

  @Override
  public NewPrintoutResource props(Consumer<Builder> props) {
    final var builder = ImmutableNewPrintoutResourceProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @Override
  public Uni<Model<PrintoutResource>> build() {
    Objects.requireNonNull(props, () -> "props must be defined");

    if (props.getUploadBody() == null) {
      throw new AuthoringException(props, "uploadBody must be provided for resource: " + props.getResourceName());
    }

    if ("text/*".equals(props.getContentType())) {
      return buildScriptResource();
    }

    if ("image/*".equals(props.getContentType())) {
      return buildImageResource();
    }

    throw new AuthoringException(props, "Unsupported content type: " + props.getContentType());
  }

  private Uni<Model<PrintoutResource>> buildScriptResource() {
    return config.getPersistence().worldBuilder()
      .createdAt(getCreatedAt())
      .author(getAuthor())
      .docs(BodyType.PRINTOUT_RESOURCE)
      .build(nextWorld -> {
        final var body = internalBuildScript(nextWorld.getCurrentWorld());
        return nextWorld.newModel(body.getResourceName(), body);
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
            final var body = internalBuildImage(nextWorld.getCurrentWorld(), image.getId());
            return nextWorld.newModel(body.getResourceName(), body);
          })
      );
  }

  private PrintoutResource internalBuildScript(ModelWorld world) {
    validateAndCheckDuplicates(world);

    final var printoutResource = ImmutablePrintoutResource.builder()
        .resourceName(props.getResourceName())
        .contentType(props.getContentType())
        .externalLocation("")
        .content(props.getUploadBody())
        .templateIds(props.getTemplateIds());

    return printoutResource.build();
  }

  private PrintoutResource internalBuildImage(ModelWorld world, String externalLocation) {
    validateAndCheckDuplicates(world);

    final var printoutResource = ImmutablePrintoutResource.builder()
        .resourceName(props.getResourceName())
        .contentType(props.getContentType())
        .externalLocation(externalLocation)
        .content(props.getUploadBody())
        .templateIds(props.getTemplateIds());

    return printoutResource.build();
  }

  private void validateAndCheckDuplicates(ModelWorld world) {
    final var duplicate = world.getPrintoutResources().values().stream()
        .filter(p -> p.getBody().getResourceName().equals(props.getResourceName()))
        .findFirst();

    if(duplicate.isPresent()) {
      throw new AuthoringException(props, "PrintoutResource: '" + props.getResourceName() + "' already exists!");
    }

    for(final var templateId : props.getTemplateIds()) {
      if(!world.getPrintoutPages().containsKey(templateId)) {
        throw new AuthoringException(props,
            "PrintoutResource templateId: '" + templateId + "' does not exist in: '" + String.join(",", world.getPrintoutPages().keySet()) + "'!");
      }
    }
  }
}
