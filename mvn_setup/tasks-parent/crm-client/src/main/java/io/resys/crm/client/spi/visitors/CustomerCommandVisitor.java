package io.resys.crm.client.spi.visitors;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.resys.crm.client.api.model.Customer;
import io.resys.crm.client.api.model.CustomerCommand;
import io.resys.crm.client.api.model.CustomerCommand.ChangeCustomerInfo;
import io.resys.crm.client.api.model.CustomerCommand.CreateCustomer;
import io.resys.crm.client.api.model.Document.DocumentType;
import io.resys.crm.client.api.model.ImmutableCustomer;
import io.resys.crm.client.api.model.ImmutableCustomerTransaction;
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
  
  public Customer visitTransaction(List<? extends CustomerCommand> commands) {
    commands.forEach(this::visitCommand);
    
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
    visitedCommands.add(command);
    switch (command.getCommandType()) {
      case ChangeCustomerInfo:
        return visitChangeCustomerInfo((ChangeCustomerInfo) command);
      case CreateCustomer:
        return visitCreateCustomer((CreateCustomer)command);
    }
    throw new UpdateProjectVisitorException(String.format("Unsupported command type: %s, body: %s", command.getClass().getSimpleName(), command.toString()));
  }
  
  
  private Customer visitCreateCustomer(CreateCustomer command) {
    final var id = command.getName();
    final var targetDate = requireTargetDate(command);
    
    this.current = ImmutableCustomer.builder()
      .id(id)
      .name(id)
      .created(targetDate)
      .updated(targetDate)
      .addTransactions(
          ImmutableCustomerTransaction.builder()
          .id("1")
          .addCommands(command)
          .build())
      .documentType(DocumentType.CRM)
      .build();
    
    return this.current;
  }


  private Customer visitChangeCustomerInfo(ChangeCustomerInfo command) {
    this.current = this.current
        .withName(command.getName())
        .withUpdated(requireTargetDate(command));
    return this.current;
  }
  

  public static Instant requireTargetDate(CustomerCommand command) {
    final var targetDate = command.getTargetDate();
    if (targetDate == null) {
      throw new UpdateProjectVisitorException("targetDate not defined");
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
