package io.dialob.client.tests.steps.support;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.SerializationUtils;

import io.dialob.api.proto.Action;
import io.dialob.api.proto.Actions;
import io.dialob.api.proto.ImmutableActions;
import io.dialob.api.questionnaire.ImmutableQuestionnaire;
import io.dialob.api.questionnaire.ImmutableQuestionnaireMetadata;
import io.dialob.api.questionnaire.Questionnaire;
import io.dialob.client.api.DialobClient;
import io.dialob.client.api.DialobErrorHandler.DocumentNotFoundException;
import io.dialob.client.spi.event.DistributedEvent.FormUpdatedEvent;
import io.dialob.client.spi.event.EventPublisher;
import io.dialob.client.spi.event.QuestionnaireEvent.QuestionnaireActionsEvent;
import io.dialob.client.tests.client.DialobClientTestImpl;
import io.dialob.client.tests.steps.support.StepsBuilder.Expectations;
import io.dialob.client.tests.steps.support.StepsBuilder.WhenMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Context implements EventPublisher {
  
  private final Map<String, Questionnaire> db_questionnaire = new HashMap<>();
  private DialobClient client;  
  private DialobClient.QuestionnaireExecutor executor;
  private String questionnaireId;
  private Questionnaire questionnaire;
  private DialobClient.ProgramEnvir envir;
  private String rev;
  
  private List<WhenMessage> steps = new ArrayList<>();
  private WhenMessage whenMessage;
  private List<Actions> eventActions = new ArrayList<>();

  
  public void setClient(DialobClient client) { this.client = client; };
  public void addStep(WhenMessage msg) { steps.add(msg); }
  public String getRev() { return rev; }
  public void setRev(String rev) { this.rev = rev; }
  public String getQuestionnaireId() { return questionnaireId; }
  public DialobClient getClient() { return client; }
  public void setEnvir(DialobClient.ProgramEnvir envir) { this.envir = envir; }


  public ImmutableQuestionnaire save(ImmutableQuestionnaire document) {
    if (document.getId() == null) {
      document = document.withId(UUID.randomUUID().toString());
      document = document.withRev("1-" + document.getId());
    } else {
      String rev = document.getRev();
      String revNumber = rev.substring(0, rev.indexOf('-'));
      document = document.withRev((Integer.parseInt(revNumber) + 1) + "-" + document.getId());
    }
    this.questionnaireId = document.getId();
    this.db_questionnaire.put(document.getId(), SerializationUtils.clone(document));
    return document;
  }

  public Actions answerSession(Actions answer) {
    if(this.executor == null) { 
      this.executor = client.executor(envir).restore(this.questionnaire);
    }
    final var result = this.executor
        .actions(ImmutableActions.builder().from(answer).rev(rev).build())
        .execute();
    this.questionnaire = this.executor.toSession().getQuestionnaire();
    this.rev = result.getRev();
    try {
      final var text = DialobClientTestImpl.Builder.MAPPER.writeValueAsString(result);
      LOGGER.info("<-- " + text);
    } catch(IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
    return result;
  }
  
  public Actions openSession(String questionnaireId) {
    try {
      if(!db_questionnaire.containsKey(questionnaireId)) {
        throw new DocumentNotFoundException("", questionnaireId);
      }
      final var questionnaire = db_questionnaire.get(this.questionnaireId);
      final var meta = ImmutableQuestionnaireMetadata.builder().from(questionnaire.getMetadata()).language("en").build();
      final var restore = ImmutableQuestionnaire.builder().from(questionnaire).metadata(meta)
          .id(UUID.randomUUID().toString())
          .rev(UUID.randomUUID().toString())
          .build();
      
      this.executor = client.executor(envir).restore(restore);
      this.questionnaire = this.executor.toSession().getQuestionnaire();
      final var actions = executor.execute();
      try {
        final var text = DialobClientTestImpl.Builder.MAPPER.writeValueAsString(actions);
        LOGGER.info("<-- " + text);
      } catch(IOException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
      return actions;
    } catch(Exception e) {
      return client.getConfig().getErrorHandler().toActions(e);
    }
    

  }
  
  public boolean execute() throws Exception {
    final Iterator<WhenMessage> whenMessageIterator = steps.iterator();

    // Here we send messages...
    try {
      while (whenMessageIterator.hasNext()) {
        eventActions.clear();
        this.whenMessage = whenMessageIterator.next();
        whenMessage.accept();
        
        Expectations expectations = whenMessage.getExpectations();
        if (expectations.isSuccess()) {
          LOGGER.info("Step success: \"{}\"", whenMessage.getName());
        }
        

        if (!expectations.isCompleted()) {
          LOGGER.info("Message not completed, stored event: \"{}\"", whenMessage.getName());

          Thread.sleep(100);
          
          for(final var event : this.eventActions) {
            LOGGER.info("Event published: \"{}\"", DialobClientTestImpl.Builder.MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(event));
            whenMessage.getExpectations().accept(event);
          }
          
          if(!expectations.isCompleted()) {
            fail("Could not complete step '" + whenMessage.getName() + "'");            
          }
        }
        if (!expectations.isSuccess()) {
          LOGGER.error("Failed expectations {}", expectations);
          fail("Step not successful '" + whenMessage.getName() + "'");
        }
      }

    } catch(Error e) {
      throw e;
    }
    return true;
  }
  
  @Override
  public void publish(Event event) {
    try {
      if(event instanceof FormUpdatedEvent) {
        LOGGER.info("New form published: \"{}\"", DialobClientTestImpl.Builder.MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(event));
        this.executor = null;
      } else if(event instanceof QuestionnaireActionsEvent) {
        // retrieve from event
        final var resp = (QuestionnaireActionsEvent) event;
        final var actions = resp.getActions().getActions().stream()
            .filter(a -> a.getType() != Action.Type.ANSWER)
            .collect(Collectors.toList());
        
        if(!actions.isEmpty()) {
          final var result = ImmutableActions.builder().from(resp.getActions()).actions(actions).build();
          eventActions.add(result);
        }
        
      }
    } catch(Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
