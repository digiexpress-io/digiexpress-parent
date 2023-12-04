package io.resys.sysconfig.client.spi.visitors;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.resys.sysconfig.client.api.model.Document.DocumentType;
import io.resys.sysconfig.client.api.model.ImmutableSysConfig;
import io.resys.sysconfig.client.api.model.ImmutableSysConfigService;
import io.resys.sysconfig.client.api.model.ImmutableSysConfigTransaction;
import io.resys.sysconfig.client.api.model.SysConfig;
import io.resys.sysconfig.client.api.model.SysConfigCommand;
import io.resys.sysconfig.client.api.model.SysConfigCommand.CreateSysConfig;
import io.resys.sysconfig.client.api.model.SysConfigCommand.CreateSysConfigRelease;
import io.resys.sysconfig.client.spi.store.DocumentConfig;


public class SysConfigCommandVisitor {
  private final DocumentConfig ctx;
  private final SysConfig start;
  private final List<SysConfigCommand> visitedCommands = new ArrayList<>();
  private ImmutableSysConfig current;
  
  public SysConfigCommandVisitor(DocumentConfig ctx) {
    this.start = null;
    this.current = null;
    this.ctx = ctx;
  }
  
  public SysConfigCommandVisitor(SysConfig start, DocumentConfig ctx) {
    this.start = start;
    this.current = ImmutableSysConfig.builder().from(start).build();
    this.ctx = ctx;
  }
  
  public SysConfig visitTransaction(List<? extends SysConfigCommand> commands) throws NoChangesException {
    commands.forEach(this::visitCommand);
    
    if(visitedCommands.isEmpty()) {
      throw new NoChangesException();
    }
    
    final var transactions = new ArrayList<>(start == null ? Collections.emptyList() : start.getTransactions());
    final var id = String.valueOf(transactions.size() +1);
    transactions
      .add(ImmutableSysConfigTransaction.builder()
        .id(id)
        .commands(visitedCommands)
        .build());
    this.current = this.current.withVersion(id).withTransactions(transactions);
    return this.current;
  }
  
  private SysConfig visitCommand(SysConfigCommand command) {
    switch (command.getCommandType()) {
    case CreateSysConfig: return visitCreateSysConfig((CreateSysConfig) command);
    case CreateSysConfigRelease: return visitCreateSysConfigRelease((CreateSysConfigRelease) command); 
    }
    throw new SysConfigVisitorException(String.format("Unsupported command type: %s, body: %s", command.getClass().getSimpleName(), command.toString()));
  }
  
  private SysConfig visitCreateSysConfig(CreateSysConfig command) {
    final var id = ctx.getGid().getNextId(DocumentType.SYS_CONFIG);
    final var version = ctx.getGid().getNextVersion(DocumentType.SYS_CONFIG);
    final var targetDate = requireTargetDate(command);
    
    this.current = ImmutableSysConfig.builder()
      .id(id)
      .created(targetDate)
      .updated(targetDate)
      .version(version)
      .tenantId(command.getTenantId())
      .name(command.getName())
      .wrenchHead(command.getWrenchHead())
      .stencilHead(command.getStencilHead())
      .services(command.getServices().stream()
          .map(init -> ImmutableSysConfigService.builder().from(init)
              .id(ctx.getGid().getNextId(DocumentType.SYS_CONFIG))
              .build())
          .toList())
      .addTransactions(
          ImmutableSysConfigTransaction.builder()
          .id("1")
          .addCommands(command)
          .build())
      .build();
    visitedCommands.add(command);
    
    return this.current;
  }

  // log the release command
  private SysConfig visitCreateSysConfigRelease(CreateSysConfigRelease command) {
    this.current = this.current.withUpdated(requireTargetDate(command));
    visitedCommands.add(command);
    return this.current;
  }
  
  public static Instant requireTargetDate(SysConfigCommand command) {
    final var targetDate = command.getTargetDate();
    if (targetDate == null) {
      throw new SysConfigVisitorException("targetDate not defined");
    }
    return targetDate;
  }

  
  public static class NoChangesException extends Exception {
    private static final long serialVersionUID = -4373837491237504039L;
  }

  public static class SysConfigVisitorException extends RuntimeException {
    private static final long serialVersionUID = -1385190644836838881L;

    public SysConfigVisitorException(String message, Throwable cause) {
      super(message, cause);
    }

    public SysConfigVisitorException(String message) {
      super(message);
    }
  }
}
