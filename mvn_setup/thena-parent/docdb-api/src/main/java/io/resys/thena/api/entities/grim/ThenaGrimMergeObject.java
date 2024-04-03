package io.resys.thena.api.entities.grim;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewAssignment;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewLabel;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewLink;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewObjective;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewRemark;
import io.vertx.core.json.JsonObject;

// Generic interfaces for create/update/delete operations 
public interface ThenaGrimMergeObject {  
  
  interface MergeMission {
    MergeMission title(String title);
    MergeMission description(String description);
    
    MergeMission parentId(@Nullable String parentId);
    MergeMission reporterId(@Nullable String reporterId);    
    
    MergeMission status(@Nullable String status);
    MergeMission startDate(@Nullable LocalDate startDate);
    MergeMission dueDate(@Nullable LocalDate dueDate);
    MergeMission priority(@Nullable String priority);
    
    // nested builders
    <T> MergeMission setAllAssignees(List<T> replacments, Function<T, Consumer<NewAssignment>> assignment);
    <T> MergeMission setAllLabels(List<T> replacments, Function<T, Consumer<NewLabel>> label);
    <T> MergeMission setAllLinks(List<T> replacments, Function<T, Consumer<NewLink>> link);
    
    MergeMission addAssignees(Consumer<NewAssignment> assignment);
    MergeMission addLabels(Consumer<NewLabel> label);
    MergeMission addLink(Consumer<NewLink> link);
    MergeMission addRemark(Consumer<NewRemark> remark);
    MergeMission addCommands(List<JsonObject> commandToAppend);    
    MergeMission addObjective(Consumer<NewObjective> goal);
    
    
    MergeMission modifyGoal(String goalId, Consumer<MergeGoal> goal);
    MergeMission modifyObjective(String objectiveId, Consumer<MergeObjective> objective);
    MergeMission modifyRemark(String remarkId, Consumer<MergeRemark> objective);
    
    MergeMission removeGoal(String goalId);
    MergeMission removeObjective(String objectiveId);
    MergeMission removeRemark(String remarkId);

    void build();
  }
  
  // support interface inside of callback
  interface MergeObjective {
    MergeObjective title(String title);
    MergeObjective description(String description);
    MergeObjective status(@Nullable String status);
    MergeObjective startDate(@Nullable LocalDate startDate);
    MergeObjective dueDate(@Nullable LocalDate dueDate);
    
    MergeObjective addGoal(Consumer<MergeGoal> MergeGoal);
    MergeObjective addAssignees(Consumer<NewAssignment> assignment);
    
    <T> MergeObjective setAllAssignees(List<T> replacments, Function<T, Consumer<NewAssignment>> assignment);
    void build();    
  }    
  // support interface inside of callback
  interface MergeGoal {
    MergeGoal title(String title);
    MergeGoal description(String description);
    MergeGoal status(@Nullable String status);
    MergeGoal startDate(@Nullable LocalDate startDate);
    MergeGoal dueDate(@Nullable LocalDate dueDate);
    
    MergeGoal addAssignees(Consumer<NewAssignment> assignment);
    <T> MergeGoal setAllAssignees(List<T> replacments, Function<T, Consumer<NewAssignment>> assignment);
    void build(); 
  }
  // support interface inside of callback
  interface MergeRemark {
    MergeRemark remarkText(String remarkText);
    MergeRemark remarkStatus(@Nullable String remarkStatus);
    MergeRemark reporterId(String reporterId);
    void build(); 
  }
}
