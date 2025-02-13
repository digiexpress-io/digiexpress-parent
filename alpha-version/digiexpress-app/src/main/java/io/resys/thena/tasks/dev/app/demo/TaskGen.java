package io.resys.thena.tasks.dev.app.demo;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.crm.client.api.model.Customer;
import io.resys.crm.client.api.model.CustomerCommand.UpsertSuomiFiPerson;
import io.resys.crm.client.api.model.ImmutableUpsertSuomiFiPerson;
import io.resys.permission.client.api.model.Principal;
import io.resys.permission.client.api.model.Principal.Role;
import io.resys.thena.tasks.client.api.model.ImmutableCreateTask;
import io.resys.thena.tasks.client.api.model.ImmutableTaskExtension;
import io.resys.thena.tasks.client.api.model.Task.TaskExtensionType;
import io.resys.thena.tasks.client.api.model.TaskCommand.CreateTask;
import io.smallrye.mutiny.tuples.Tuple2;

public class TaskGen {

  public List<UpsertSuomiFiPerson> generateCustomers(List<Integer> windows) {
    final var provider =  new RandomDataProvider();  
    return windows.stream().map(junk -> {
      final var person = provider.person();
      return ImmutableUpsertSuomiFiPerson.builder()
          .customerId(provider.ssn())
          .userName(person.getUsername())
          .firstName(person.getFirstName())
          .lastName(person.getLastName())
          .protectionOrder(person.getProtectionOrder())
          .contact(person.getContact())
          .build();
    }).collect(Collectors.toList());
  }
  
  
  public List<CreateTask> generateTasks(
      List<Integer> windows, 
      List<Customer> customers,
      Tuple2<List<Principal>, List<Role>> permissions
   ) {
    
    final var targetDate = Instant.now();
    final var provider =  new RandomDataProvider();  
    final var windowsIt = windows.iterator();
    final var customersIt = customers.iterator();
    final var bulkTasks = new ArrayList<CreateTask>();

    while(customersIt.hasNext()) {
      final var customer = customersIt.next();
      final var count = windowsIt.next();
      for(int index = 0; index < count; index++) {
        
        final var startAndDueDate = provider.getStartDateAndDueDate(LocalDate.ofInstant(targetDate, ZoneId.of("UTC")));
        final var newTask = ImmutableCreateTask.builder()
          .startDate(startAndDueDate.getStartDate())
          .dueDate(startAndDueDate.getDueDate())
          .targetDate(targetDate.minus(10, java.time.temporal.ChronoUnit.DAYS))
          .checklist(provider.getChecklists(LocalDate.ofInstant(targetDate, ZoneId.of("UTC")), permissions))
          .title(provider.getTitle())
          .description(provider.getDescription())
          .priority(provider.getPriority())
          .roles(provider.getRoles(permissions))
          .assigneeIds(provider.getAssigneeIds(permissions))
          .reporterId(provider.getReporterId(permissions))
          .status(provider.getStatus())
          .userId("demo-gen-1")
          .addAllExtensions(provider.getExtensions())
          .comments(provider.getComments())
          .addExtensions(ImmutableTaskExtension.builder()
              .id(customer.getId())
              .created(targetDate)
              .updated(targetDate)
              .type(TaskExtensionType.CUSTOMER.name())
              .externalId(customer.getId())
              .build())
          .build();
        bulkTasks.add(newTask);
      }
    }
    
    return bulkTasks;
  }
}
