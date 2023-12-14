package io.resys.thena.tasks.dev.app;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.dialob.api.form.Form;
import io.dialob.api.form.FormPutResponse;
import io.dialob.api.questionnaire.Questionnaire;
import io.dialob.client.api.DialobClient;
import io.dialob.client.api.DialobComposer;
import io.dialob.client.api.DialobComposer.ComposerDocumentState;
import io.dialob.client.api.DialobDocument.FormDocument;
import io.dialob.client.api.DialobDocument.FormRevisionDocument;
import io.dialob.client.api.ImmutableFormDocument;
import io.dialob.client.spi.DialobComposerImpl;
import io.resys.thena.projects.client.api.TenantConfigClient;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfig;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfigType;
import io.resys.thena.projects.client.spi.store.MainBranch;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.jackson.Jacksonized;

@Path("q/digiexpress/api/dialob/api")
public class DialobResource {
  
  @Inject CurrentTenant currentTenant;
  @Inject CurrentUser currentUser;
  @Inject TenantConfigClient tenantClient;
  @Inject DialobClient dialobClient;

  @lombok.Data @lombok.Builder @Jacksonized
  public static class DialobTenant {
    private String id;
    private String name;
  }
  
  @lombok.Data @lombok.Builder @Jacksonized
  public static class DialobTenantEntry {
    private String id; // FormTechnicalName; //technicalName, (resys tenant: MyTenantTestForm)
    private Form.Metadata metadata;
  }

  
  @lombok.Data @lombok.Builder @Jacksonized
  public static class DialobTenantEntryTag {
    private String id; // FormTechnicalName; //technicalName, (resys tenant: MyTenantTestForm)
    private Form.Metadata metadata;
  }

  
  
  @GET @Path("tenants") @Produces(MediaType.APPLICATION_JSON)
  public Uni<List<DialobTenant>> findAllTenants() {
    return getConfig().onItem().transform(e -> Arrays.asList(DialobTenant.builder().id(e.getRepoId()).name(e.getRepoId()).build()));
  }
  
  @GET @Path("forms") @Produces(MediaType.APPLICATION_JSON)
  public Uni<List<DialobTenantEntry>> findAllForms() {
    return getComposer().onItem().transformToUni(composer -> composer.get())
        .onItem().transform(state -> {
          final Collection<FormRevisionDocument> revisions = state.getRevs().values();
          
          return revisions.stream().map(rev -> DialobTenantEntry.builder()
            .id(rev.getName())
            .metadata(state.getForms().get(rev.getHead()).getData().getMetadata())
            .build()
          ).toList();
          
        });
  }

  @GET @Path("forms/{technicalName}") @Produces(MediaType.APPLICATION_JSON)
  public Uni<Form> getFormByTechnicalName(@PathParam("technicalName") String technicalName) {
    return getComposer().onItem().transformToUni(composer -> composer.get())
        .onItem().transform(state -> {
          final Collection<FormRevisionDocument> revisions = state.getRevs().values();
          final var revision = revisions.stream().filter(rev -> rev.getName().equals(technicalName)).findFirst();
          
          return state.getForms().get(revision.get().getHead()).getData();
        });
  }
  
  @GET @Path("forms/{docId}/tags") @Produces(MediaType.APPLICATION_JSON)
  public Uni<List<DialobTenantEntryTag>> getFormTags(@PathParam("docId") String technicalName) {
    return getComposer().onItem().transformToUni(composer -> composer.get())
        .onItem().transform(state -> {
          
          return Collections.emptyList();
        });
  }
  @GET @Path("questionnaires") @Produces(MediaType.APPLICATION_JSON)
  public Uni<List<Questionnaire>> getQuestionnaires(@QueryParam("formName") String technicalName) {
    return getComposer().onItem().transformToUni(composer -> composer.get())
        .onItem().transform(state -> {
          return Collections.emptyList();
        });
  }
  private Uni<DialobComposer> getComposer() {
    return getConfig().onItem().transform(config -> 
      new DialobComposerImpl(dialobClient.withRepo(config.getRepoId(), MainBranch.HEAD_NAME))
    );
  }
  
  private Uni<TenantRepoConfig> getConfig() {
    return tenantClient.queryActiveTenantConfig().get(currentTenant.tenantId())
    .onItem().transform(config -> {
      final var dialobConfig = config.getRepoConfigs().stream().filter(entry -> entry.getRepoType() == TenantRepoConfigType.DIALOB).findFirst().get();
      return dialobConfig;
    });
  }

  
  

  @GET @Path("edit/forms/{id}") @Produces(MediaType.APPLICATION_JSON)
  public Uni<Form> getDialobFormForEdit(@PathParam("id") String id) {
    return getComposer().onItem().transformToUni(composer -> composer.get())
        .onItem().transform(state -> {
          
          return state.getForms().get(id).getData();
        });
  }
  

  @PUT @Path("edit/forms/{id}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  public Uni<FormPutResponse> updateDialobForm(
      @PathParam("id") String id,
      @QueryParam("dryRun") Boolean dryRun,
      Form form
      ) {
    return getComposer().onItem().transformToUni(composer -> composer.get()
        .onItem().transformToUni(state -> {
          
          final FormDocument doc = state.getForms().get(id); 
          final var next = ImmutableFormDocument.builder()
              .from(doc)
              .data(form)
              .build();
          
          if(Boolean.TRUE.equals(dryRun)) {
            return composer.validate(next);
          }
          
          final Uni<ComposerDocumentState> result = composer.update(next);
          
          return result.onItem().transformToUni(updated -> composer.validate(updated.getForm()));
        }));
  }
  

  
  
  
  
  // GET api/tenants
  // GET api/forms/${dialobFormId}/tags
  
  // GET/POST api/forms
  
  // GET/DELETE api/forms/${dialobFormId}
  
  // GET: api/questionnaires/?formName=${props.technicalName}&tenantId=${props.tenantId}
}
