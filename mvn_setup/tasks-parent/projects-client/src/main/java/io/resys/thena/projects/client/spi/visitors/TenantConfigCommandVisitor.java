package io.resys.thena.projects.client.spi.visitors;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import io.resys.thena.projects.client.api.model.ImmutableTenantConfig;
import io.resys.thena.projects.client.api.model.ImmutableTenantPreferences;
import io.resys.thena.projects.client.api.model.ImmutableTenantRepoConfig;
import io.resys.thena.projects.client.api.model.TenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfigType;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantStatus;
import io.resys.thena.projects.client.api.model.TenantConfigCommand;
import io.resys.thena.projects.client.api.model.TenantConfigCommand.ArchiveTenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfigCommand.ChangeTenantConfigInfo;
import io.resys.thena.projects.client.api.model.TenantConfigCommand.CreateTenantConfig;
import io.resys.thena.projects.client.spi.store.DocumentConfig;
import io.resys.thena.support.OidUtils;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.core.json.JsonObject;


public class TenantConfigCommandVisitor {
  
  @SuppressWarnings("unused")
  private final DocumentConfig ctx;
  @SuppressWarnings("unused")
  private final TenantConfig start;
  private final List<TenantConfigCommand> visitedCommands = new ArrayList<>();
  private ImmutableTenantConfig current;
  
  public TenantConfigCommandVisitor(DocumentConfig ctx) {
    this.start = null;
    this.current = null;
    this.ctx = ctx;
  }
  
  public TenantConfigCommandVisitor(TenantConfig start, DocumentConfig ctx) {
    this.start = start;
    this.current = ImmutableTenantConfig.builder().from(start).build();
    this.ctx = ctx;
  }
  
  public Tuple2<TenantConfig, List<JsonObject>> visitTransaction(List<? extends TenantConfigCommand> commands) {
    commands.forEach(this::visitCommand);
    return Tuple2.of(this.current, this.visitedCommands.stream()
        .map(JsonObject::mapFrom)
        .toList());
  }
  
  private TenantConfig visitCommand(TenantConfigCommand command) {
    visitedCommands.add(command);
    switch (command.getCommandType()) {
      case ChangeTenantConfigInfo:
        return visitChangeTenantConfigInfo((ChangeTenantConfigInfo) command);
      case ArchiveTenantConfig:
        return visitArchiveTenantConfig((ArchiveTenantConfig) command);
      case CreateTenantConfig:
        return visitCreateTenantConfig((CreateTenantConfig)command);
    }
    throw new UpdateProjectVisitorException(String.format("Unsupported command type: %s, body: %s", command.getClass().getSimpleName(), command.toString()));
  }
  
  
  private TenantConfig visitCreateTenantConfig(CreateTenantConfig command) {
    final var id = command.getName();
    this.current = ImmutableTenantConfig.builder()
      .id(id).name(id)
      .status(TenantStatus.IN_FORCE)
      .preferences(ImmutableTenantPreferences.builder()
          .landingApp(TenantConfig.APP_BACKOFFICE)
          .build())
      .addRepoConfigs(ImmutableTenantRepoConfig.builder()
          .repoId(nextRepoId())
          .repoType(TenantRepoConfigType.TASKS)
          .build())
      .addRepoConfigs(ImmutableTenantRepoConfig.builder()
          .repoId(nextRepoId())
          .repoType(TenantRepoConfigType.CRM)
          .build())
      .addRepoConfigs(ImmutableTenantRepoConfig.builder()
          .repoId(nextRepoId())
          .repoType(TenantRepoConfigType.STENCIL)
          .build())
      .addRepoConfigs(ImmutableTenantRepoConfig.builder()
          .repoId(nextRepoId())
          .repoType(TenantRepoConfigType.WRENCH)
          .build())
      .addRepoConfigs(ImmutableTenantRepoConfig.builder()
          .repoId(nextRepoId())
          .repoType(TenantRepoConfigType.DIALOB)
          .build())
      .addRepoConfigs(ImmutableTenantRepoConfig.builder()
          .repoId(nextRepoId())
          .repoType(TenantRepoConfigType.SYS_CONFIG)
          .build())
      .addRepoConfigs(ImmutableTenantRepoConfig.builder()
          .repoId(nextRepoId())
          .repoType(TenantRepoConfigType.USER_PROFILE)
          .build())
      .addRepoConfigs(ImmutableTenantRepoConfig.builder()
          .repoId(nextRepoId())
          .repoType(TenantRepoConfigType.PERMISSIONS)
          .build())
      .build();
    
    return this.current;
  }
  private final String nextRepoId() {
    final var gen = OidUtils.gen();
    return gen.substring(0, 7);
  }

  private TenantConfig visitArchiveTenantConfig(ArchiveTenantConfig command) {
    this.current = this.current.withArchived(Instant.now()).withStatus(TenantStatus.ARCHIVED);
    return this.current;
  }
  private TenantConfig visitChangeTenantConfigInfo(ChangeTenantConfigInfo command) {
    this.current = this.current.withName(command.getName());
    return this.current;
  }
  public static class UpdateProjectVisitorException extends RuntimeException {

    private static final long serialVersionUID = -1385190644836838881L;

    public UpdateProjectVisitorException(String message, Throwable cause) {
      super(message, cause);
    }

    public UpdateProjectVisitorException(String message) {
      super(message);
    }
  }
}
