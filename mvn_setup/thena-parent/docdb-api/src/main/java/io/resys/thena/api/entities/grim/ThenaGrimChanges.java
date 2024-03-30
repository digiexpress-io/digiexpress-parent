package io.resys.thena.api.entities.grim;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

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
    MissionChanges addAssignees(Supplier<AssignmentChanges> assignment);
    MissionChanges addLabels(Supplier<LabelChanges> label);
    MissionChanges addLink(Supplier<LinkChanges> link);
    
    <T> MissionChanges setAllAssignees(List<T> replacments, Function<T, AssignmentChanges> assignment);
    <T> MissionChanges setAllLabels(List<T> replacments, Function<T, LabelChanges> label);
    <T> MissionChanges setAllLinks(List<T> replacments, Function<T, LinkChanges> link);
    
    MissionChanges addObjective(Supplier<ObjectiveChanges> goal);
    
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
    void build();    
  }    
  // support interface inside of callback
  interface GoalChanges {
    GoalChanges title(String title);
    GoalChanges description(String description);
    GoalChanges status(@Nullable String status);
    GoalChanges startDate(@Nullable LocalDate startDate);
    GoalChanges dueDate(@Nullable LocalDate dueDate);
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
}
