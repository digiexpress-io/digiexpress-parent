package io.digiexpress.eveli.client.web.resources.worker;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.digiexpress.eveli.client.api.FeedbackClient;
import io.digiexpress.eveli.client.api.FeedbackClient.CreateFeedbackCommand;
import io.digiexpress.eveli.client.api.FeedbackClient.Feedback;
import io.digiexpress.eveli.client.api.FeedbackClient.FeedbackTemplate;
import io.digiexpress.eveli.client.api.FeedbackClient.ModifyOneFeedbackCommand;
import io.digiexpress.eveli.client.api.FeedbackClient.SentimentAndSubcategory;
import io.digiexpress.eveli.client.api.FeedbackClient.SimilarFeedback;
import io.digiexpress.eveli.client.api.ImmutableDeleteReplyCommand;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.WorkerAuthClient;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@RestController
@RequestMapping("/worker/rest/api/feedback")
@Slf4j
@RequiredArgsConstructor
public class FeedbackApiController {
  private final WorkerAuthClient securityClient;
  private final FeedbackClient feedbackClient;
  private final TaskClient taskClient;

  
  @GetMapping
  public Multi<Feedback> findAllFeedback() {
    return feedbackClient.queryFeedbacks().findAll();
  }
  
  
  @GetMapping("/{taskIdOrFeedbackId}")
  public Uni<ResponseEntity<Feedback>> getOneFeedback(@PathVariable("taskIdOrFeedbackId") String id) {
    return feedbackClient.queryFeedbacks().findOneById(id)
      .onItem().transformToUni(feedback -> {
        
        if(feedback.isPresent()) {
          return Uni.createFrom().item(new ResponseEntity<>(feedback.get(), HttpStatus.OK));
        }
        
        return taskClient.queryTasks().findOneById(id)
          .onItem().transformToUni(task -> {
            if(task.isPresent()) {
              return feedbackClient.queryFeedbacks().findOneById(task.get().getTaskRef());
            }
            return Uni.createFrom().item(Optional.<Feedback>empty());
          })
          .onItem().transform(found -> {
            if(found.isPresent()) {
              return new ResponseEntity<>(found.get(), HttpStatus.OK);
            }
            return ResponseEntity.notFound().build();  
          });
      });
  }
  
  @PutMapping(value = "/{taskIdOrFeedbackId}", consumes = "application/json")
  public Uni<Feedback> modifyOneFeedback(@PathVariable("taskIdOrFeedbackId") String id, @RequestBody ModifyOneFeedbackCommand body) {
    return feedbackClient.modifyOneFeedback(body, securityClient.getUser().getPrincipal().getUsername());
  }
  
  @PostMapping(value="/{taskIdOrFeedbackId}")
  public Uni<Feedback> createOneFeedback(@PathVariable("taskIdOrFeedbackId") String id, @RequestBody CreateFeedbackCommand command) {
    return feedbackClient.createOneFeedback(command, securityClient.getUser().getPrincipal().getUsername());
  }
  
  @DeleteMapping("/{taskIdOrFeedbackId}")
  public Uni<ResponseEntity<Feedback>> deleteOneFeedback(@PathVariable("taskIdOrFeedbackId") String id) {
    final var user = securityClient.getUser().getPrincipal().getUsername();
    return feedbackClient.queryFeedbacks().findOneById(id)
        .onItem().transformToUni(feedback -> {
    
          if(feedback.isEmpty()) {
            return Uni.createFrom().item(ResponseEntity.notFound().build()); 
          }

          final var command = ImmutableDeleteReplyCommand.builder().addReplyIds(feedback.get().getId()).build();
          return feedbackClient.queryFeedbacks().deleteAll(command, user)
            .collect().asList()
            .onItem().transform(found -> new ResponseEntity<>(feedback.get(), HttpStatus.OK));
        });
  }
  
  
  
  @GetMapping(value="/{taskIdOrFeedbackId}/templates")
  public Uni<FeedbackTemplate> getTaskFeedbackTemplate(@PathVariable("taskIdOrFeedbackId") String id) {
    final var authentication = securityClient.getUser();
    return feedbackClient.queryTemplate().getOneByTaskId(id, authentication.getPrincipal().getUsername());
  }
  
  @GetMapping(value="/{taskIdOrFeedbackId}/enabled")
  public Uni<ResponseEntity<?>> getTaskFeedbackEnabled(@PathVariable("taskIdOrFeedbackId") String id) {
    final var authentication = securityClient.getUser();
    return feedbackClient.queryTemplate()
        .findOneByTaskId(id, authentication.getPrincipal().getUsername())
        .map(template -> new ResponseEntity<>(Map.of("enabled", template.isPresent()), HttpStatus.OK));
  }

  @GetMapping("/{taskIdOrFeedbackId}/sentiment-and-subcategory")
  public Uni<SentimentAndSubcategory> getFeedbackSentimentAndSubcategory(@PathVariable("taskIdOrFeedbackId") String id) {
    return feedbackClient.queryFeedbackAnalyzer().getOneSentimentAndSubcategoryById(id);
  }

  @GetMapping("/{taskIdOrFeedbackId}/similar")
  public Uni<ResponseEntity<SimilarFeedback>> getSimilarFeedback(@PathVariable("taskIdOrFeedbackId") String id) {
    return feedbackClient.queryFeedbackAnalyzer().findOneSimilarFeedbackById(id)
        .map(similarFeedback -> similarFeedback
            .map(ResponseEntity::ok)
            // empty result expected if there are no existing feedbacks
            .orElse(ResponseEntity.notFound().build()));

  }
}
