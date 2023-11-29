package io.resys.thena.tasks.dev.app;

import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.resys.thena.projects.client.api.TenantConfigClient;
import io.resys.thena.projects.client.api.model.TenantConfig;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.ext.web.RoutingContext;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.UriBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URL;

@Slf4j
@ApplicationScoped
@RouteBase(path = "/portal")
public class IndexPageProxy {

  @Inject
  Vertx vertx;

  WebClient client;

  @Inject
  TenantConfigClient tenantClient;

  @ConfigProperty(name = "digiexpress.assets-url", defaultValue="http://localhost:3000")
  String assetsUrl;

  @ConfigProperty(name = "digiexpress.index-page", defaultValue="/")
  String indexPage;

  @Inject
  BeanFactory.CurrentTenant currentTenant;

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
      .get(currentTenant.getTenantId());
  }

  // TODO Baking fixed urls into root page would be better option...
  @Route(path = "/static/*", methods = Route.HttpMethod.GET)
  @Route(path = "favicon.*", methods = Route.HttpMethod.GET)
  Uni<Void> redirectToStaticAssets(RoutingContext rc) {
    io.vertx.mutiny.ext.web.RoutingContext routingContext = new io.vertx.mutiny.ext.web.RoutingContext(rc);
    return getAssetsBaseUrl()
      .onItem().transform(redirectUrl -> redirectUrl + routingContext.normalizedPath())
      .onItem().transformToUni(routingContext::redirect);
  }

  @Route(path = "/", methods = Route.HttpMethod.GET, produces = "text/html")  // Overrides default static resource
  @Route(path = "/*", methods = Route.HttpMethod.GET, produces = "text/html", order = Integer.MAX_VALUE - 1)
    // Last..
  Uni<HttpResponse<Buffer>> index(io.vertx.ext.web.RoutingContext rc) {
    var routingContext = new io.vertx.mutiny.ext.web.RoutingContext(rc);
    log.trace("GET index page for {}", routingContext.normalizedPath());
    // proxy page from remote source
    return getAssetsBaseUrl().onItem().transformToUni(url -> {
      String uri = StringUtils.appendIfMissing(url.toExternalForm(), indexPage, ".html");
      return client
        .getAbs(uri).send()
        .onItem().call(response -> {
            int statusCode = response.statusCode();
            if (statusCode >= 200 && statusCode <= 299) {
              return routingContext
                .response()
                .putHeader("Cache-Control", "no-cache")
                .putHeader("Content-Type", response.getHeader("Content-Type"))
                .setStatusCode(response.statusCode())
                .end(response.bodyAsBuffer());
            } else {
              log.error("GET root page failed: {} {}", response.statusCode(), response.statusMessage());
              return routingContext
                .response()
                .setStatusCode(500)
                .end();
            }
          }
        )
        .onFailure().invoke(t -> {
          log.error("GET root page failed", t);
        });
    });
  }
}
