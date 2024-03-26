package io.resys.sysconfig.client.spi.executor.visitors;

import java.util.Optional;

import io.dialob.client.api.DialobClient.ProgramStatus;
import io.dialob.client.api.DialobClient.ProgramWrapper;
import io.dialob.client.api.DialobDocument.DocumentType;
import io.dialob.client.api.DialobDocument.FormDocument;
import io.dialob.client.api.ImmutableProgramWrapper;
import io.dialob.client.api.ImmutableStoreEntity;
import io.dialob.program.DialobProgram;
import io.resys.sysconfig.client.api.AssetClient;
import io.resys.sysconfig.client.api.model.SysConfigRelease;
import io.resys.sysconfig.client.api.model.SysConfigRelease.AssetType;
import io.resys.sysconfig.client.api.model.SysConfigRelease.SysConfigAsset;
import io.resys.sysconfig.client.spi.executor.exceptions.ExecutorException;
import io.resys.thena.support.ErrorMsg;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetDialobProgramFromReleaseVisitor {
  
  private final AssetClient assetClient;
  private final String dialobFormId;
  
  public ProgramWrapper visit(SysConfigRelease release) {
    final var asset = findFormAsset(release);

    if(asset.isEmpty()) {
      throw new ExecutorException(ErrorMsg.builder()
          .withCode("FORM_NOT_FOUND_FROM_RELEASE")
          .withProps(JsonObject.of("dialobFormId", dialobFormId, "release", release.getId(), "releaseName", release.getName()))
          .withMessage("Can't find dialob form from release!")
          .toString());
    }
    
    return createProgramWrapper(asset.get(), release);
  }
  
  private Optional<SysConfigAsset> findFormAsset(SysConfigRelease release) {
    return release.getAssets().stream()
      .filter(asset -> asset.getBodyType() == AssetType.DIALOB)
      .filter(asset -> asset.getId().equals(dialobFormId))
      .findFirst();
  } 
  

  private ProgramWrapper createProgramWrapper(SysConfigAsset asset, SysConfigRelease release) {
    final var src = ImmutableStoreEntity.builder()
        .bodyType(DocumentType.FORM)
        .id(asset.getId())
        .version(asset.getVersion())
        .body(asset.getBody())
        .build();
    
    final FormDocument ast;
    try {
      ast = assetClient.getConfig().getDialob().getConfig().getMapper().toFormDoc(src);
    } catch(Exception e) {
      throw new ExecutorException(ErrorMsg.builder()
          .withCode("FORM_PARSING_FAILED_FROM_RELEASE")
          .withProps(JsonObject.of("dialobFormId", dialobFormId, "dialobFormVersion", asset.getVersion(),"release", release.getId(), "releaseName", release.getName()))
          .withMessage("Can't parse dialob form from release, because of: " + e.getMessage() + "!")
          .toString(), e);
    }


    final DialobProgram program;
    try {
      program = assetClient.getConfig().getDialob().program().form(ast).build();
    } catch(Exception e) {
      throw new ExecutorException(ErrorMsg.builder()
          .withCode("FORM_COMPILING_FAILED_FROM_RELEASE")
          .withProps(JsonObject.of("dialobFormId", dialobFormId, "dialobFormVersion", asset.getVersion(),"release", release.getId(), "releaseName", release.getName()))
          .withMessage("Can't compile dialob form from release, because of: " + e.getMessage() + "!")
          .toString(), e);
    }

    return ImmutableProgramWrapper
        .builder()
        .status(ProgramStatus.UP)
        .id(src.getId())
        .document(ast)
        .program(Optional.ofNullable(program))
        .source(src)
        .build();
  
  }
}
