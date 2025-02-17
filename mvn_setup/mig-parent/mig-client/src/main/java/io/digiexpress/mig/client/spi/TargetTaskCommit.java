package io.digiexpress.mig.client.spi;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.digiexpress.mig.client.api.SourceTasks;
import io.digiexpress.mig.client.api.SourceTasks.SourceComment;
import io.digiexpress.mig.client.api.SourceTasks.SourceTask;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class TargetTaskCommit {
  private final List<CommitEvent> processEvents;
  
  public enum CommitEventType { COMMENT, CREATED, UPDATED }

  @Data @Builder
  public static class CommitEvent {
    private final String eventId;
    private final String parentEventId;
    private final String eventAuthor;
    private final OffsetDateTime eventDate;
    private final CommitEventType eventType;
  }
  
  public String createdWithCommit() {
    final var created = processEvents.get(0);
    
    if(created.getEventType() != CommitEventType.CREATED) {
      throw new RuntimeException("This should not happen... create commit must always be the first one!");
    }
    
    return created.getEventId();
  }
  
  public String updatedWithCommit() {
    return processEvents.stream()
        .filter(e -> e.getEventType() == CommitEventType.UPDATED)
        .findFirst().map(e -> e.getEventId())
        .orElse(createdWithCommit());
  }
  
  public String treeUpdatedWithCommit() {
    return processEvents.get(processEvents.size() -1).getEventId();
  }
  
  public List<CommitEvent> getEvents() {
    return processEvents;
  }

  public static TargetTaskCommit of(long task, SourceTasks source) {
    return of(source.getTasks().get(task), source);
  }
  
  public static TargetTaskCommit of(SourceTask task, SourceTasks source) {
    final var events = new ArrayList<CommitEvent>();
    events.add(createEvent(task, source));
    events.add(createUpdateEvent(task, source));
    events.addAll(source.getComments(task.getId()).stream().map(c -> createCommentEvent(c, source)).toList());
    
    
    final var result = new ArrayList<CommitEvent>();
    CommitEvent previous = null;
    for(final var event : events.stream().sorted((a, b) -> a.getEventDate().compareTo(b.getEventDate())).toList()) {
      if(previous == null) {
        result.add(event);
      } else {
        // copy and attach previous
        result.add(CommitEvent.builder()
            .parentEventId(previous.getEventId())
            .eventAuthor(event.getEventAuthor())
            .eventId(event.getEventId())
            .eventType(event.getEventType())
            .eventDate(event.getEventDate())
            .build());
      }
      previous = event;
    }
    
    return new TargetTaskCommit(result);
  }
  
  
  private static CommitEvent createUpdateEvent(SourceTask task, SourceTasks source) {
    return CommitEvent.builder()
        .eventAuthor(task.getUpdater_id().orElse("conversion"))
        .eventId(task.getId() + "/UP")
        .parentEventId(null)
        .eventType(CommitEventType.UPDATED)
        .eventDate(task.getUpdated().atZone(source.getZoneId()).toOffsetDateTime())
        .build();
  }
  
  public static CommitEvent createCommentEvent(SourceComment comment, SourceTasks source) {
    return CommitEvent.builder()
        .eventAuthor(comment.getUser_name())
        .eventId(comment.getId() + "/CM")
        .parentEventId(null)
        .eventType(CommitEventType.COMMENT)
        .eventDate(comment.getCreated().atZone(source.getZoneId()).toOffsetDateTime())
        .build();
  }
  
  private static CommitEvent createEvent(SourceTask task, SourceTasks source) {
    final String author;
    if(task.getClient_identificator().isPresent() && !task.getClient_identificator().get().isBlank()) {
      author = task.getClient_identificator().get();
    } else {
      author = Optional.ofNullable(source.getAccess().get(task.getId())).orElse(Collections.emptyList())
        .stream().sorted((a, b) -> a.getUpdated().compareTo(b.getUpdated()))
        .findFirst().map(e -> e.getUser_id()).orElse("conversion");
    }
    return CommitEvent.builder()
      .eventAuthor(author)
      .eventId(task.getId() + "/" + task.getVersion() + "/C")
      .parentEventId(null)
      .eventType(CommitEventType.CREATED)
      .eventDate(task.getCreated().atZone(source.getZoneId()).toOffsetDateTime())
      .build();
    
  } 
}
