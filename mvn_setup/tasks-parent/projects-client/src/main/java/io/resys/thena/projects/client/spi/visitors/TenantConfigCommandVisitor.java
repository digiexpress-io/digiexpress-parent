package io.resys.thena.projects.client.spi.visitors;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.resys.thena.projects.client.api.model.Document.DocumentType;
import io.resys.thena.projects.client.api.model.ImmutableTenantConfig;
import io.resys.thena.projects.client.api.model.ImmutableTenantConfigTransaction;
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


public class TenantConfigCommandVisitor {
  private final DocumentConfig ctx;
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
  
  public TenantConfig visitTransaction(List<? extends TenantConfigCommand> commands) {
    commands.forEach(this::visitCommand);
    
    final var transactions = new ArrayList<>(start == null ? Collections.emptyList() : start.getTransactions());
    final var id = String.valueOf(transactions.size() +1);
    transactions
      .add(ImmutableTenantConfigTransaction.builder()
        .id(id)
        .commands(visitedCommands)
        .build());
    this.current = this.current.withVersion(id).withTransactions(transactions);
    return this.current;
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
    final var targetDate = requireTargetDate(command);
    
    this.current = ImmutableTenantConfig.builder()
      .id(id)
      .name(id)
      .created(targetDate)
      .updated(targetDate)
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
      .addTransactions(
          ImmutableTenantConfigTransaction.builder()
          .id("1")
          .addCommands(command)
          .build())
      .documentType(DocumentType.TENANT_CONFIG)
      .build();
    
    return this.current;
  }

  private TenantConfig visitArchiveTenantConfig(ArchiveTenantConfig command) {
    final var targetDate = requireTargetDate(command);
    this.current = this.current
        .withArchived(targetDate)
        .withUpdated(targetDate)
        .withStatus(TenantStatus.ARCHIVED);
    return this.current;
  }


  private TenantConfig visitChangeTenantConfigInfo(ChangeTenantConfigInfo command) {
    this.current = this.current
        .withName(command.getName())
        .withUpdated(requireTargetDate(command));
    return this.current;
  }
  

  public static Instant requireTargetDate(TenantConfigCommand command) {
    final var targetDate = command.getTargetDate();
    if (targetDate == null) {
      throw new UpdateProjectVisitorException("targetDate not defined");
    }
    return targetDate;
  }
  
  
  private final String nextRepoId() {
    final var gen = ctx.getGid();
    final var id = gen.getNextId(DocumentType.TENANT_CONFIG);
    return id.substring(0, 7);
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
