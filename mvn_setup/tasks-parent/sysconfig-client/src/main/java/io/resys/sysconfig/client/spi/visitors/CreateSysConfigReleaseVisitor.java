package io.resys.sysconfig.client.spi.visitors;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import io.resys.sysconfig.client.api.AssetClient;
import io.resys.sysconfig.client.api.model.Document;
import io.resys.sysconfig.client.api.model.ImmutableSysConfig;
import io.resys.sysconfig.client.api.model.ImmutableSysConfigAsset;
import io.resys.sysconfig.client.api.model.ImmutableSysConfigRelease;
import io.resys.sysconfig.client.api.model.SysConfig;
import io.resys.sysconfig.client.api.model.SysConfigCommand.CreateSysConfigRelease;
import io.resys.sysconfig.client.api.model.SysConfigRelease;
import io.resys.sysconfig.client.api.model.SysConfigRelease.AssetType;
import io.resys.sysconfig.client.spi.SysConfigStore;
import io.resys.thena.api.actions.DocCommitActions.CreateOneDoc;
import io.resys.thena.api.actions.DocCommitActions.ModifyOneDocBranch;
import io.resys.thena.api.actions.DocQueryActions.DocObjectsQuery;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.DocCommands;
import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.doc.DocCommitTree;
import io.resys.thena.api.entities.doc.DocLog;
import io.resys.thena.api.envelope.DocContainer.DocObject;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.spi.DocStoreException;
import io.resys.thena.spi.ThenaDocConfig;
import io.resys.thena.spi.ThenaDocConfig.DocObjectVisitor;
import io.resys.thena.support.OidUtils;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;


public class CreateSysConfigReleaseVisitor implements DocObjectVisitor<Uni<SysConfigRelease>> {
  private final AssetClient assetClient;
  private final SysConfigStore ctx;
  private final ModifyOneDocBranch updateBuilder;
  private final CreateOneDoc createBuilder;
  private final CreateSysConfigRelease command;
  
  public CreateSysConfigReleaseVisitor(CreateSysConfigRelease command, SysConfigStore ctx, AssetClient assetClient) {
    super();

    this.ctx = ctx;
    this.command = command;
    this.assetClient = assetClient;
    
    final var config = ctx.getConfig();
    this.updateBuilder = config.getClient().doc(config.getRepoId()).commit().modifyOneBranch()
        .commitMessage("Update config for release")
        .commitAuthor(config.getAuthor().get());
    this.createBuilder = config.getClient().doc(config.getRepoId()).commit().createOneDoc()
        .docType(Document.DocumentType.SYS_CONFIG_RELEASE.name())
        .commitMessage("Create new release")
        .commitAuthor(config.getAuthor().get());
  }

  @Override
  public Uni<QueryEnvelope<DocObject>> start(ThenaDocConfig config, DocObjectsQuery builder) {
    return builder.get(command.getId());
  }

  @Override
  public DocObject visitEnvelope(ThenaDocConfig config, QueryEnvelope<DocObject> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocStoreException.builder("GET_SYS_CONFIG_FOR_CREATING_RELEASE_FAIL")
        .add(config, envelope)
        .add((callback) -> callback.addArgs(JsonObject.mapFrom(command).encode()))
        .build();
    }
    final var result = envelope.getObjects();
    if(result == null || result.getDoc() == null) {
      throw DocStoreException.builder("GET_SYS_CONFIG_FOR_CREATING_RELEASE_NOT_FOUND")   
        .add(config, envelope)
        .add((callback) -> callback.addArgs(JsonObject.mapFrom(command).encode()))
        .build();
    }
    return result;
  }

  @Override
  public Uni<SysConfigRelease> end(ThenaDocConfig config, DocObject blob) {
    final var sysConfig = blob.accept((
        Doc doc, 
        DocBranch docBranch, 
        Map<String, DocCommit> commit, 
        List<DocCommands> commands,
        List<DocCommitTree> trees
   ) -> docBranch.getValue().mapTo(ImmutableSysConfig.class)).stream().findFirst().get();
  
    return assetClient.withTenantConfig(sysConfig.getTenantId())
        .onItem().transformToUni(client -> doInAssetClient(client, sysConfig))
        .onItem().transformToUni(entity -> {
          final var json = JsonObject.mapFrom(entity);
          return createBuilder.append(json).docId(entity.getId()).build().onItem().transform(envelope -> {
            if(envelope.getStatus() == CommitResultStatus.OK) {
              return ImmutableSysConfigRelease.builder().from(entity).version(envelope.getBranch().getCommitId()).build();
            }
            throw new DocumentStoreException("CREATE_SYS_CONFIG_RELEASE_FAIL", DocumentStoreException.convertMessages(envelope));
          });
        });
  }


  private Uni<SysConfigRelease> doInAssetClient(AssetClient client, SysConfig config) {
    final var dialobs = config.getServices().stream().map(e -> e.getFormId()).toList();

    return Uni.combine().all().unis(
        client.assetQuery().getDialobAssets(dialobs),
        client.assetQuery().getStencilAsset(config.getStencilHead()),
        client.assetQuery().getWrenchAsset(config.getWrenchHead())
    ).asTuple().onItem().transform(tuple -> {
      
      
      
      final SysConfigRelease release = ImmutableSysConfigRelease.builder()
          .id(OidUtils.gen())
          .name(command.getReleaseName())
          .author(command.getUserId())
          .created(command.getTargetDate())
          .scheduledAt(command.getScheduledAt())
          .tenantId(config.getTenantId())
          
          .services(config.getServices())
          
          .addAllAssets(tuple.getItem1().stream().map(dialob -> ImmutableSysConfigAsset.builder()
              .bodyType(AssetType.DIALOB)
              .body(dialob.getAssetBody())
              .name(dialob.getName())
              .id(dialob.getId())
              .version(dialob.getVersion())
              .updated(Instant.now())
              .build()).toList()
          )
          .addAssets(ImmutableSysConfigAsset.builder()
              .bodyType(AssetType.STENCIL)
              .body(tuple.getItem2().getAssetBody())
              .name(tuple.getItem2().getName())
              .id(tuple.getItem2().getId())
              .version(tuple.getItem2().getVersion())
              .updated(Instant.now())
              .build())
          .addAssets(ImmutableSysConfigAsset.builder()
              .bodyType(AssetType.WRENCH)
              .body(tuple.getItem3().getAssetBody())
              .name(tuple.getItem3().getName())
              .id(tuple.getItem3().getId())
              .version(tuple.getItem3().getVersion())
              .updated(Instant.now())
              .build())          
          .build();
      
      return release;
    });
    
  }
}
