package io.digiexpress.client.spi.executors;

import java.time.LocalDateTime;

import io.digiexpress.client.api.ImmutableExecution;
import io.digiexpress.client.api.ServiceClient.Execution;
import io.digiexpress.client.api.ServiceClient.ServiceClientConfig;
import io.digiexpress.client.api.ServiceClient.StencilExecutor;
import io.digiexpress.client.api.ServiceEnvir;
import io.digiexpress.client.api.ServiceEnvir.ServiceProgramStencil;
import io.digiexpress.client.spi.support.ExecutorException;
import io.digiexpress.client.spi.support.ServiceAssert;
import io.thestencil.client.api.MigrationBuilder.LocalizedSite;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StencilExecutorImpl implements StencilExecutor {

  private final ServiceClientConfig config;
  private final ServiceEnvir envir;
  private LocalDateTime targetDate;
  private String locale;
  
  @Override
  public StencilExecutor targetDate(LocalDateTime targetDate) {
    ServiceAssert.notNull(targetDate, () -> "targetDate must be defined!");
    this.targetDate = targetDate;
    return this;
  }
  @Override
  public StencilExecutor locale(String locale) {
    ServiceAssert.notNull(locale, () -> "locale must be defined!");
    this.locale = locale;
    return this;
  }
  @Override
  public Execution<LocalizedSite> build() {
    final var targetDate = this.targetDate == null ? LocalDateTime.now() : this.targetDate;
    final var def = envir.getDef(targetDate).getDelegate(config);
    final var program = (ServiceProgramStencil) envir.getByRefId(def.getStencil());
    final var sites = program.getDelegate(config);
    
    if(locale == null || locale.isEmpty()) {
      final var site = sites.getSites().values().iterator().next();
      return ImmutableExecution.<LocalizedSite>builder().body(site).build();
    }
    
    final var site = sites.getSites().values().stream()
        .filter(s -> s.getLocale().equalsIgnoreCase(locale))
        .findAny().orElseThrow(() -> ExecutorException.stencilContentNotFound(() -> {
          final var candidates = String.join(",", sites.getSites().keySet());
          return "There is no content for locale: '" + locale + "'. Possible candidates: " + candidates + "!";
        }));
    return ImmutableExecution.<LocalizedSite>builder().body(site).build();
  }
}
