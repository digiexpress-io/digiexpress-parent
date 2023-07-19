package io.dialob.spring.composer.controllers;

import java.io.PrintWriter;
import java.io.StringWriter;

/*-
 * #%L
 * hdes-spring-bundle-editor
 * %%
 * Copyright (C) 2020 - 2021 Copyright 2020 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.time.Duration;
import java.util.Map;

import org.immutables.value.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.dialob.api.form.Form;
import io.dialob.api.proto.Action;
import io.dialob.api.proto.Actions;
import io.dialob.api.proto.ImmutableAction;
import io.dialob.api.proto.ImmutableActions;
import io.dialob.client.api.DialobClient;
import io.dialob.client.api.DialobComposer;
import io.dialob.client.api.DialobComposer.ComposerState;
import io.dialob.client.api.DialobErrorHandler.DocumentNotFoundException;
import io.dialob.client.api.DialobFill;
import io.dialob.client.api.DialobFill.FillEntry;
import io.dialob.client.api.QuestionnaireSession;
import io.dialob.client.spi.DialobComposerImpl;
import io.dialob.client.spi.DialobInMemoryFill;
import io.dialob.client.spi.form.FormActions;
import io.dialob.client.spi.form.FormActionsUpdatesCallback;
import io.dialob.spring.composer.config.UiConfigBean;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(UiConfigBean.REST_SPRING_CTX_PATH_EXP)
public class DialobComposerServiceController {
  
  private final DialobClient client;
  private final DialobComposer composer;
  private final DialobFill fill;
  private final ObjectMapper objectMapper;
  private static final Duration timeout = Duration.ofMillis(10000);
  private long warningThreshold = 2000000000L; // 2 seconds
  private boolean returnStackTrace = true;

  @Value.Immutable @JsonSerialize(as = ImmutableInitSession.class) @JsonDeserialize(as = ImmutableInitSession.class)
  public interface InitSession {
    String getFormId();
    String getLanguage();
    Map<String, Object> getContextValues();
  }
  
  public DialobComposerServiceController(DialobClient client, ObjectMapper objectMapper, ApplicationContext ctx) {
    super();
    this.client = client;
    this.composer = new DialobComposerImpl(client);
    this.fill = DialobInMemoryFill.builder().build("DialobComposerServiceController", client);
    this.objectMapper = objectMapper;
    
    final var servicePath = ctx.getEnvironment().getProperty(UiConfigBean.REST_SPRING_CTX_PATH);
    final var uiPath = ctx.getEnvironment().getProperty(UiConfigBean.UI_SPRING_CTX_PATH);    
    final var uiEnabled = ctx.getEnvironment().getProperty(UiConfigBean.UI_ENABLED);
    
    final var log = new StringBuilder()
    .append("Dialob, Composer Service: UP").append(System.lineSeparator())
    .append("service paths:").append(System.lineSeparator())
    .append("  - GET, html").append(uiPath).append(": ").append("dialob composer user interface, enabled: ").append(uiEnabled).append(System.lineSeparator())
    .append("  - GET, json").append(servicePath).append(": ").append("/models").append(": ").append("returns all form revisions").append(System.lineSeparator())
    .append("  - GET, json").append(servicePath).append(": ").append("/forms/{id}").append(": ").append("returns form body based on id").append(System.lineSeparator())
    .append("  - GET, json").append(servicePath).append(": ").append("/sessions/{sessionId}").append(": ").append("returns fill session based on id").append(System.lineSeparator())
    .append("  - POST Actions, json").append(servicePath).append(": ").append("/sessions/{sessionId}").append(": ").append("returns actions for given session id evals").append(System.lineSeparator())
    ;
    
    LOGGER.info(log.toString());
  }

  @GetMapping(path = "/models", produces = MediaType.APPLICATION_JSON_VALUE)
  public ComposerState models() {
    final var state = composer.get().await().atMost(timeout);
    return state;
  }
  
  @GetMapping(path = "/forms/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Form forms(@PathVariable String id) {
    final var form = composer.get().onItem()
        .transform(e -> e.getForms().values().stream()
            .filter(a -> a.getId().equals(id)).findFirst())
        .await().atMost(timeout);

    if(form.isEmpty()) {
      // throw 404
    }
    
    return form.get().getData();
  }


  @GetMapping("/sessions/{sessionId}")
  public ResponseEntity<Actions> sessionState(@PathVariable("sessionId") String sessionId) {
    long start = System.nanoTime();
    LOGGER.debug("Received 'GET /{}' request", sessionId);
    final ImmutableActions.Builder actions = ImmutableActions.builder();
    try {
      final var session = fill.query().get(sessionId).await().atMost(timeout);
      final var state = client.store().query().get().await().atMost(timeout);
      final var envir = client.envir().from(state).build();
      
      QuestionnaireSession questionnaireSession = client.executor(envir).restore(session).toSession();
      FormActions formActions = new FormActions();
      questionnaireSession.buildFullForm(new FormActionsUpdatesCallback(formActions));
      actions.actions(formActions.getActions());
      actions.rev(questionnaireSession.getRevision());
    } catch(DocumentNotFoundException e) {
      return createQuestionnaireNotFoundResponse(sessionId, e);
    } catch(Exception e) {
      LOGGER.error(String.format("Dialog fetch failed: %s", e.getMessage()), e);
      return createServiceErrorResponse(e);
    }
    long time = System.nanoTime() - start;
    if (time > warningThreshold) {
      LOGGER.warn("Request time {}ns exceeds warning threshold {}.", time, warningThreshold);
    } else if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("Request time {}ms", time / 1e6);
    }
    return ResponseEntity.ok(actions.build());
  }

  @PostMapping("/sessions")
  public ResponseEntity<FillEntry> createSession(@RequestBody InitSession init) {
    long start = System.nanoTime();
    try {
      
      final var session = fill.create()
          .formId(init.getFormId())
          .language(init.getLanguage())
          .contextValues(init.getContextValues())
          .build().await().atMost(timeout);

      return ResponseEntity.ok(session);
    } finally {
      long time = System.nanoTime() - start;
      if (time > warningThreshold) {
        LOGGER.warn("Request time {}ns exceeds warning threshold {}.", time, warningThreshold);
      } else if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Request time {}ms", time / 1e6);
      }
    }    
  }
  
  @PostMapping("/sessions/{sessionId}")
  public ResponseEntity<Actions> answers(@PathVariable("sessionId") String sessionId, @RequestBody Actions actions) {
    long start = System.nanoTime();
    try {
      final var session = fill.query().get(sessionId).await().atMost(timeout);
      final var state = client.store().query().get().await().atMost(timeout);
      final var envir = client.envir().from(state).build();
      final var result = client.executor(envir).restore(session).actions(actions).executeAndGetBody();
      fill.save(result.getQuestionnaire()).await().atMost(timeout);;
      
      return ResponseEntity.ok(result.getActions());
    } catch(DocumentNotFoundException e) {
      return createQuestionnaireNotFoundResponse(sessionId, e);
    } catch(Exception e) {
      LOGGER.error(String.format("Dialog update failed: %s", e.getMessage()), e);
      return createServiceErrorResponse(e);
    } finally {
      long time = System.nanoTime() - start;
      if (time > warningThreshold) {
        LOGGER.warn("Request time {}ns exceeds warning threshold {}.", time, warningThreshold);
      } else if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Request time {}ms", time / 1e6);
      }
    }    
  }

  private ResponseEntity<Actions> createServiceErrorResponse(Exception e) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ImmutableActions.builder().addActions(createNotifyServerErrorAction(e)).build());
  }

  private Action createNotifyServerErrorAction(Exception e) {
    final ImmutableAction.Builder action = ImmutableAction.builder();
    action.type(Action.Type.SERVER_ERROR);
    action.serverEvent(true);
    if (returnStackTrace) {
      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      action.message(e.getMessage());
      action.trace(sw.toString());
    }
    return action.build();
  }
  private ResponseEntity<Actions> createQuestionnaireNotFoundResponse(String sessionId, DocumentNotFoundException e) {
    LOGGER.debug("Action QUESTIONNAIRE_NOT_FOUND: backend response '{}'", e != null ? e.getMessage() : "Security block");
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
      ImmutableActions.builder().addActions(ImmutableAction.builder()
        .type(Action.Type.SERVER_ERROR)
        .serverEvent(true)
        .message("not found")
        .id(sessionId).build()).build());
  }
  
  /*

  @GetMapping(path = "/" + HdesWebConfig.EXPORTS, produces = MediaType.APPLICATION_JSON_VALUE)
  public StoreDump exports() {
    return composer.getStoreDump().await().atMost(timeout);
  }
  
  @PostMapping(path = "/" + HdesWebConfig.COMMANDS, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public ComposerEntity<?> commands(@RequestBody String body) throws JsonMappingException, JsonProcessingException {
    final var command = objectMapper.readValue(body, UpdateEntity.class);
    return composer.dryRun(command).await().atMost(timeout);
  }

  @PostMapping(path = "/" + HdesWebConfig.DEBUGS, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public DebugResponse debug(@RequestBody DebugRequest debug) {
    return composer.debug(debug).await().atMost(timeout);
  }

  @PostMapping(path = "/" + HdesWebConfig.IMPORTS, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public ComposerState importTag(@RequestBody AstTag entity) {
    return composer.importTag(entity).await().atMost(timeout);
  }

  @PostMapping(path = "/" + HdesWebConfig.RESOURCES, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public ComposerState create(@RequestBody CreateEntity entity) {
    return composer.create(entity).await().atMost(timeout);
  }
  @PutMapping(path = "/" + HdesWebConfig.RESOURCES, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public ComposerState update(@RequestBody UpdateEntity entity) {
    return composer.update(entity).await().atMost(timeout);
  }
  @DeleteMapping(path = "/" + HdesWebConfig.RESOURCES + "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ComposerState delete(@PathVariable String id) {
    return composer.delete(id).await().atMost(timeout);
  }
  @GetMapping(path = "/" + HdesWebConfig.RESOURCES + "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ComposerEntity<?> get(@PathVariable String id) {
    return composer.get(id).await().atMost(timeout);
  }
  
  @PostMapping(path = "/" + HdesWebConfig.COPYAS, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public ComposerState copyAs(@RequestBody CopyAs entity) {
    return composer.copyAs(entity).await().atMost(timeout);
  }
  @GetMapping(path = "/" + HdesWebConfig.HISTORY + "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public HistoryEntity history(@RequestParam("id") String id) {
    return composer.getHistory(id).await().atMost(timeout);
  }
  */
}
