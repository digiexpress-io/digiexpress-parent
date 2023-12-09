package io.resys.sysconfig.client.spi.executor.visitors;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import io.resys.hdes.client.api.ImmutableStoreEntity;
import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.hdes.client.api.ast.AstTag;
import io.resys.hdes.client.api.ast.AstTag.AstTagValue;
import io.resys.sysconfig.client.api.AssetClient;
import io.resys.sysconfig.client.api.model.SysConfigRelease;
import io.resys.sysconfig.client.api.model.SysConfigRelease.AssetType;
import io.resys.sysconfig.client.api.model.SysConfigRelease.SysConfigAsset;
import io.resys.sysconfig.client.spi.executor.ExecutorStore.WrenchFlow;
import io.resys.sysconfig.client.spi.executor.ImmutableWrenchFlow;
import io.resys.sysconfig.client.spi.executor.exceptions.ExecutorException;
import io.resys.sysconfig.client.spi.support.ErrorMsg;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class GetFlowProgramFromReleaseVisitor {
  
  private final AssetClient assetClient;
  private final String flowId;
  
  public WrenchFlow visit(SysConfigRelease release) {
    final var asset = findFormAsset(release);

    if(asset.isEmpty()) {
      throw new ExecutorException(ErrorMsg.builder()
          .withCode("FLOW_NOT_FOUND_FROM_RELEASE")
          .withProps(JsonObject.of("flowId", flowId, "release", release.getId(), "releaseName", release.getName()))
          .withMessage("Can't find flow from release!")
          .toString());
    }
    
    return createProgramWrapper(asset.get(), release, flowId);
  }
  
  private Optional<SysConfigAsset> findFormAsset(SysConfigRelease release) {
    return release.getAssets().stream()
      .filter(asset -> asset.getBodyType() == AssetType.WRENCH)
      .findFirst();
  } 
  

  private WrenchFlow createProgramWrapper(SysConfigAsset wrenchAssets, SysConfigRelease release, String flowId) {
    try {
      final Map<AstBodyType, Integer> order = Map.of(
          AstBodyType.DT, 1,
          AstBodyType.FLOW_TASK, 2,
          AstBodyType.FLOW, 3);

      final AstTag tag = assetClient.getConfig().getHdes().mapper().parseJson(wrenchAssets.getBody(), AstTag.class);
      
      final var assets = new ArrayList<>(tag.getValues());
      assets.sort((AstTagValue o1, AstTagValue o2) -> Integer.compare(order.get(o1.getBodyType()), order.get(o2.getBodyType())));
      
      final var builder = assetClient.getConfig().getHdes().envir();
      for(final var asset : assets) {
        final var id = asset.getId() == null ? UUID.randomUUID().toString() : asset.getId();
        final var entity = ImmutableStoreEntity.builder().id(id).hash(asset.getHash()).body(asset.getCommands()).bodyType(asset.getBodyType()).build();
        switch (asset.getBodyType()) {
        case FLOW:
          builder.addCommand().flow(entity).id(id).build();            
          break;
        case DT:
          builder.addCommand().decision(entity).id(id).build();            
          break;
        case FLOW_TASK:
          builder.addCommand().service(entity).id(id).build();            
          break;
        default: continue;
        }
      }
      final var envir = builder.build();
      final var flow = envir.getFlowsByName().get(flowId);
      
      return ImmutableWrenchFlow.builder().envir(envir).flow(flow.getProgram().get()).build();
    } catch(Exception e) {
      throw new ExecutorException(ErrorMsg.builder()
          .withCode("FLOW_COMPILING_FAILED_FROM_RELEASE")
          .withProps(JsonObject.of("flowId", flowId, "release", release.getId(), "releaseName", release.getName()))
          .withMessage("Can't compile dialob form from release, because of: " + e.getMessage() + "!")
          .toString(), e);
    }

  }
}
