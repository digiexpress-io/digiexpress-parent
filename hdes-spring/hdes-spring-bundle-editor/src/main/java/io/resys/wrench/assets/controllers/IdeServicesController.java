package io.resys.wrench.assets.controllers;

/*-
 * #%L
 * wrench-assets-bundle
 * %%
 * Copyright (C) 2016 - 2019 Copyright 2016 ReSys OÜ
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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

import io.resys.wrench.assets.bundle.api.repositories.AssetIdeServices;
import io.resys.wrench.assets.bundle.api.repositories.AssetIdeServices.AssetCommand;
import io.resys.wrench.assets.bundle.api.repositories.AssetIdeServices.AssetCopyAs;
import io.resys.wrench.assets.bundle.api.repositories.AssetIdeServices.AssetDebug;
import io.resys.wrench.assets.bundle.api.repositories.AssetIdeServices.AssetResource;
import io.resys.wrench.assets.bundle.api.repositories.AssetIdeServices.AssetSummary;
import io.resys.wrench.assets.bundle.api.repositories.AssetServiceRepository.ServiceDataModel;
import io.resys.wrench.assets.bundle.api.repositories.AssetServiceRepository.ServiceType;

@RestController
@RequestMapping(ControllerUtil.ASSET_CONTEXT_PATH)
public class IdeServicesController {
  private final AssetIdeServices assetIdeServices;

  public IdeServicesController(AssetIdeServices assetIdeServices) {
    super();
    this.assetIdeServices = assetIdeServices;
  }

  @GetMapping(value="/about", produces = MediaType.APPLICATION_JSON_VALUE)
  public AssetSummary summaries() {
    return assetIdeServices.summary();
  }

  @GetMapping(value="/dataModels", produces = MediaType.APPLICATION_JSON_VALUE)
  public Map<ServiceType, List<ServiceDataModel>> dataModels() {
    return assetIdeServices.models();
  }

  @PostMapping(path = "/commands", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public JsonNode commands(@RequestBody AssetCommand command) {
    return assetIdeServices.commands(command);
  }

  @PostMapping(path = "/debugs", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public String debug(@RequestBody AssetDebug debug) {
    return assetIdeServices.debug(debug);
  }

  @PostMapping(path = "/resources", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public AssetResource create(@RequestBody AssetResource entity) {
    return assetIdeServices.persist(entity);
  }

  @PutMapping(path = "/resources/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public AssetResource update(@PathVariable String id, @RequestBody AssetResource entity) {
    return assetIdeServices.persist(entity);
  }
  
  @DeleteMapping(path = "/resources/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AssetResource> delete(@PathVariable String id) {
    Collection<AssetResource> resources = assetIdeServices.query().id(id).build();
    if(resources.isEmpty()) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    AssetResource resource = assetIdeServices.remove(resources.iterator().next());
    return new ResponseEntity<>(resource, HttpStatus.OK);
  }
  
  @PostMapping(path = "/copyas", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public AssetResource copyAs(@RequestBody AssetCopyAs entity) {
    return assetIdeServices.copyAs(entity);
  }

  @GetMapping(path = "/resources/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AssetResource> get(@PathVariable String id) {
    Collection<AssetResource> resources = assetIdeServices.query().id(id).build();
    if(resources.isEmpty()) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(resources.iterator().next(), HttpStatus.OK);
  }
  
  @GetMapping(path = "/resources", produces = MediaType.APPLICATION_JSON_VALUE)
  public Collection<AssetResource> find(
      @RequestParam("id") String id,
      @RequestParam("name") String name,
      @RequestParam("rev") String rev,
      @RequestParam("type") ServiceType type) {
    return assetIdeServices.query()
        .id(id)
        .name(name)
        .rev(rev)
        .type(type)
        .build();
  }
}
