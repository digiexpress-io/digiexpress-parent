package io.dialob.client.spi.event;

import java.net.InetAddress;

import javax.annotation.Nonnull;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.dialob.api.proto.Actions;
import io.dialob.client.spi.event.EventPublisher.Event;

/**
 * Common type for all questionnaire related events
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = ImmutableQuestionnaireCompletedEvent.class, name = "QuestionnaireCompleted"),
  @JsonSubTypes.Type(value = ImmutableQuestionnaireActionsEvent.class, name = "QuestionnaireActions"),
  @JsonSubTypes.Type(value = ImmutableQuestionnaireClientConnectedEvent.class, name = "QuestionnaireClientConnected"),
  @JsonSubTypes.Type(value = ImmutableQuestionnaireClientDisconnectedEvent.class, name = "QuestionnaireClientDisconnected"),
  @JsonSubTypes.Type(value = ImmutableQuestionnaireCreatedEvent.class, name = "QuestionnaireCreated"),
  @JsonSubTypes.Type(value = ImmutableQuestionnaireOpenedEvent.class, name = "QuestionnaireOpened")
})
public interface QuestionnaireEvent extends Event {

  /**
   * @return questionnaire id of event
   */
  @Nonnull
  String getQuestionnaireId();
  
  
  @Value.Immutable
  interface QuestionnaireActionsEvent extends QuestionnaireEvent {
    @Nonnull
    Actions getActions();
  }

  @Value.Immutable
  interface QuestionnaireClientConnectedEvent extends QuestionnaireEvent {
    @Nonnull
    InetAddress getClient();
  }
  
  @Value.Immutable
  interface QuestionnaireCompletedEvent extends QuestionnaireEvent {}

  @Value.Immutable
  interface QuestionnaireCreatedEvent extends QuestionnaireEvent {}
  
  @Value.Immutable
  public interface QuestionnaireOpenedEvent extends QuestionnaireEvent {}


  @Value.Immutable
  interface QuestionnaireClientDisconnectedEvent extends QuestionnaireEvent {
    InetAddress getClient();
    int getCloseStatus();
  
  }


}
