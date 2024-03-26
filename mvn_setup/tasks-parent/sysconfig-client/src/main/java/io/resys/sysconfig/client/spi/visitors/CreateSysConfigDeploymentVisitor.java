package io.resys.sysconfig.client.spi.visitors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.resys.sysconfig.client.api.model.Document;
import io.resys.sysconfig.client.api.model.ImmutableSysConfigDeployment;
import io.resys.sysconfig.client.api.model.SysConfigDeployment;
import io.resys.sysconfig.client.api.model.SysConfigDeploymentCommand.CreateSysConfigDeployment;
import io.resys.sysconfig.client.spi.store.DocumentConfig;
import io.resys.sysconfig.client.spi.store.DocumentConfig.DocCreateVisitor;
import io.resys.sysconfig.client.spi.store.DocumentStoreException;
import io.resys.sysconfig.client.spi.visitors.SysConfigDeploymentCommandVisitor.NoChangesException;
import io.resys.thena.api.actions.DocCommitActions.CreateManyDocs;
import io.resys.thena.api.actions.DocCommitActions.ManyDocsEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.doc.ThenaDocObject.DocBranch;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CreateSysConfigDeploymentVisitor implements DocCreateVisitor<SysConfigDeployment> {
  private final List<? extends CreateSysConfigDeployment> commands;
  private final List<SysConfigDeployment> entities = new ArrayList<SysConfigDeployment>();
  
  @Override
  public CreateManyDocs start(DocumentConfig config, CreateManyDocs builder) {
    builder
      .docType(Document.DocumentType.SYS_CONFIG_DEPLOYMENT.name())
      .author(config.getAuthor().get())
      .message("creating sys-config-deployment");
    
    for(final var command : commands) {
      try {
        final var entity = new SysConfigDeploymentCommandVisitor(config).visitTransaction(Arrays.asList(command));
        final var json = JsonObject.mapFrom(entity);
        builder.item()
          .append(json)
          .docId(entity.getId())
          .next();
        entities.add(entity);
      } catch (NoChangesException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    }
    return builder;
  }

  @Override
  public List<DocBranch> visitEnvelope(DocumentConfig config, ManyDocsEnvelope envelope) {
    if(envelope.getStatus() == CommitResultStatus.OK) {
      return envelope.getBranch();
    }
    throw new DocumentStoreException("SYS_CONFIG_DEPLOYMENT_CREATE_FAIL", DocumentStoreException.convertMessages(envelope));
  }

  @Override
  public List<SysConfigDeployment> end(DocumentConfig config, List<DocBranch> branches) {
    final Map<String, SysConfigDeployment> configsById = new HashMap<>(
        this.entities.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)));
    
    branches.forEach(branch -> {
      
      final var next = ImmutableSysConfigDeployment.builder()
          .from(configsById.get(branch.getDocId()))
          .version(branch.getCommitId())
          .build();
      
      configsById.put(next.getId(), next);
    });
    
    return Collections.unmodifiableList(new ArrayList<>(configsById.values()));
  }

}