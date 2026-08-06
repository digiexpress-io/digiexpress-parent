package io.digiexpress.eveli.client.web.resources.assets;

import java.util.List;

/*-
 * #%L
 * eveli-client
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

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchema;

import io.digiexpress.eveli.client.assets.property_object.api.PropertyObjectDescriptorFactory;
import io.resys.limaone.authoring.Authoring;
import io.resys.limaone.authoring.ModifyPropertyObject.ModifyPropertyObjectProps;
import io.resys.limaone.authoring.NewPropertyObject.NewPropertyObjectProps;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.model.PropertyObject;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/worker/rest/api/assets/properties")
@Slf4j
@RequiredArgsConstructor
public class AssetsPropertyObjectController {

  private final Authoring composer;
  private final PropertyObjectDescriptorFactory descriptorFactory;

  @GetMapping("/")
  public Uni<ModelWorld> root() {
    return getObjects();
  }
  @GetMapping("/objects")
  public Uni<ModelWorld> getObjects() {
    return composer.worldQuery()
      .docs(
        BodyType.PROPERTY_OBJECT
      )
      .findAll();
  }

  @PostMapping("/objects")
  public Uni<Model<PropertyObject>> createResource(@RequestBody NewPropertyObjectProps body) {
    return composer.newModel().newPropertyObject().props(body).build();
  }
  @PutMapping("/objects")
  public Uni<Model<PropertyObject>> updateResource(@RequestBody ModifyPropertyObjectProps body) {
    return composer.modifyModel().modifyPropertyObject().props(body).build();
  }
  @DeleteMapping("/objects/{id}")
  public Uni<Model<?>> deleteResource(@PathVariable("id") String id) {
    return composer.deleteModel().deleteAny()
        .props(props -> props.bodyType(BodyType.PROPERTY_OBJECT).id(id)).build();
  }

  @GetMapping("/objectTypes")
  public Uni<List<String>> getObjectTypes() {
    return Uni.createFrom().item(descriptorFactory.getRegisteredPropertyObjectTypes());
  }
  
  @GetMapping("/objectSchema/{objectType}")
  public Uni<JsonSchema> getObjectSchema(@PathVariable("objectType") String objectType) {
    return Uni.createFrom().item(
        descriptorFactory.getDescriptor(objectType).orElse(descriptorFactory.getDefaultDescriptor())
        .getSchema());
  }
  
}
