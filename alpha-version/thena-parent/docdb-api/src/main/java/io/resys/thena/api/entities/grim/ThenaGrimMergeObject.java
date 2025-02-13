package io.resys.thena.api.entities.grim;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.MergeLink;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewAssignment;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewGoal;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewLabel;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewLink;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewObjective;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewRemark;
import io.vertx.core.json.JsonObject;

// Generic interfaces for create/update/delete operations 
public interface ThenaGrimMergeObject {  
  
  interface MergeMission {
    MergeMission onCurrentState(Consumer<GrimMissionContainer> handleCurrentState);
    MergeMission title(String title);
    MergeMission description(String description);
    
    MergeMission parentId(@Nullable String parentId);
    MergeMission reporterId(@Nullable String reporterId);    
    
    MergeMission status(@Nullable String status);
    MergeMission archivedAt(@Nullable OffsetDateTime archivedAt);
    MergeMission startDate(@Nullable LocalDate startDate);
    MergeMission dueDate(@Nullable LocalDate dueDate);
    MergeMission priority(@Nullable String priority);
    
    // nested builders
    <T> MergeMission setAllAssignees(String assigneeType, List<T> replacments, Function<T, Consumer<NewAssignment>> assignment);
    <T> MergeMission setAllLabels(String labelType, List<T> replacments, Function<T, Consumer<NewLabel>> label);
    <T> MergeMission setAllLinks(String linkType, List<T> replacments, Function<T, Consumer<NewLink>> link);
    
    MergeMission addAssignees(Consumer<NewAssignment> assignment);
    MergeMission addLabels(Consumer<NewLabel> label);
    MergeMission addLink(Consumer<NewLink> link);
    MergeMission addRemark(Consumer<NewRemark> remark);
    MergeMission addCommands(List<JsonObject> commandToAppend);    
    MergeMission addObjective(Consumer<NewObjective> goal);
    
    MergeMission modifyLink(String linkId, Consumer<MergeLink> goal);
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
    
    MergeObjective addGoal(Consumer<NewGoal> newGoal);
    MergeObjective addLabels(Consumer<NewLabel> label);
    MergeObjective addLink(Consumer<NewLink> link);
    MergeObjective addAssignees(Consumer<NewAssignment> assignment);
    MergeObjective addRemark(Consumer<NewRemark> remark);
    
    <T> MergeObjective setAllAssignees(List<T> replacments, Function<T, Consumer<NewAssignment>> assignment);
    <T> MergeObjective setAllLabels(List<T> replacments, Function<T, Consumer<NewLabel>> label);
    <T> MergeObjective setAllLinks(List<T> replacments, Function<T, Consumer<NewLink>> link);
    
    void build();    
  }    
  // support interface inside of callback
  interface MergeGoal {
    MergeGoal title(String title);
    MergeGoal description(String description);
    MergeGoal status(@Nullable String status);
    MergeGoal startDate(@Nullable LocalDate startDate);
    MergeGoal dueDate(@Nullable LocalDate dueDate);
    
    MergeGoal addLabels(Consumer<NewLabel> label);
    MergeGoal addLink(Consumer<NewLink> link);
    MergeGoal addAssignees(Consumer<NewAssignment> assignment);
    MergeGoal addRemark(Consumer<NewRemark> remark);
    
    <T> MergeGoal setAllAssignees(String assignmentType, List<T> replacments, Function<T, Consumer<NewAssignment>> assignment);
    <T> MergeGoal setAllLabels(String labelType, List<T> replacments, Function<T, Consumer<NewLabel>> label);
    <T> MergeGoal setAllLinks(String linkType, List<T> replacments, Function<T, Consumer<NewLink>> link);
    
    void build(); 
  }
  // support interface inside of callback
  interface MergeRemark {
    MergeRemark parentId(@Nullable String parentId);
    MergeRemark remarkText(String remarkText);
    MergeRemark remarkStatus(@Nullable String remarkStatus);
    MergeRemark reporterId(String reporterId);
    void build(); 
  }
}
