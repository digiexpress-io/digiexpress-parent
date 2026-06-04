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

import java.util.Objects;
import java.util.function.Consumer;

import io.resys.limaone.authoring.ImmutableNewPrintoutProps;
import io.resys.limaone.authoring.ImmutableNewPrintoutProps.Builder;
import io.resys.limaone.authoring.NewPrintout;
import io.resys.limaone.model.ImmutableLocaleLabel;
import io.resys.limaone.model.ImmutablePrintout;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.model.Printout;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.smallrye.mutiny.Uni;



public class NewPrintoutImpl extends AuthoringTemplate<NewPrintoutImpl, Model<Printout>> implements NewPrintout {

  private NewPrintoutProps props;

  public NewPrintoutImpl(AuthoringConfig config) {
    super(config);
  }
  
  @Override
  public NewPrintoutImpl props(NewPrintoutProps props) {
    this.props = props;
    return this;
  }

  @Override
  public NewPrintoutImpl props(Consumer<Builder> props) {
    final var builder = ImmutableNewPrintoutProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @Override
  public Uni<Model<Printout>> build() {
    return config.getPersistence().worldBuilder()
      .createdAt(getCreatedAt())
      .author(getAuthor())
      .docs(BodyType.PRINTOUT, BodyType.LOCALE)
      .build(nextWorld -> {
        final var body = internalBuild(nextWorld.getCurrentWorld());
        return nextWorld.newModel(body.getServiceName(), body);
      });
  }
  
  private Printout internalBuild(ModelWorld world) {
    Objects.requireNonNull(props, () -> "props must be defined");

    
    final var printout = ImmutablePrintout.builder()
        .serviceName(props.getServiceName())
        .orchestratorName(props.getOrchestratorName());

    final var duplicate = world.getPrintouts().values().stream()
        .filter(p -> p.getBody().getServiceName().equals(props.getServiceName()))
        .findFirst();
    
    if(duplicate.isPresent()) {
      throw new AuthoringException(props, "Printout: '" + props.getServiceName() + "' already exists!");
    }
              
    for(final var label : props.getLabels()) {        

      final var localeRef = label.getLocale();
      final var locale = world.findOneLocale(localeRef);
          
      printout.addLabels(ImmutableLocaleLabel.builder()
          .locale(locale.map(e -> e.getId()).orElse(localeRef))
          .labelValue(label.getLabelValue())
          .build());

      if(locale.isEmpty()) {
        throw new AuthoringException(
            props, 
            "Locale with id: '" + label.getLocale() + "' does not exist in: '" + String.join(",", world.getLocales().keySet()) + "'!");          
      }
    }
    
    return printout.build();
  }
}
