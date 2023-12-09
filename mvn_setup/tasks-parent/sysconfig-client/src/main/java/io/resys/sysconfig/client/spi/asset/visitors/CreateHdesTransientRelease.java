package io.resys.sysconfig.client.spi.asset.visitors;

import java.time.Instant;
import java.util.Optional;

import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.HdesComposer.ComposerEntity;
import io.resys.hdes.client.api.HdesComposer.ComposerState;
import io.resys.hdes.client.api.HdesStore;
import io.resys.hdes.client.api.ImmutableComposerState;
import io.resys.hdes.client.api.ImmutableCreateEntity;
import io.resys.hdes.client.api.ast.AstBody;
import io.resys.hdes.client.api.ast.AstFlow;
import io.resys.hdes.client.spi.composer.ComposerEntityMapper;
import io.resys.hdes.client.spi.composer.CreateEntityVisitor;
import io.resys.sysconfig.client.api.AssetClient.WrenchAssetEntry;
import io.resys.sysconfig.client.api.AssetClient.WrenchAssets;
import io.resys.sysconfig.client.api.ImmutableAssetParam;
import io.resys.sysconfig.client.api.ImmutableWrenchAssetEntry;
import io.resys.sysconfig.client.api.ImmutableWrenchAssets;
import io.resys.thena.docdb.support.OidUtils;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateHdesTransientRelease {
  private final HdesClient client;
  private final HdesStore store;
  private final HdesStore.StoreState state;
  
  public WrenchAssets visit(String releaseId) {
    final var command = ImmutableCreateEntity.builder()
        .name("transient-release: " + releaseId + " at: " + Instant.now().toString())
        .desc("created on the fly")
        .type(AstBody.AstBodyType.TAG)
        .build();

    final var composerState = state(this.client, this.state);
    final var transientEntity = new CreateEntityVisitor(composerState, command, client).visit();
    final var tag = client.ast().commands(transientEntity.getBody()).tag();
    final var assetBody = client.mapper().toJson(tag); 
    
    final var entries = composerState.getFlows().values().stream()
        .map(flow -> createEntries(composerState, flow))
        .toList();
    
    return ImmutableWrenchAssets.builder()
        .id(OidUtils.gen())
        .version("")
        .created(Instant.now())
        .name(command.getName())
        .addAllFlows(entries)
        .assetBody(assetBody)
        .flows(entries)
        .build();
  }
  
  public static WrenchAssetEntry createEntries(ComposerState state, ComposerEntity<AstFlow> flow) {
    final var inputs = flow.getAst().getHeaders().getAcceptDefs().stream()
        .map(param -> ImmutableAssetParam.builder()
            .isRequired(true)
            .isInput(true)
            .name(param.getName())
            .type(param.getValueType().name())
            .getDefault(Optional.empty())
            .build())
        .toList();
    
    final var outputs = flow.getAst().getHeaders().getReturnDefs().stream()
        .map(param -> ImmutableAssetParam.builder()
            .isRequired(true)
            .isInput(false)
            .name(param.getName())
            .type(param.getValueType().name())
            .getDefault(Optional.empty())
            .build())
        .toList();
    return ImmutableWrenchAssetEntry.builder()
        .flowName(flow.getAst().getName())
        .addAllParams(inputs)
        .addAllParams(outputs)
        .build();
  }
  
  
  public static ComposerState state(HdesClient client, HdesStore.StoreState source) {
    // create envir
    final var envir = ComposerEntityMapper.toEnvir(client.envir(), source).build();
    
    // map envir
    final var builder = ImmutableComposerState.builder();
    envir.getValues().values().forEach(v -> ComposerEntityMapper.toComposer(builder, v));
    return builder.build(); 
  }
}
