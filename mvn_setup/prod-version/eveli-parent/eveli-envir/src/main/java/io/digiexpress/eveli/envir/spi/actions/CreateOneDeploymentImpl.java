package io.digiexpress.eveli.envir.spi.actions;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.dialob.api.form.Form;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.CreateOneDeployment;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeployment;
import io.digiexpress.eveli.envir.spi.EveliEnvirStore;
import io.digiexpress.eveli.envir.spi.visitors.CreateUserProfileVisitor;
import io.digiexpress.eveli.envir.spi.visitors.UpdateUserProfileVisitor;
import io.resys.hdes.client.api.ast.AstTag;
import io.resys.thena.api.actions.DocQueryActions.DocObjectsQuery;
import io.resys.thena.api.envelope.DocContainer.DocTenantObjects;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.spi.ThenaDocConfig;
import io.resys.thena.spi.ThenaDocConfig.DocObjectsVisitor;
import io.resys.thena.support.RepoAssert;
import io.resys.userprofile.client.api.model.UserProfile;
import io.resys.userprofile.client.api.model.UserProfileCommand.CreateUserProfile;
import io.resys.userprofile.client.api.model.UserProfileCommand.UpsertUserProfile;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.StencilComposer.SiteState;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;


@RequiredArgsConstructor
@Setter @Accessors(fluent = true)
public class CreateOneDeploymentImpl implements CreateOneDeployment, DocObjectsVisitor<Uni<EveliDeployment>> {
  private final EveliEnvirStore ctx;
  private String name;
  private OffsetDateTime startsAt;
  private SiteState stencil;
  private AstTag wrench;
  private List<Form> dialob;
  
  @Override
  public Uni<EveliDeployment> build() {
    RepoAssert.notEmpty(name, () -> "name must be defined!");
    RepoAssert.notNull(startsAt, () -> "startsAt must be defined!");
    RepoAssert.notNull(stencil, () -> "stencil must be defined!");
    RepoAssert.notNull(wrench, () -> "wrench must be defined!");
    RepoAssert.notNull(dialob, () -> "dialob must be defined!");
    
    
    return ctx.getConfig()
        .accept(this).onItem()
        .transformToUni(item -> item)
        .onItem().transform(items -> items.get(0));
  }

  @Override
  public Uni<QueryEnvelope<DocTenantObjects>> start(ThenaDocConfig config, DocObjectsQuery builder) {
    builder.docType(EveliEnvirStore.DOC_TYPE_DEPLOYMENT).ex;
    return null;
  }

  @Override
  public DocTenantObjects visitEnvelope(ThenaDocConfig config, QueryEnvelope<DocTenantObjects> envelope) {
    // TODO Auto-generated method stub
    return null;
  }
  
  @Override
  public Uni<EveliDeployment> end(ThenaDocConfig config, DocTenantObjects ref) {
    // TODO Auto-generated method stub
    return null;
  }

}
