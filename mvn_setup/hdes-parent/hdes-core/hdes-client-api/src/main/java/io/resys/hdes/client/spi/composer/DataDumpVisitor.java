package io.resys.hdes.client.spi.composer;

/*-
 * #%L
 * hdes-client-api
 * %%
 * Copyright (C) 2020 - 2021 Copyright 2020 ReSys OÜ
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.codec.digest.DigestUtils;

import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.HdesClient.HdesTypesMapper;
import io.resys.hdes.client.api.HdesComposer.StoreDump;
import io.resys.hdes.client.api.HdesStore.StoreEntity;
import io.resys.hdes.client.api.HdesStore.StoreState;
import io.resys.hdes.client.api.ImmutableStoreDump;
import io.resys.hdes.client.api.ast.AstBody.AstSource;
import io.resys.hdes.client.api.ast.AstCommand;
import io.resys.hdes.client.api.ast.AstCommand.AstCommandValue;
import io.resys.hdes.client.spi.changeset.AstCommandOptimiser;
import io.resys.hdes.client.api.ast.ImmutableAstCommand;
import io.resys.hdes.client.api.ast.ImmutableAstSource;



public class DataDumpVisitor {

  private final HdesTypesMapper defs;
  private final AstCommandOptimiser optimise;
  
  public DataDumpVisitor(HdesClient client) {
    super();
    this.defs = client.mapper();
    this.optimise = new AstCommandOptimiser(client);
  }

  private static String md5(String ...input) {
    Vector<InputStream> v = new Vector<>();
    
    for(final var el : input) {
      v.add(new ByteArrayInputStream(el.getBytes(StandardCharsets.UTF_8)));
    }
    SequenceInputStream seqStream = new SequenceInputStream(v.elements());
    try {
      String md5Hash = DigestUtils.md5Hex(seqStream);
      seqStream.close();
      return md5Hash;
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  private AstSource visitDt(StoreEntity service) throws IOException {
    final List<AstCommand> commands = optimise.optimise(service.getBody(), service.getBodyType());

    final var body = defs.commandsString(commands);
    return ImmutableAstSource.builder()
        .id(service.getId())
        .bodyType(service.getBodyType())
        .hash(md5(body))
        .addAllCommands(commands)
        .build();
  }

  private AstSource visitSt(StoreEntity service) throws IOException {
    final var commands = optimise.optimise(service.getBody(), service.getBodyType());
    final var body = commands.get(0).getValue();
    
    return ImmutableAstSource.builder()
        .id(service.getId())
        .bodyType(service.getBodyType())
        .hash(md5(body))
        .commands(commands)
        .build();
  }

  private AstSource visitFl(StoreEntity service) throws IOException {
    final var commands = optimise.optimise(service.getBody(), service.getBodyType());
    final var body = commands.get(0).getValue();
    
    return ImmutableAstSource.builder()
        .id(service.getId())
        .bodyType(service.getBodyType())
        .hash(md5(body))
        .addCommands(ImmutableAstCommand.builder().value(body).type(AstCommandValue.SET_BODY).build())
        .build();
  }

  public StoreDump visit(StoreState state) {
    final var result = ImmutableStoreDump.builder();
    final var inputs = new ArrayList<String>();
    
    final var assets = new ArrayList<StoreEntity>();
    assets.addAll(state.getDecisions().values());
    assets.addAll(state.getServices().values());
    assets.addAll(state.getFlows().values());
    
    for (final var service : assets) {
      try {
        switch (service.getBodyType()) {
        case FLOW:
          final var flow = visitFl(service);
          inputs.add(flow.getHash());
          result.addValue(flow);
          break;
        case DT:
          final var decision = visitDt(service);
          inputs.add(decision.getHash());
          result.addValue(decision);
          break;
        case FLOW_TASK:
          final var flowtask = visitSt(service); 
          inputs.add(flowtask.getHash());
          result.addValue(flowtask);
          break;

        default:
          continue;
        }
      } catch (Exception e) {
        throw new RuntimeException("Failed to create migration because of asset: '"  + service.getId() + "', msg: " + e.getMessage(), e);
      }
    }
    return result.id(md5(inputs.toArray(new String[0]))).build();
  }
}
