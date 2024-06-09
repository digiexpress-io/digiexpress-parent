package io.thestencil.client.rest;

import java.util.List;

import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.ImmutableArticleMutator;
import io.thestencil.client.api.ImmutableCreateArticle;
import io.thestencil.client.api.ImmutableCreateLink;
import io.thestencil.client.api.ImmutableCreateLocale;
import io.thestencil.client.api.ImmutableCreatePage;
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
import io.thestencil.client.api.StencilClient.Template;
import io.thestencil.client.api.StencilClient.Workflow;
import io.thestencil.client.api.StencilComposer.SiteState;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

public interface StencilRestApi {
  @POST @Path("articles") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Entity<Article>> createArticle( ImmutableCreateArticle body);
  
  @PUT @Path("articles") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Entity<Article>> updateArticle( ImmutableArticleMutator body);
  
  @DELETE @Path("articles/{id}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Entity<Article>> deleteArticle( @PathParam("id") String id);
  
  
  
  @POST @Path("migrations") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<SiteState> createMigration( String json);
  
  
  
  @POST @Path("sites") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<SiteState> createSites();
  
  @GET @Path("sites") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<SiteState> getSites();

  
  
  @POST @Path("links") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Entity<Link>> createLink( ImmutableCreateLink body);
  
  @PUT @Path("links") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Entity<Link>> updateLink( ImmutableLinkMutator body);
  
  @DELETE @Path("links/{id}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Entity<Link>> deleteLink( @PathParam("id") String id, @QueryParam("articleId") String articleId);
  
  
  
  @POST @Path("workflows") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Entity<Workflow>> createWorkflow( ImmutableCreateWorkflow body);
  
  @PUT @Path("workflows") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Entity<Workflow>> updateWorkflow( ImmutableWorkflowMutator body);
  
  @DELETE @Path("workflows/{id}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Entity<Workflow>> deleteWorkflow( @PathParam("id") String id, @QueryParam("articleId") String articleId);
  

  
  @POST @Path("locales") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Entity<Locale>> createLocale( ImmutableCreateLocale body);
  
  @PUT @Path("locales") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Entity<Locale>> updateLocale( ImmutableLocaleMutator body);
  
  @DELETE @Path("locales/{id}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Entity<Locale>> deleteLocale( @PathParam("id") String id);
  

  @POST @Path("pages") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Entity<Page>> createPage( ImmutableCreatePage body);
  
  @PUT @Path("pages") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<List<Entity<Page>>> updatePage( List<ImmutablePageMutator> body);
  
  @DELETE @Path("pages/{id}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Entity<Page>> deletePage( @PathParam("id") String id);

  
  @POST @Path("templates") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Entity<Template>> createTemplate( ImmutableCreateTemplate body);
  
  @PUT @Path("templates") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Entity<Template>> updateTemplate( ImmutableTemplateMutator body);
  
  @DELETE @Path("templates/{id}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Entity<Template>> deleteTemplate( @PathParam("id") String id);
}
