package io.resys.sysconfig.client.spi.visitors;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.resys.sysconfig.client.api.model.Document.DocumentType;
import io.resys.sysconfig.client.api.model.ImmutableSysConfigDeployment;
import io.resys.sysconfig.client.api.model.ImmutableSysConfigDeploymentTransaction;
import io.resys.sysconfig.client.api.model.SysConfigDeployment;
import io.resys.sysconfig.client.api.model.SysConfigDeploymentCommand;
import io.resys.sysconfig.client.api.model.SysConfigDeploymentCommand.CreateSysConfigDeployment;
import io.resys.sysconfig.client.api.model.SysConfigDeploymentCommand.UpdateSysConfigDeploymentDisabled;
import io.resys.sysconfig.client.api.model.SysConfigDeploymentCommand.UpdateSysConfigDeploymentLiveDate;
import io.resys.sysconfig.client.spi.store.DocumentConfig;


public class SysConfigDeploymentCommandVisitor {
  private final DocumentConfig ctx;
  private final SysConfigDeployment start;
  private final List<SysConfigDeploymentCommand> visitedCommands = new ArrayList<>();
  private ImmutableSysConfigDeployment current;
  
  public SysConfigDeploymentCommandVisitor(DocumentConfig ctx) {
    this.start = null;
    this.current = null;
    this.ctx = ctx;
  }
  
  public SysConfigDeploymentCommandVisitor(SysConfigDeployment start, DocumentConfig ctx) {
    this.start = start;
    this.current = ImmutableSysConfigDeployment.builder().from(start).build();
    this.ctx = ctx;
  }
  
  public SysConfigDeployment visitTransaction(List<? extends SysConfigDeploymentCommand> commands) throws NoChangesException {
    commands.forEach(this::visitCommand);
    
    if(visitedCommands.isEmpty()) {
      throw new NoChangesException();
    }
    
    final var transactions = new ArrayList<>(start == null ? Collections.emptyList() : start.getTransactions());
    final var id = String.valueOf(transactions.size() +1);
    transactions
      .add(ImmutableSysConfigDeploymentTransaction.builder()
        .id(id)
        .commands(visitedCommands)
        .build());
    this.current = this.current.withVersion(id).withTransactions(transactions);
    return this.current;
  }
  
  private SysConfigDeployment visitCommand(SysConfigDeploymentCommand command) {
    switch (command.getCommandType()) {
    case CreateDeployment: return visitCreateSysConfig((CreateSysConfigDeployment) command);
    case UpdateDeploymentDisabled: return visitUpdateDeploymentDisabled((UpdateSysConfigDeploymentDisabled) command);
    case UpdateDeploymentLiveDate: return visitUpdateDeploymentLiveDate((UpdateSysConfigDeploymentLiveDate) command);
    }
    throw new SysConfigDeploymentVisitorException(String.format("Unsupported command type: %s, body: %s", command.getClass().getSimpleName(), command.toString()));
  }
  
  private SysConfigDeployment visitCreateSysConfig(CreateSysConfigDeployment command) {
    final var id = ctx.getGid().getNextId(DocumentType.SYS_CONFIG);
    final var version = ctx.getGid().getNextVersion(DocumentType.SYS_CONFIG);
    final var targetDate = requireTargetDate(command);
    
    this.current = ImmutableSysConfigDeployment.builder()
      .id(id)
      .created(targetDate)
      .updated(targetDate)
      .liveDate(command.getLiveDate())
      .disabled(Boolean.TRUE.equals(command.getDisabled()))
      .tenantId(command.getBody().getTenantId())
      .hash("TODO::")
      .version(version)
      .addTransactions(
          ImmutableSysConfigDeploymentTransaction.builder()
          .id("1")
          .addCommands(command)
          .build())
      .build();
    visitedCommands.add(command);
    
    return this.current;
  }


  private SysConfigDeployment visitUpdateDeploymentDisabled(UpdateSysConfigDeploymentDisabled command) {
    this.current = this.current.withUpdated(requireTargetDate(command)).withDisabled(command.getDisabled());
    visitedCommands.add(command);
    return this.current;
  }
  private SysConfigDeployment visitUpdateDeploymentLiveDate(UpdateSysConfigDeploymentLiveDate command) {
    this.current = this.current.withUpdated(requireTargetDate(command)).withLiveDate(command.getLiveDate());
    visitedCommands.add(command);
    return this.current;
  }
  
  
  public static Instant requireTargetDate(SysConfigDeploymentCommand command) {
    final var targetDate = command.getTargetDate();
    if (targetDate == null) {
      throw new SysConfigDeploymentVisitorException("targetDate not defined sys config deployment");
    }
    return targetDate;
  }

  
  public static class NoChangesException extends Exception {
    private static final long serialVersionUID = -4373837491237504039L;
  }

  public static class SysConfigDeploymentVisitorException extends RuntimeException {
    private static final long serialVersionUID = -1385190644836838881L;

    public SysConfigDeploymentVisitorException(String message, Throwable cause) {
      super(message, cause);
    }

    public SysConfigDeploymentVisitorException(String message) {
      super(message);
    }
  }
}
