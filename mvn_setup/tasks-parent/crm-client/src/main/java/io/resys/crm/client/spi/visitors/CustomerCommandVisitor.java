package io.resys.crm.client.spi.visitors;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.resys.crm.client.api.model.Customer;
import io.resys.crm.client.api.model.CustomerCommand;
import io.resys.crm.client.api.model.CustomerCommand.ArchiveCustomer;
import io.resys.crm.client.api.model.CustomerCommand.ChangeCustomerAddress;
import io.resys.crm.client.api.model.CustomerCommand.ChangeCustomerEmail;
import io.resys.crm.client.api.model.CustomerCommand.ChangeCustomerFirstName;
import io.resys.crm.client.api.model.CustomerCommand.ChangeCustomerLastName;
import io.resys.crm.client.api.model.CustomerCommand.ChangeCustomerSsn;
import io.resys.crm.client.api.model.CustomerCommand.CreateCustomer;
import io.resys.crm.client.api.model.CustomerCommand.UpsertSuomiFiPerson;
import io.resys.crm.client.api.model.CustomerCommand.UpsertSuomiFiRep;
import io.resys.crm.client.api.model.Document.DocumentType;
import io.resys.crm.client.api.model.ImmutableCustomer;
import io.resys.crm.client.api.model.ImmutableCustomerTransaction;
import io.resys.crm.client.api.model.ImmutablePerson;
import io.resys.crm.client.spi.store.DocumentConfig;


public class CustomerCommandVisitor {
  private final DocumentConfig ctx;
  private final Customer start;
  private final List<CustomerCommand> visitedCommands = new ArrayList<>();
  private ImmutableCustomer current;
  
  public CustomerCommandVisitor(DocumentConfig ctx) {
    this.start = null;
    this.current = null;
    this.ctx = ctx;
  }
  
  public CustomerCommandVisitor(Customer start, DocumentConfig ctx) {
    this.start = start;
    this.current = ImmutableCustomer.builder().from(start).build();
    this.ctx = ctx;
  }
  
  public Customer visitTransaction(List<? extends CustomerCommand> commands) throws NoChangesException {
    commands.forEach(this::visitCommand);
    
    if(visitedCommands.isEmpty()) {
      throw new NoChangesException();
    }
    
    final var transactions = new ArrayList<>(start == null ? Collections.emptyList() : start.getTransactions());
    final var id = String.valueOf(transactions.size() +1);
    transactions
      .add(ImmutableCustomerTransaction.builder()
        .id(id)
        .commands(visitedCommands)
        .build());
    this.current = this.current.withVersion(id).withTransactions(transactions);
    return this.current;
  }
  
  private Customer visitCommand(CustomerCommand command) {
    switch (command.getCommandType()) {
    case CreateCustomer:
      return visitCreateCustomer((CreateCustomer) command);
    case UpsertSuomiFiPerson:
      return visitUpsertSuomiFiPerson((UpsertSuomiFiPerson) command);
    case UpsertSuomiFiRep:
      return visitUpsertSuomiFiRep((UpsertSuomiFiRep) command);
    case ChangeCustomerFirstName:
      return visitChangeCustomerFirstName((ChangeCustomerFirstName) command);
    case ChangeCustomerLastName:
      return visitChangeCustomerLastName((ChangeCustomerLastName) command);
    case ChangeCustomerSsn:
      return visitChangeCustomerSsn((ChangeCustomerSsn) command);
    case ChangeCustomerEmail:
      return visitChangeCustomerEmail((ChangeCustomerEmail) command);
    case ChangeCustomerAddress:
      return visitChangeCustomerAddress((ChangeCustomerAddress) command);
    case ArchiveCustomer:
      return visitArchiveCustomer((ArchiveCustomer) command);
    }
    
    throw new UpdateProjectVisitorException(String.format("Unsupported command type: %s, body: %s", command.getClass().getSimpleName(), command.toString())); 
  }
  
  
  private Customer visitCreateCustomer(CreateCustomer command) {
    final var id = ctx.getGid().getNextId(DocumentType.CUSTOMER);
    final var targetDate = requireTargetDate(command);
    
    this.current = ImmutableCustomer.builder()
      .id(id)
      .body(ImmutablePerson.builder().from(command.getBody()).build())
      .externalId(command.getExternalId())
      .created(targetDate)
      .updated(targetDate)
      
      .addTransactions(
          ImmutableCustomerTransaction.builder()
          .id("1")
          .addCommands(command)
          .build())
      .documentType(DocumentType.CUSTOMER)
      .build();
    visitedCommands.add(command);
    
    return this.current;
  }


  private Customer visitUpsertSuomiFiPerson(UpsertSuomiFiPerson command) {
    final var targetDate = requireTargetDate(command);
    if(this.current == null) {
      final var id = ctx.getGid().getNextId(DocumentType.CUSTOMER);
      this.current = ImmutableCustomer.builder()
          .id(id)
          .body(ImmutablePerson.builder()
              .contact(command.getContact())
              .firstName(command.getFirstName())
              .lastName(command.getLastName())
              .protectionOrder(command.getProtectionOrder())
              .build())
          .externalId(command.getCustomerId())
          .created(targetDate)
          .updated(targetDate)
          .addTransactions(
              ImmutableCustomerTransaction.builder()
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
    final var isIdUpdated = command.getCustomerId().equals(this.current.getExternalId());
    
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
  private Customer visitUpsertSuomiFiRep(UpsertSuomiFiRep command) {
    this.current = this.current
        .withUpdated(requireTargetDate(command));
    visitedCommands.add(command);
    return this.current;
  }

  //TODO
  private Customer visitChangeCustomerFirstName(ChangeCustomerFirstName command) {
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
  private Customer visitChangeCustomerLastName(ChangeCustomerLastName command) {
    this.current = this.current
        .withUpdated(requireTargetDate(command));
    visitedCommands.add(command);
    return this.current;
  }

  //TODO 
  private Customer visitChangeCustomerSsn(ChangeCustomerSsn command) {
    this.current = this.current
        .withUpdated(requireTargetDate(command));
    visitedCommands.add(command);
    return this.current;
  }
  
  //TODO
  private Customer visitChangeCustomerEmail(ChangeCustomerEmail command) {
    this.current = this.current
        .withUpdated(requireTargetDate(command));
    visitedCommands.add(command);
    return this.current;
  }
  
  //TODO
  private Customer visitChangeCustomerAddress(ChangeCustomerAddress command) {
    this.current = this.current
        .withUpdated(requireTargetDate(command));
    visitedCommands.add(command);
    return this.current;
  }
  
  //TODO 
  private Customer visitArchiveCustomer(ArchiveCustomer command) {
    this.current = this.current
        .withUpdated(requireTargetDate(command));
    visitedCommands.add(command);
    return this.current;
  }
  
  
  
  public static Instant requireTargetDate(CustomerCommand command) {
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
