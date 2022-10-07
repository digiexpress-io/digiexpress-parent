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

import io.digiexpress.spring.composer.config.FileConfig;
import io.digiexpress.spring.composer.config.FileConfigBean;
import io.digiexpress.spring.composer.config.PgConfig;
import io.digiexpress.spring.composer.config.PgConfigBean;
import io.digiexpress.spring.composer.config.UiConfigBean;
import io.digiexpress.spring.composer.controllers.DigiexpressComposerServiceController;
import io.digiexpress.spring.composer.controllers.DigiexpressComposerUiController;
import io.digiexpress.spring.composer.controllers.DigiexpressComposerUiRedirectController;
import io.digiexpress.spring.composer.controllers.util.ControllerUtil;
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
      ObjectMapper objectMapper, ApplicationContext ctx) {
    return new DigiexpressComposerServiceController(objectMapper, ctx);
  }
  @Bean
  public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
    log.info("Digiexpress, Composer Jackson Modules: UP");
    return builder -> builder.modules(new GuavaModule(), new JavaTimeModule(), new Jdk8Module());
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
