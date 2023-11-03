package io.thestencil.client.rest;

import java.util.List;

import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.ImmutableArticleMutator;
import io.thestencil.client.api.ImmutableCreateArticle;
import io.thestencil.client.api.ImmutableCreateLink;
import io.thestencil.client.api.ImmutableCreateLocale;
import io.thestencil.client.api.ImmutableCreatePage;
import io.thestencil.client.api.ImmutableCreateRelease;
import io.thestencil.client.api.ImmutableCreateTemplate;
import io.thestencil.client.api.ImmutableCreateWorkflow;
import io.thestencil.client.api.ImmutableLinkMutator;
import io.thestencil.client.api.ImmutableLocaleMutator;
import io.thestencil.client.api.ImmutablePageMutator;
import io.thestencil.client.api.ImmutableTemplateMutator;
import io.thestencil.client.api.ImmutableWorkflowMutator;
import io.thestencil.client.api.StencilClient.Article;
import io.thestencil.client.api.StencilClient.Entity;
import io.thestencil.client.api.StencilClient.Link;
import io.thestencil.client.api.StencilClient.Locale;
import io.thestencil.client.api.StencilClient.Page;
import io.thestencil.client.api.StencilClient.Release;
import io.thestencil.client.api.StencilClient.Template;
import io.thestencil.client.api.StencilClient.Workflow;
import io.thestencil.client.api.StencilComposer.SiteState;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

public interface StencilRestApi {

  public static final String PROJECT_ID = "Project-ID";
  
  @POST @Path("articles") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Entity<Article>> createArticle(@HeaderParam(PROJECT_ID) String projectId, ImmutableCreateArticle body);
  
  @PUT @Path("articles") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Entity<Article>> updateArticle(@HeaderParam(PROJECT_ID) String projectId, ImmutableArticleMutator body);
  
  @DELETE @Path("articles/{id}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Entity<Article>> deleteArticle(@HeaderParam(PROJECT_ID) String projectId, @PathParam("id") String id);
  
  
  
  @POST @Path("migrations") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<SiteState> createMigration(@HeaderParam(PROJECT_ID) String projectId, String json);
  
  
  
  @POST @Path("sites") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<SiteState> createSites(@HeaderParam(PROJECT_ID) String projectId);
  
  @GET @Path("sites") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<SiteState> getSites(@HeaderParam(PROJECT_ID) String projectId);

  
  
  @POST @Path("links") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Entity<Link>> createLink(@HeaderParam(PROJECT_ID) String projectId, ImmutableCreateLink body);
  
  @PUT @Path("links") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Entity<Link>> updateLink(@HeaderParam(PROJECT_ID) String projectId, ImmutableLinkMutator body);
  
  @DELETE @Path("links/{id}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Entity<Link>> deleteLink(@HeaderParam(PROJECT_ID) String projectId, @PathParam("id") String id, @QueryParam("articleId") String articleId);
  
  
  
  @POST @Path("workflows") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Entity<Workflow>> createWorkflow(@HeaderParam(PROJECT_ID) String projectId, ImmutableCreateWorkflow body);
  
  @PUT @Path("workflows") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Entity<Workflow>> updateWorkflow(@HeaderParam(PROJECT_ID) String projectId, ImmutableWorkflowMutator body);
  
  @DELETE @Path("workflows/{id}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Entity<Workflow>> deleteWorkflow(@HeaderParam(PROJECT_ID) String projectId, @PathParam("id") String id, @QueryParam("articleId") String articleId);
  

  
  @POST @Path("locales") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Entity<Locale>> createLocale(@HeaderParam(PROJECT_ID) String projectId, ImmutableCreateLocale body);
  
  @PUT @Path("locales") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Entity<Locale>> updateLocale(@HeaderParam(PROJECT_ID) String projectId, ImmutableLocaleMutator body);
  
  @DELETE @Path("locales/{id}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Entity<Locale>> deleteLocale(@HeaderParam(PROJECT_ID) String projectId, @PathParam("id") String id);
  

  @POST @Path("pages") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Entity<Page>> createPage(@HeaderParam(PROJECT_ID) String projectId, ImmutableCreatePage body);
  
  @PUT @Path("pages") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<List<Entity<Page>>> updatePage(@HeaderParam(PROJECT_ID) String projectId, List<ImmutablePageMutator> body);
  
  @DELETE @Path("pages/{id}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Entity<Page>> deletePage(@HeaderParam(PROJECT_ID) String projectId, @PathParam("id") String id);

  
  @POST @Path("templates") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Entity<Template>> createTemplate(@HeaderParam(PROJECT_ID) String projectId, ImmutableCreateTemplate body);
  
  @PUT @Path("templates") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Entity<Template>> updateTemplate(@HeaderParam(PROJECT_ID) String projectId, ImmutableTemplateMutator body);
  
  @DELETE @Path("templates/{id}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Entity<Template>> deleteTemplate(@HeaderParam(PROJECT_ID) String projectId, @PathParam("id") String id);
  
  
  @POST @Path("releases") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Entity<Release>> createRelease(@HeaderParam(PROJECT_ID) String projectId, ImmutableCreateRelease body);
  
  @GET @Path("releases/{id}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<SiteState> getRelease(@HeaderParam(PROJECT_ID) String projectId, @PathParam("id") String id);
  
  @DELETE @Path("releases/{id}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Entity<Release>> deleteRelease(@HeaderParam(PROJECT_ID) String projectId, @PathParam("id") String id);
}
