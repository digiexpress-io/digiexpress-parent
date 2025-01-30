package io.digiexpress.eveli.envir.spi.actions;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.digiexpress.eveli.envir.api.EveliEnvirClient.DeploymentQuery;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeployment;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeploymentStatus;
import io.digiexpress.eveli.envir.spi.EveliEnvirStore;
import io.resys.thena.api.actions.DocQueryActions.DocObjectsQuery;
import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.DocCommands;
import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.doc.DocCommitTree;
import io.resys.thena.api.envelope.DocContainer.DocTenantObjects;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.spi.DocStoreException;
import io.resys.thena.spi.ThenaDocConfig;
import io.resys.thena.spi.ThenaDocConfig.DocObjectsVisitor;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Setter @Accessors(fluent = true)
public class DeploymentQueryImpl implements DeploymentQuery, DocObjectsVisitor<List<EveliDeployment>>{
  private final EveliEnvirStore ctx;
  private EveliDeploymentStatus status;
  
  @Override
  public Uni<List<EveliDeployment>> findAll() {
    final var config = ctx.getConfig();
    return config.accept(this);
  }  
  @Override
  public Uni<QueryEnvelope<DocTenantObjects>> start(ThenaDocConfig config, DocObjectsQuery builder) {
    if(status != null) {
      builder.subStatus(status.name());
    }
    
    return builder.docType(EveliEnvirStore.DOC_TYPE_DEPLOYMENT).findAll();
  }
  @Override
  public DocTenantObjects visitEnvelope(ThenaDocConfig config, QueryEnvelope<DocTenantObjects> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocStoreException.builder("FIND_ALL_DEPLOYMENTS_FAIL").add(config, envelope).build();
    }
    return envelope.getObjects();
  }
  @Override
  public List<EveliDeployment> end(ThenaDocConfig config, DocTenantObjects ref) {
    if(ref == null) {
      return Collections.emptyList();
    }
    return ref.accept((
        Doc doc, 
        DocBranch docBranch, 
        Map<String, DocCommit> commit, 
        List<DocCommands> commands,
        List<DocCommitTree> trees) -> EveliEnvirStore.map(doc, Optional.ofNullable(docBranch))
    );
  }
  

}
