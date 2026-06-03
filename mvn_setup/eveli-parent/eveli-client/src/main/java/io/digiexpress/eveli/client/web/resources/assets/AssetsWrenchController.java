package io.digiexpress.eveli.client.web.resources.assets;

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

import java.util.Collections;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.digiexpress.eveli.client.spi.assets.HdesDefaultAssets;
import io.digiexpress.eveli.envir.api.EveliEnvirClient;
import io.resys.hdes.client.api.HdesComposer;
import io.resys.hdes.client.api.HdesComposer.ComposerEntity;
import io.resys.hdes.client.api.HdesComposer.ComposerState;
import io.resys.hdes.client.api.HdesComposer.CopyAs;
import io.resys.hdes.client.api.HdesComposer.CreateEntity;
import io.resys.hdes.client.api.HdesComposer.DebugRequest;
import io.resys.hdes.client.api.HdesComposer.DebugResponse;
import io.resys.hdes.client.api.HdesComposer.StoreDump;
import io.resys.hdes.client.api.HdesComposer.TagDiff;
import io.resys.hdes.client.api.HdesComposer.UpdateEntity;
import io.resys.hdes.client.api.HdesStore.CommitLog;
import io.resys.hdes.client.api.HdesStore.StoreEntity;
import io.resys.hdes.client.api.ImmutableDiffRequest;
import io.resys.hdes.client.api.ast.AstCommand;
import io.resys.hdes.client.api.ast.AstTag;
import io.resys.hdes.client.api.ast.AstTagSummary;
import io.resys.hdes.client.spi.composer.DebugVisitor;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import lombok.RequiredArgsConstructor;



@RestController
@RequestMapping("/worker/rest/api/assets/wrench")
@RequiredArgsConstructor
public class AssetsWrenchController {
  private final ObjectMapper objectMapper;
  private final HdesComposer composer;
  private final EveliEnvirClient envir;

  @GetMapping(path = "/defaultAssets", produces = MediaType.APPLICATION_JSON_VALUE)
  public Uni<List<StoreEntity>> defaultAssets() {
    return getComposer()
        .onItem().transformToUni(composer ->  new HdesDefaultAssets(composer.getClient(), true).accept());
  }
  
  @GetMapping(path = "/dataModels", produces = MediaType.APPLICATION_JSON_VALUE)
  public Uni<ComposerState> dataModels(@RequestHeader(value = "Branch-Name", required = false) String branchName) {
    return getComposer()
        .onItem().transformToUni(composer -> composer.withBranch(branchName).get());
  }

  @GetMapping(path = "/exports", produces = MediaType.APPLICATION_JSON_VALUE)
  public Uni<StoreDump> exports() {
    return getComposer()
        .onItem().transformToUni(composer -> composer.getStoreDump());
  }

