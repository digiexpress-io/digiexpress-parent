package io.resys.thena.projects.client.spi.visitors;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.resys.thena.projects.client.api.model.Document.DocumentType;
import io.resys.thena.projects.client.api.model.ImmutableProject;
import io.resys.thena.projects.client.api.model.ImmutableProjectTransaction;
import io.resys.thena.projects.client.api.model.ImmutableTenantConfig;
import io.resys.thena.projects.client.api.model.ImmutableTenantConfigTransaction;
import io.resys.thena.projects.client.api.model.TenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfigCommand;
import io.resys.thena.projects.client.api.model.TenantConfigCommand.ArchiveProject;
import io.resys.thena.projects.client.api.model.TenantConfigCommand.ArchiveTenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfigCommand.AssignProjectUsers;
import io.resys.thena.projects.client.api.model.TenantConfigCommand.ChangeProjectInfo;
import io.resys.thena.projects.client.api.model.TenantConfigCommand.ChangeTenantConfigInfo;
import io.resys.thena.projects.client.api.model.TenantConfigCommand.CreateProject;
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
  
  
  private TenantConfig visitCreateTenantConfig(CreateProject command) {
    final var gen = ctx.getGid();
    final var id = gen.getNextId(DocumentType.PROJECT_META);
    final var repoId = id.substring(0, 7);
    final var targetDate = requireTargetDate(command);
    this.current = ImmutableProject.builder()
        .id(id)
        .version(gen.getNextVersion(DocumentType.PROJECT_META))
        .users(command.getUsers().stream().distinct().toList())
        .repoType(command.getRepoType())
        .repoId(repoId)        
        .title(command.getTitle())
        .description(command.getDescription())
        .created(targetDate)
        .updated(targetDate)
        .addTransactions(ImmutableProjectTransaction.builder().id(String.valueOf(1)).addCommands(command).build())
        .build();
    return this.current;
  }

  private TenantConfig visitArchiveTenantConfig(ArchiveProject command) {
    final var targetDate = requireTargetDate(command);
    this.current = this.current
        .withArchived(targetDate)
        .withUpdated(targetDate);
    return this.current;
  }

  private TenantConfig visitAssignProject(AssignProjectUsers command) {
    this.current = this.current
        .withUsers(command.getUsers().stream().distinct().sorted().toList())
        .withUpdated(requireTargetDate(command));
    return this.current;
  }

  private TenantConfig visitChangeTenantConfigInfo(ChangeProjectInfo command) {
    this.current = this.current
        .withTitle(command.getTitle())
        .withDescription(command.getDescription())
        .withUpdated(requireTargetDate(command));
    return this.current;
  }
  

  public static Instant requireTargetDate(TenantConfigCommand command) {
    final var targetDate = command.getTargetDate();
    if (targetDate == null) {
      throw new UpdateProjectVisitorException("targetDate not found");
    }
    return targetDate;
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
