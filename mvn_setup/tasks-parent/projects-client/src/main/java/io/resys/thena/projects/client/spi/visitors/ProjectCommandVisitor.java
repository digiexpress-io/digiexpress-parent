package io.resys.thena.projects.client.spi.visitors;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.resys.thena.projects.client.api.model.Document.DocumentType;
import io.resys.thena.projects.client.api.model.ImmutableProject;
import io.resys.thena.projects.client.api.model.ImmutableProjectTransaction;
import io.resys.thena.projects.client.api.model.Project;
import io.resys.thena.projects.client.api.model.ProjectCommand;
import io.resys.thena.projects.client.api.model.ProjectCommand.ArchiveProject;
import io.resys.thena.projects.client.api.model.ProjectCommand.AssignProjectUsers;
import io.resys.thena.projects.client.api.model.ProjectCommand.ChangeProjectInfo;
import io.resys.thena.projects.client.api.model.ProjectCommand.CreateProject;
import io.resys.thena.projects.client.spi.store.DocumentConfig;


public class ProjectCommandVisitor {
  private final DocumentConfig ctx;
  private final Project start;
  private final List<ProjectCommand> visitedCommands = new ArrayList<>();
  private ImmutableProject current;
  
  public ProjectCommandVisitor(DocumentConfig ctx) {
    this.start = null;
    this.current = null;
    this.ctx = ctx;
  }
  
  public ProjectCommandVisitor(Project start, DocumentConfig ctx) {
    this.start = start;
    this.current = ImmutableProject.builder().from(start).build();
    this.ctx = ctx;
  }
  
  public Project visitTransaction(List<? extends ProjectCommand> commands) {
    commands.forEach(this::visitCommand);
    
    final var transactions = new ArrayList<>(start == null ? Collections.emptyList() : start.getTransactions());
    final var id = String.valueOf(transactions.size() +1);
    transactions
      .add(ImmutableProjectTransaction.builder()
        .id(id)
        .commands(visitedCommands)
        .build());
    this.current = this.current.withVersion(id).withTransactions(transactions);
    return this.current;
  }
  
  private Project visitCommand(ProjectCommand command) {
    visitedCommands.add(command);
    switch (command.getCommandType()) {
      case ChangeProjectInfo:
        return visitChangeProjectInfo((ChangeProjectInfo) command);
      case AssignProjectUsers:
        return visitAssignProject((AssignProjectUsers) command);
      case ArchiveProject:
        return visitArchiveProject((ArchiveProject) command);
      case CreateProject:
        return visitCreateProject((CreateProject)command);
    }
    throw new UpdateProjectVisitorException(String.format("Unsupported command type: %s, body: %s", command.getClass().getSimpleName(), command.toString()));
  }
  
  
  private Project visitCreateProject(CreateProject command) {
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

  private Project visitArchiveProject(ArchiveProject command) {
    final var targetDate = requireTargetDate(command);
    this.current = this.current
        .withArchived(targetDate)
        .withUpdated(targetDate);
    return this.current;
  }

  private Project visitAssignProject(AssignProjectUsers command) {
    this.current = this.current
        .withUsers(command.getUsers().stream().distinct().sorted().toList())
        .withUpdated(requireTargetDate(command));
    return this.current;
  }

  private Project visitChangeProjectInfo(ChangeProjectInfo command) {
    this.current = this.current
        .withTitle(command.getTitle())
        .withDescription(command.getDescription())
        .withUpdated(requireTargetDate(command));
    return this.current;
  }
  

  public static Instant requireTargetDate(ProjectCommand command) {
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
