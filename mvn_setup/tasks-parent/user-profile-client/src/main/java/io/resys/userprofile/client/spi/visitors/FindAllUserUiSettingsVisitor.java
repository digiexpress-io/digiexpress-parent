package io.resys.userprofile.client.spi.visitors;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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
import io.resys.userprofile.client.api.UserProfileClient.UserProfileNotFoundException;
import io.resys.userprofile.client.api.model.ImmutableUiSettings;
import io.resys.userprofile.client.api.model.UiSettings;
import io.resys.userprofile.client.spi.support.DataConstants;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class FindAllUserUiSettingsVisitor implements DocObjectsVisitor<List<UiSettings>>{
  private final String id;
  
  @Override
  public Uni<QueryEnvelope<DocTenantObjects>> start(ThenaDocConfig config, DocObjectsQuery query) {
    return query.docType(DataConstants.DOC_TYPE_USER_PROFILE_SETTINGS).parentId(id).findAll();
  }
  @Override
  public DocTenantObjects visitEnvelope(ThenaDocConfig config, QueryEnvelope<DocTenantObjects> envelope) {
    if(envelope.isNotFound()) {
      throw new UserProfileNotFoundException("Can't find ui settings for profile: " + id + "!");
    }
    
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocStoreException.builder("GET_UI_SETTINGS_BY_USER_PROFILE_ID_FAIL")
        .add(config, envelope)
        .add((callback) -> callback.addArgs(id))
        .build();
    }
    final var result = envelope.getObjects();
    if(result == null) {
      throw DocStoreException.builder("GET_UI_SETTINGS_BY_USER_PROFILE_ID_NOT_FOUND")   
        .add(config, envelope)
        .add((callback) -> callback.addArgs(id))
        .build();
    }
    return result;
  }


  @Override
  public List<UiSettings> end(ThenaDocConfig config, DocTenantObjects ref) {
    if(ref == null) {
      return Collections.emptyList();
    }
    return ref.accept((
        Doc doc, 
        DocBranch docBranch, 
        Map<String, DocCommit> commit, 
        List<DocCommands> commands,
        List<DocCommitTree> trees) -> mapToUiSettings(docBranch)
    );
  }
  
  public static ImmutableUiSettings mapToUiSettings(DocBranch docBranch) {
    return docBranch.getValue().mapTo(ImmutableUiSettings.class);
  }
}
