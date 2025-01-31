package io.digiexpress.eveli.envir.spi;

import java.util.Optional;

import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeployment;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeploymentStatus;
import io.digiexpress.eveli.envir.api.ImmutableEveliDeployment;
import io.digiexpress.eveli.envir.api.ImmutableEveliSources;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.spi.DocStoreImpl;
import io.resys.thena.spi.ThenaDocConfig;



public class EveliEnvirStore extends DocStoreImpl<EveliEnvirStore> {

  public static String DOC_TYPE_DEPLOYMENT = "deployment";
  
  public EveliEnvirStore(ThenaDocConfig config, DocStoreFactory<EveliEnvirStore> factory) {
    super(config, factory);
  }

  public static Builder<EveliEnvirStore> builder() {
    final DocStoreFactory<EveliEnvirStore> factory = (config, delegate) -> new EveliEnvirStore(config, delegate);
    return new Builder<EveliEnvirStore>(factory);
  }
  
  @Override
  public StoreTenantQuery<EveliEnvirStore> query() {
    return super.query().repoType(StructureType.doc);
  }
  
  public static EveliDeployment map(Doc doc, Optional<DocBranch> branch) {
    return ImmutableEveliDeployment.builder()
        .id(doc.getId())
        .externalId(doc.getExternalId())
        .startsAt(doc.getStartsAt())
        .status(EveliDeploymentStatus.valueOf(doc.getSubStatus()))
        .externalId(doc.getExternalId())
        .createdAt(doc.getCreatedAt())
        .errors(doc.getMeta())
        .sources(branch
            .map(src -> src.getValue().mapTo(ImmutableEveliSources.class))
            .orElse(null))
        .build();
  }
}
