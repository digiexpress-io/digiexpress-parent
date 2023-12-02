package io.resys.sysconfig.client.spi.visitors;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.resys.crm.client.api.model.ImmutablePerson;
import io.resys.crm.client.api.model.SysConfigCommand.ArchiveSysConfig;
import io.resys.crm.client.api.model.SysConfigCommand.ChangeSysConfigAddress;
import io.resys.crm.client.api.model.SysConfigCommand.ChangeSysConfigEmail;
import io.resys.crm.client.api.model.SysConfigCommand.ChangeSysConfigFirstName;
import io.resys.crm.client.api.model.SysConfigCommand.ChangeSysConfigLastName;
import io.resys.crm.client.api.model.SysConfigCommand.ChangeSysConfigSsn;
import io.resys.crm.client.api.model.SysConfigCommand.UpsertSuomiFiPerson;
import io.resys.crm.client.api.model.SysConfigCommand.UpsertSuomiFiRep;
import io.resys.sysconfig.client.api.model.Document.DocumentType;
import io.resys.sysconfig.client.api.model.ImmutableSysConfig;
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
    case CreateSysConfig:
      return visitCreateSysConfig((CreateSysConfig) command);
    case CreateSysConfigRelease:
      return visitUpsertSuomiFiPerson((CreateSysConfigRelease) command);
    
    throw new UpdateProjectVisitorException(String.format("Unsupported command type: %s, body: %s", command.getClass().getSimpleName(), command.toString())); 
  }
  
  
  private SysConfig visitCreateSysConfig(CreateSysConfig command) {
    final var id = ctx.getGid().getNextId(DocumentType.CUSTOMER);
    final var targetDate = requireTargetDate(command);
    
    this.current = ImmutableSysConfig.builder()
      .id(id)
      .body(ImmutablePerson.builder().from(command.getBody()).build())
      .externalId(command.getExternalId())
      .created(targetDate)
      .updated(targetDate)
      
      .addTransactions(
          ImmutableSysConfigTransaction.builder()
          .id("1")
          .addCommands(command)
          .build())
      .documentType(DocumentType.CUSTOMER)
      .build();
    visitedCommands.add(command);
    
    return this.current;
  }


  private SysConfig visitCreateSysConfigRelease(CreateSysConfigRelease command) {
    final var targetDate = requireTargetDate(command);
    if(this.current == null) {
      final var id = ctx.getGid().getNextId(DocumentType.CUSTOMER);
      this.current = ImmutableSysConfig.builder()
          .id(id)
          .body(ImmutablePerson.builder()
              .contact(command.getContact())
              .username(command.getUserName())
              .firstName(command.getFirstName())
              .lastName(command.getLastName())
              .protectionOrder(command.getProtectionOrder())
              .build())
          .externalId(command.getSysConfigId())
          .created(targetDate)
          .updated(targetDate)
          .addTransactions(
              ImmutableSysConfigTransaction.builder()
              .id("1")
              .addCommands(command)
              .build())
          .documentType(DocumentType.CUSTOMER)
          .build();
      visitedCommands.add(command);
      return this.current;
    }
    
    final var nextBody = ImmutablePerson.builder()
        .contact(command.getContact())
        .username(command.getUserName())
        .firstName(command.getFirstName())
        .lastName(command.getLastName())
        .protectionOrder(command.getProtectionOrder())
        .build();
    
    final var isBodyUpdated = !nextBody.equals(this.current.getBody());
    final var isIdUpdated = command.getSysConfigId().equals(this.current.getExternalId());
    
    if(!isBodyUpdated && !isIdUpdated) {
      return this.current;
    }
    
    this.current = this.current
        .withUpdated(requireTargetDate(command))
        .withBody(nextBody);
    visitedCommands.add(command);
    return this.current;
  }
  
  //TODO
  private SysConfig visitUpsertSuomiFiRep(UpsertSuomiFiRep command) {
    this.current = this.current
        .withUpdated(requireTargetDate(command));
    visitedCommands.add(command);
    return this.current;
  }

  //TODO
  private SysConfig visitChangeSysConfigFirstName(ChangeSysConfigFirstName command) {
    this.current = this.current
        .withBody(ImmutablePerson.builder()
            .from(this.current.getBody())
            .firstName(command.getFirstName())
            .build())
        .withUpdated(requireTargetDate(command));
    visitedCommands.add(command);
    return this.current;
  }
  
  //TODO 
  private SysConfig visitChangeSysConfigLastName(ChangeSysConfigLastName command) {
    this.current = this.current
        .withUpdated(requireTargetDate(command));
    visitedCommands.add(command);
    return this.current;
  }

  //TODO 
  private SysConfig visitChangeSysConfigSsn(ChangeSysConfigSsn command) {
    this.current = this.current
        .withUpdated(requireTargetDate(command));
    visitedCommands.add(command);
    return this.current;
  }
  
  //TODO
  private SysConfig visitChangeSysConfigEmail(ChangeSysConfigEmail command) {
    this.current = this.current
        .withUpdated(requireTargetDate(command));
    visitedCommands.add(command);
    return this.current;
  }
  
  //TODO
  private SysConfig visitChangeSysConfigAddress(ChangeSysConfigAddress command) {
    this.current = this.current
        .withUpdated(requireTargetDate(command));
    visitedCommands.add(command);
    return this.current;
  }
  
  //TODO 
  private SysConfig visitArchiveSysConfig(ArchiveSysConfig command) {
    this.current = this.current
        .withUpdated(requireTargetDate(command));
    visitedCommands.add(command);
    return this.current;
  }
  
  
  
  public static Instant requireTargetDate(SysConfigCommand command) {
    final var targetDate = command.getTargetDate();
    if (targetDate == null) {
      throw new UpdateProjectVisitorException("targetDate not defined");
    }
    return targetDate;
  }

  
  public static class NoChangesException extends Exception {
    
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
