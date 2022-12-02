package io.digiexpress.spring.composer;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.immutables.value.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.digiexpress.client.api.ServiceClient;
import io.digiexpress.client.spi.ServiceClientImpl;
import io.digiexpress.client.spi.store.ServiceRepoBuilderImpl;
import io.digiexpress.spring.composer.config.FileConfig;
import io.digiexpress.spring.composer.config.FileConfigBean;
import io.digiexpress.spring.composer.config.PgConfig;
import io.digiexpress.spring.composer.config.PgConfigBean;
import io.digiexpress.spring.composer.config.UiConfigBean;
import io.digiexpress.spring.composer.controllers.DigiexpressComposerServiceController;
import io.digiexpress.spring.composer.controllers.DigiexpressComposerUiController;
import io.digiexpress.spring.composer.controllers.DigiexpressComposerUiRedirectController;
import io.digiexpress.spring.composer.controllers.util.ControllerUtil;
import io.resys.thena.docdb.spi.pgsql.PgErrors;
import io.resys.thena.docdb.sql.DocDBFactorySql;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import lombok.extern.slf4j.Slf4j;

@Configuration
@ConditionalOnProperty(name = "digiexpress.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties({
  FileConfigBean.class,
  PgConfigBean.class,
  UiConfigBean.class})
@Import({
  FileConfig.class, 
  PgConfig.class })
@Slf4j
public class ComposerAutoConfiguration {
  
  @org.springframework.beans.factory.annotation.Value("${server.servlet.context-path:}")
  private String contextPath;
  

  @ConditionalOnProperty(name = "digiexpress.composer.ui.enabled", havingValue = "true")
  @Bean
  public DigiexpressComposerUiController digiexpressComposerUiController(UiConfigBean composerConfig, Optional<SpringIdeTokenSupplier> token) {
    final var config = ControllerUtil.ideOnClasspath(contextPath);
    log.info("Digiexpress, UI Controller: " + config.getMainJs());
    return new DigiexpressComposerUiController(composerConfig, config, token);
  }
  @ConditionalOnProperty(name = {"digiexpress.composer.ui.enabled", "digiexpress.composer.ui.redirect"}, havingValue = "true")
  @Bean
  public DigiexpressComposerUiRedirectController digiexpressUIRedirectController(UiConfigBean composerConfig) {
    log.info("Digiexpress, UI Redirect: UP");
    return new DigiexpressComposerUiRedirectController(composerConfig);
  }
  @ConditionalOnProperty(name = "digiexpress.composer.service.enabled", havingValue = "true")
  @Bean
  public DigiexpressComposerServiceController digiexpressComposerServiceController(
      ObjectMapper objectMapper, ApplicationContext ctx, ServiceClient client) {
    return new DigiexpressComposerServiceController(objectMapper, ctx, client);
  }
  @Bean
  public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
    log.info("Digiexpress, Composer Jackson Modules: UP");
    return builder -> builder.modules(new GuavaModule(), new JavaTimeModule(), new Jdk8Module());
  }
  
  @Bean
  @ConditionalOnProperty(name = "digiexpress.db.pg.enabled", havingValue = "true", matchIfMissing = false)
  public ServiceClient digiexpressClient(ObjectMapper om, PgConfigBean config) {
      log.info(new StringBuilder()
        .append(System.lineSeparator())
        .append("Configuring Thena: ").append(System.lineSeparator())
        .append("  repoName: '").append(config.getRepositoryName()).append("'").append(System.lineSeparator())
        .append("  headName: '").append(config.getBranchSpecifier()).append("'").append(System.lineSeparator())
//        .append("  authorProvider: '").append(this.authorProvider == null ? "configuring" : "provided").append("'").append(System.lineSeparator())

        .append("  pgPoolSize: '").append(config.getPgPoolSize()).append("'").append(System.lineSeparator())
        .append("  pgHost: '").append(config.getPgHost()).append("'").append(System.lineSeparator())
        .append("  pgPort: '").append(config.getPgPort()).append("'").append(System.lineSeparator())
        .append("  pgDb: '").append(config.getPgDb()).append("'").append(System.lineSeparator())
        .append("  pgUser: '").append(config.getPgUser() == null ? "null" : "***").append("'").append(System.lineSeparator())
        .append("  pgPass: '").append(config.getPgPass() == null ? "null" : "***").append("'").append(System.lineSeparator())
        .toString());
    
    final var connectOptions = new PgConnectOptions().setDatabase(config.getPgDb())
        .setHost(config.getPgHost()).setPort(config.getPgPort())
        .setUser(config.getPgUser()).setPassword(config.getPgPass());
    final var poolOptions = new PoolOptions().setMaxSize(config.getPgPoolSize());
    final var pgPool = io.vertx.mutiny.pgclient.PgPool.pool(connectOptions, poolOptions);
    
      
    final var doc = DocDBFactorySql.create()
        .client(pgPool)
        .db(config.getRepositoryName())
        .errorHandler(new PgErrors())
        .build();
    
    final var namings = ServiceRepoBuilderImpl.Namings.builder().repoService(config.getRepositoryName()).build().withDefaults();
    
    return ServiceClientImpl.builder()
        .om(om)
        .defaultDialobEventPub()
        .defaultDialobFr()
        .defaultHdesDjc()
        .defaultHdesServiceInit()
        .repoStencil(namings.getRepoStencil())
        .repoDialob(namings.getRepoDialob())
        .repoHdes(namings.getRepoHdes())
        .repoService(namings.getRepoService())
        .doc(doc)
        .build();
  }
  
  @FunctionalInterface
  public interface SpringIdeTokenSupplier {
    Optional<IdeToken> get(HttpServletRequest request);
  }
  
  @Value.Immutable
  public interface IdeToken {
    String getKey();
    String getValue();
  }
  
}
