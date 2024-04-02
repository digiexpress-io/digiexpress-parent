package io.resys.thena.api.entities.grim;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimOneOfRelations;
import io.vertx.core.json.JsonObject;

// Generic interfaces for create/update/delete operations 
public interface ThenaGrimChanges {

  interface MissionChanges {
    MissionChanges title(String title);
    MissionChanges description(String description);
    
    MissionChanges parentId(@Nullable String parentId);
    MissionChanges reporterId(@Nullable String reporterId);    
    
    MissionChanges status(@Nullable String status);
    MissionChanges startDate(@Nullable LocalDate startDate);
    MissionChanges dueDate(@Nullable LocalDate dueDate);
    MissionChanges priority(@Nullable String priority);
    
    // nested builders
    MissionChanges addAssignees(Consumer<AssignmentChanges> assignment);
    MissionChanges addLabels(Consumer<LabelChanges> label);
    MissionChanges addLink(Consumer<LinkChanges> link);
    MissionChanges addRemark(Consumer<RemarkChanges> remark);
    <T> MissionChanges addCommand(Consumer<CommandChanges> command);    
    
    <T> MissionChanges addCommands(List<T> replacments, Function<T, Consumer<CommandChanges>> command);    
    <T> MissionChanges setAllAssignees(List<T> replacments, Function<T, Consumer<AssignmentChanges>> assignment);
    <T> MissionChanges setAllLabels(List<T> replacments, Function<T, Consumer<LabelChanges>> label);
    <T> MissionChanges setAllLinks(List<T> replacments, Function<T, Consumer<LinkChanges>> link);
    
    MissionChanges addObjective(Consumer<ObjectiveChanges> goal);
    
    void build();
  }
  
  // support interface inside of callback
  interface AssignmentChanges {
    AssignmentChanges assignee(String assignee);
    AssignmentChanges assignmentType(String assignmentType);
    AssignmentChanges oneOfRelations(@Nullable GrimOneOfRelations rels);
    void build();
  }  
  // support interface inside of callback
  interface LabelChanges {
    LabelChanges labelType(String labelType);
    LabelChanges labelValue(String labelValue);
    LabelChanges labelBody(@Nullable JsonObject labelBody);
    LabelChanges oneOfRelations(@Nullable GrimOneOfRelations rels);
    void build();
  }
  // support interface inside of callback
  interface LinkChanges {
    LinkChanges linkType(String linkType);
    LinkChanges linkValue(String linkValue);
    LinkChanges linkBody(@Nullable JsonObject linkBody);
    LinkChanges oneOfRelations(@Nullable GrimOneOfRelations rels);
    void build();
  }  
  // support interface inside of callback
  interface ObjectiveChanges {
    ObjectiveChanges title(String title);
    ObjectiveChanges description(String description);
    ObjectiveChanges status(@Nullable String status);
    ObjectiveChanges startDate(@Nullable LocalDate startDate);
    ObjectiveChanges dueDate(@Nullable LocalDate dueDate);
    
    ObjectiveChanges addGoal(Consumer<GoalChanges> newGoal);
    ObjectiveChanges addAssignees(Consumer<AssignmentChanges> assignment);
    <T> ObjectiveChanges setAllAssignees(List<T> replacments, Function<T, Consumer<AssignmentChanges>> assignment);
    
    void build();    
  }    
  // support interface inside of callback
  interface GoalChanges {
    GoalChanges title(String title);
    GoalChanges description(String description);
    GoalChanges status(@Nullable String status);
    GoalChanges startDate(@Nullable LocalDate startDate);
    GoalChanges dueDate(@Nullable LocalDate dueDate);
    
    GoalChanges addAssignees(Consumer<AssignmentChanges> assignment);
    <T> GoalChanges setAllAssignees(List<T> replacments, Function<T, Consumer<AssignmentChanges>> assignment);
    void build(); 
  }
  // support interface inside of callback
  interface RemarkChanges {
    RemarkChanges remarkText(String remarkText);
    RemarkChanges remarkStatus(@Nullable String remarkStatus);
    RemarkChanges reporterId(String reporterId);
    RemarkChanges oneOfRelations(@Nullable GrimOneOfRelations rels);
    void build(); 
  }
  
  // support interface inside of callback
  interface CommandChanges {
    CommandChanges commandValue(JsonObject command);
    CommandChanges commandBody(JsonObject command);
    void build(); 
  }
}
