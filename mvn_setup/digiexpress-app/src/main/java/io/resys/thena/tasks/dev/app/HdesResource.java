package io.resys.thena.tasks.dev.app;

import java.time.Instant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.HdesComposer;
import io.resys.hdes.client.api.HdesComposer.ComposerEntity;
import io.resys.hdes.client.api.HdesComposer.ComposerState;
import io.resys.hdes.client.api.HdesComposer.CopyAs;
import io.resys.hdes.client.api.HdesComposer.CreateEntity;
import io.resys.hdes.client.api.HdesComposer.DebugRequest;
import io.resys.hdes.client.api.HdesComposer.DebugResponse;
import io.resys.hdes.client.api.HdesComposer.StoreDump;
import io.resys.hdes.client.api.HdesComposer.UpdateEntity;
import io.resys.hdes.client.api.HdesStore.HistoryEntity;
import io.resys.hdes.client.api.ast.AstTag;
import io.resys.hdes.client.spi.HdesComposerImpl;
import io.resys.thena.projects.client.api.ProjectClient;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfig;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfigType;
import io.resys.thena.structures.doc.actions.DocObjectsQueryImpl;
import io.resys.thena.tasks.dev.app.user.CurrentTenant;
import io.resys.thena.tasks.dev.app.user.CurrentUser;
import io.smallrye.mutiny.Uni;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@Path("q/digiexpress/api/hdes")
public class HdesResource {
  @Inject CurrentTenant currentTenant;
  @Inject CurrentUser currentUser;
  @Inject ProjectClient tenantClient;
  @Inject HdesClient hdesClient;
  @Inject ObjectMapper objectMapper;


  @GET @Path("dataModels") @Produces(MediaType.APPLICATION_JSON)
  public Uni<ComposerState> dataModels() {
    return getComposer().onItem().transformToUni(composer -> composer.get());
  }

  @GET @Path("exports") @Produces(MediaType.APPLICATION_JSON)
  public Uni<StoreDump> exports() {
    return getComposer().onItem().transformToUni(composer -> composer.getStoreDump());
  }

  @POST @Path("commands") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  public Uni<ComposerEntity<?>> commands(String body) throws JsonMappingException, JsonProcessingException {
    final var command = objectMapper.readValue(body, UpdateEntity.class);
    return getComposer().onItem().transformToUni(composer -> composer.dryRun(command));
  }

  @POST @Path("debugs") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  public Uni<DebugResponse> debug(DebugRequest debug) {
    return getComposer().onItem().transformToUni(composer -> composer.debug(debug));
  }

  @POST @Path("importTag") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  public Uni<ComposerState> importTag(AstTag entity) {
    return getComposer().onItem().transformToUni(composer -> composer.importTag(entity));
  }

  @POST @Path("resources") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  public Uni<ComposerState> create(CreateEntity entity) {
    return getComposer().onItem().transformToUni(composer -> composer.create(entity));
  }

  @PUT @Path("resources") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  public Uni<ComposerState> update(UpdateEntity entity) {
    return getComposer().onItem().transformToUni(composer -> composer.update(entity));
  }

  @DELETE @Path("resources/{id}") @Produces(MediaType.APPLICATION_JSON)
  public Uni<ComposerState> delete(@PathParam("id") String id) {
    return getComposer().onItem().transformToUni(composer -> composer.delete(id));
  }

  @GET @Path("/resources/{id}") @Produces(MediaType.APPLICATION_JSON)
  public Uni<ComposerEntity<?>> get(@PathParam("id") String id) {
    return getComposer().onItem().transformToUni(composer -> composer.get(id));
  }

  @POST @Path("copyas") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  public Uni<ComposerState> copyAs(@RequestBody CopyAs entity) {
    return getComposer().onItem().transformToUni(composer -> composer.copyAs(entity));
  }

  @GET @Path("history/{id}") @Produces(MediaType.APPLICATION_JSON)
  public Uni<HistoryEntity> history(@PathParam("id") String id) {
    return getComposer().onItem().transformToUni(composer -> composer.getHistory(id));
  }

  @GET @Path("version") @Produces(MediaType.APPLICATION_JSON)
  public VersionEntity version() {
    return new VersionEntity("", Instant.now().toString());
  }
  
  private Uni<HdesComposer> getComposer() {
    return getConfig().onItem().transform(config -> 
      new HdesComposerImpl(hdesClient.withRepo(config.getRepoId(), DocObjectsQueryImpl.BRANCH_MAIN))
    );
  }
  private Uni<TenantRepoConfig> getConfig() {
    return tenantClient.queryActiveTenantConfig().get(currentTenant.tenantId())
    .onItem().transform(config -> {
      final var dialobConfig = config.getRepoConfigs().stream().filter(entry -> entry.getRepoType() == TenantRepoConfigType.WRENCH).findFirst().get();
      return dialobConfig;
    });
  }

  
  @Data
  @RequiredArgsConstructor
  public static class VersionEntity {
    private final String version;
    private final String built;
  }

}
