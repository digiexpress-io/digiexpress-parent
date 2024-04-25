package io.resys.thena.tasks.dev.app;

import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.resys.thena.projects.client.api.ProjectClient;
import io.resys.thena.projects.client.api.model.TenantConfig;
import io.resys.thena.tasks.dev.app.user.CurrentTenant;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.WebClient;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.UriBuilder;

@ApplicationScoped
@RouteBase(path = "/portal")
public class IndexPageProxy {

  @Inject
  Vertx vertx;

  WebClient client;

  @Inject
  ProjectClient tenantClient;

  @ConfigProperty(name = "digiexpress.assets-url", defaultValue = "http://localhost:3000")
  String assetsUrl;

  @ConfigProperty(name = "digiexpress.index-page", defaultValue = "/")
  String indexPage;

  @Inject
  CurrentTenant currentTenant;

  @PostConstruct
  void postConstruct() {
    this.client = WebClient.create(vertx);
  }

  private Uni<URL> getAssetsBaseUrl() {
    return getTenantConfig()
      .onItem().transform(config -> StringUtils.trimToNull(config.getPreferences().getLandingApp()))
      .onItem().transform(Unchecked.function(uri -> UriBuilder.fromUri(uri).build().toURL()))
      .onFailure().recoverWithItem(Unchecked.supplier(() -> UriBuilder.fromUri(assetsUrl).build().toURL()));
  }

  private Uni<TenantConfig> getTenantConfig() {
    return tenantClient
      .queryActiveTenantConfig()
      .get(currentTenant.tenantId());
  }

  // TODO Baking fixed urls into root page would be better option...
  @Route(path = "/static/*", methods = Route.HttpMethod.GET)
  @Route(path = "favicon.*", methods = Route.HttpMethod.GET)
  // Needs to be blocking, because redirect terminates response and return type must be void.
  // But url it fetched asynchronously
  @Blocking
  void redirectToStaticAssets(io.vertx.ext.web.RoutingContext rc) {
    io.vertx.mutiny.ext.web.RoutingContext routingContext = new io.vertx.mutiny.ext.web.RoutingContext(rc);
    getAssetsBaseUrl()
      .map(redirectUrl -> redirectUrl + routingContext.normalizedPath())
      .map(routingContext::redirectAndForget).await().indefinitely();
  }

  @Route(path = "/", methods = Route.HttpMethod.GET, produces = "text/html")  // Overrides default static resource
  @Route(path = "/*", methods = Route.HttpMethod.GET, produces = "text/html", order = Integer.MAX_VALUE - 1)
    // Last..
  Uni<io.vertx.mutiny.core.buffer.Buffer> index(io.vertx.ext.web.RoutingContext rc) {
    rc.response().putHeader("Cache-Control", "no-cache");
    // proxy page from remote source
    return getAssetsBaseUrl()
      .map(url -> StringUtils.appendIfMissing(url.toExternalForm(), indexPage, ".html"))
      .flatMap(uri -> client.getAbs(uri).send())
      .map(response -> {
        int statusCode = response.statusCode();
        rc.response().setStatusCode(statusCode);
        rc.response().putHeader("Content-Type", response.headers().get("Content-Type"));
        if (statusCode >= 200 && statusCode <= 299) {
          return response.bodyAsBuffer();
        }
        return Buffer.buffer();
      });
  }
}