  @PostMapping(path = "/commands", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public Uni<ComposerEntity<?>> commands(@RequestBody String body, @RequestHeader(value = "Branch-Name", required = false) String branchName) throws JsonMappingException, JsonProcessingException {
    final var command = objectMapper.readValue(body, UpdateEntity.class);
    return getComposer()
        .onItem().transformToUni(composer -> composer.withBranch(branchName).dryRun(command))
        .onItem().invoke(() -> envir.invalidateCache());
  }
  
  @GetMapping(path = "/commands/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public Uni<List<AstCommand>> getCommands(
      @PathVariable String id, 
      @RequestHeader(value = "Branch-Name", required = false) String branchName) {
    
    return getComposer()
        .onItem().transformToUni(composer -> composer.withBranch(branchName).getCommands(id));
  }

  @PostMapping(path = "/debugs", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public Uni<DebugResponse> debug(
      @RequestBody DebugRequest debug, 
      @RequestHeader(value = "Branch-Name", required = false) String branchName) {
    
    return getComposer()
        .onItem().transform(composer -> composer.withBranch(branchName).getClient())
        .onItem().transformToUni(client -> client.store().query().get().onItem().transform(state -> Tuple2.of(client, state)))
        .onItem().transform(tuple -> new DebugVisitor(tuple.getItem1()).visit(debug, tuple.getItem2()));
  }

  @PostMapping(path = "/importTag", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public Uni<ComposerState> importTag(@RequestBody AstTag entity) {
    return getComposer()
        .onItem().transformToUni(composer -> composer.importTag(entity))
        .onItem().invoke(() -> envir.invalidateCache());
  }

  @PostMapping(path = "/resources", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public Uni<ComposerState> create(
      @RequestBody CreateEntity entity, 
      @RequestHeader(value = "Branch-Name", required = false) String branchName) {
    
    return getComposer()
        .onItem().transformToUni(composer -> composer.withBranch(branchName).create(entity))
        .onItem().invoke(() -> envir.invalidateCache());
  }

  @PutMapping(path = "/resources", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public Uni<ComposerState> update(
      @RequestBody UpdateEntity entity, 
      @RequestHeader(value = "Branch-Name", required = false) String branchName) {
    
    return getComposer()
        .onItem().transformToUni(composer -> composer.withBranch(branchName).update(entity))
        .onItem().invoke(() -> envir.invalidateCache());
  }

  @DeleteMapping(path = "/resources/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Uni<ComposerState> delete(@PathVariable String id, @RequestHeader(value = "Branch-Name", required = false) String branchName) {
    return getComposer()
        .onItem().transformToUni(composer -> composer.withBranch(branchName).delete(id))
        .onItem().invoke(() -> envir.invalidateCache());
  }

  @GetMapping(path = "/resources/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Uni<ComposerEntity<?>> get(@PathVariable String id, @RequestHeader(value = "Branch-Name", required = false) String branchName) {
    return getComposer()
        .onItem().transformToUni(composer -> composer.withBranch(branchName).get(id))
        .onItem().invoke(() -> envir.invalidateCache());
  }

  @PostMapping(path = "/copyas", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public Uni<ComposerState> copyAs(@RequestBody CopyAs entity, @RequestHeader(value = "Branch-Name", required = false) String branchName) {
    return getComposer()
        .onItem().transformToUni(composer -> composer.withBranch(branchName).copyAs(entity)
        .onItem().invoke(() -> envir.invalidateCache()));
  }

  @GetMapping(path = "/diff", produces = MediaType.APPLICATION_JSON_VALUE)
  public Uni<TagDiff> diff(@RequestParam("baseId") String baseId, @RequestParam("targetId") String targetId) {
    final var request = ImmutableDiffRequest.builder().baseId(baseId).targetId(targetId).build();
    return getComposer()
        .onItem().transformToUni(composer -> composer.diff(request));
  }

  @GetMapping(path = "/summary/{tagId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Uni<AstTagSummary> summary(@PathVariable("tagId") String tagId) {
    return getComposer()
        .onItem().transformToUni(composer -> composer.summary(tagId));
  }

  @GetMapping(path="/flow-names")
  public Uni<List<String>> flowNames() {
    return getComposer()
    .onItem().transformToUni(composer -> composer.withBranch(null).get().map(state->{
      return state.getFlows().entrySet().stream().map(flow->flow.getValue().getAst().getName()).toList();
    }));
    /*
    return envir.withCockpitIdSupplier(composer.getCockpitIdSupplier())
        .runtimeQuery().findOne().onItem().transform(runtime -> {
      if(runtime.isEmpty()) {
        return Collections.emptyList();
      }
      return runtime.get().getWrench().getFlowNames();
    });
    */ 
  }
  
  @GetMapping(path = "/commitlogs", produces = MediaType.APPLICATION_JSON_VALUE)
  public Uni<List<CommitLog>> commitlogs() {
    return getComposer().onItem().transformToUni(composer -> composer.getClient().store().commitLog().build());
  }
  
  
  protected Uni<HdesComposer> getComposer() {
    return this.composer.withCockpitFromProvider();
  }
  

}
