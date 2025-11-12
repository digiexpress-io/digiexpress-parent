package io.digiexpress.eveli.client.web.resources.worker;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.digiexpress.eveli.client.api.*;
import io.digiexpress.eveli.textanalyzer.adapter.api.SentimentSubcategoryRequest;
import io.digiexpress.eveli.textanalyzer.adapter.api.SentimentSubcategoryResponse;
import io.digiexpress.eveli.textanalyzer.adapter.api.SimilarityRequest;
import io.digiexpress.eveli.textanalyzer.adapter.api.SimilarityResponse;
import io.digiexpress.eveli.textanalyzer.adapter.spi.FeedbackAnalyzerRestClient;
import io.smallrye.mutiny.Uni;
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

import io.digiexpress.eveli.client.api.FeedbackClient.CreateFeedbackCommand;
import io.digiexpress.eveli.client.api.FeedbackClient.Feedback;
import io.digiexpress.eveli.client.api.FeedbackClient.FeedbackTemplate;
import io.digiexpress.eveli.client.api.FeedbackClient.ModifyOneFeedbackCommand;
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
  private final FeedbackAnalyzerRestClient feedbackAnalyzerRestClient;
  private final FeedbackCategoriesReader feedbackCategoriesReader;
  private static final Duration timeout = Duration.ofMillis(10000);
  
  @GetMapping
  public ResponseEntity<List<Feedback>> findAllFeedback()
  {
    final var feedbacks = feedbackClient.queryFeedbacks().findAll();
    return new ResponseEntity<>(feedbacks, HttpStatus.OK);
  }
  @GetMapping("/{taskIdOrFeedbackId}")
  public ResponseEntity<Feedback> getOneFeedback(@PathVariable("taskIdOrFeedbackId") String id)
  {
    var feedback = feedbackClient.queryFeedbacks().findOneById(id);
    if(feedback.isPresent()) {
      return new ResponseEntity<>(feedback.get(), HttpStatus.OK);
    }
    
    
    final var task = taskClient.queryTasks().findAll(Arrays.asList(id)).await().atMost(timeout);
    if(!task.isEmpty()) {
      feedback = feedbackClient.queryFeedbacks().findOneById(task.iterator().next().getTaskRef());
    }
    if(feedback.isPresent()) {
      return new ResponseEntity<>(feedback.get(), HttpStatus.OK);
    }
    
    
    return ResponseEntity.notFound().build();
  }
  @PutMapping(value = "/{taskIdOrFeedbackId}", consumes = "application/json")
  public ResponseEntity<Feedback> modifyOneFeedback(@PathVariable("taskIdOrFeedbackId") String id, @RequestBody ModifyOneFeedbackCommand body) 
  {
    // spring quirks
    final var feedback = feedbackClient.modifyOneFeedback(body, securityClient.getUser().getPrincipal().getUsername());
    return new ResponseEntity<>(feedback, HttpStatus.OK);
  }
  @PostMapping(value="/{taskIdOrFeedbackId}")
  public ResponseEntity<Feedback> createOneFeedback(@PathVariable("taskIdOrFeedbackId") String id, @RequestBody CreateFeedbackCommand command)
  {
    final var feedback = feedbackClient.createOneFeedback(command, securityClient.getUser().getPrincipal().getUsername());
    return new ResponseEntity<>(feedback, HttpStatus.OK);
  }
  @DeleteMapping("/{taskIdOrFeedbackId}")
  public ResponseEntity<Feedback> deleteOneFeedback(@PathVariable("taskIdOrFeedbackId") String id)
  {
    final var feedback = feedbackClient.queryFeedbacks().findOneById(id);
    if(feedback.isEmpty()) {
      return ResponseEntity.notFound().build(); 
    }
    feedbackClient.deleteAll(ImmutableDeleteReplyCommand.builder()
        .addReplyIds(feedback.get().getId())
        .build(), securityClient.getUser().getPrincipal().getUsername());
    return new ResponseEntity<>(feedback.get(), HttpStatus.OK);
  }
  
  
  
  @GetMapping(value="/{taskIdOrFeedbackId}/templates")
  public ResponseEntity<FeedbackTemplate> getTaskFeedbackTemplate(@PathVariable("taskIdOrFeedbackId") String id)
  {
    final var authentication = securityClient.getUser();
    final var template = feedbackClient.queryTemplate().getOneByTaskId(id, authentication.getPrincipal().getUsername());
    return new ResponseEntity<>(template, HttpStatus.OK);
  }
  
  @GetMapping(value="/{taskIdOrFeedbackId}/enabled")
  public ResponseEntity<?> getTaskFeedbackEnabled(@PathVariable("taskIdOrFeedbackId") String id)
  {
    final var authentication = securityClient.getUser();
    final var template = feedbackClient.queryTemplate().findOneByTaskId(id, authentication.getPrincipal().getUsername());
    return new ResponseEntity<>(Map.of("enabled", template.isPresent()), HttpStatus.OK);
  }

  @GetMapping("/{taskIdOrFeedbackId}/sentiment-and-subcategory")
  public Uni<ResponseEntity<SentimentSubcategoryResponse>> getFeedbackSentimentAndSubcategory(@PathVariable("taskIdOrFeedbackId") String id) throws IOException
  {
    final var feedback = feedbackClient.queryFeedbacks().findOneById(id);
    final var categories = feedbackCategoriesReader.readCategoriesJsonFile();

    if (feedback.isPresent()) {
      final var sentimentAndSubcategory = feedbackAnalyzerRestClient.findSentimentAndSubcategory(
        new SentimentSubcategoryRequest(
          id,
          feedback.get().getLocale(),
          feedback.get().getContent().getQuestion(),
          feedback.get().getContent().getMain(),
          categories
        )
      );

      return Uni.createFrom().item(ResponseEntity.ok(sentimentAndSubcategory));
    }

    // if feedback not found, try finding feedback questionnaire from task
    final var questionnaire = feedbackClient.queryQuestionnaire().findOneFromTaskById(id);
    if (questionnaire.isEmpty()) {
      return Uni.createFrom().item(ResponseEntity.notFound().build());
    }
    final var sentimentAndSubcategory = feedbackAnalyzerRestClient.findSentimentAndSubcategory(
      new SentimentSubcategoryRequest(
        id,
        questionnaire.get().getQuestionnaire().getMetadata().getLanguage(),
        questionnaire.get().getContent().getMain(),
        questionnaire.get().getContent().getQuestion(),
        categories
      )
    );
    return Uni.createFrom().item(ResponseEntity.ok(sentimentAndSubcategory));
  }

  @GetMapping("/{taskIdOrFeedbackId}/similar")
  public ResponseEntity<SimilarityResponse> getSimilarFeedbacks(@PathVariable("taskIdOrFeedbackId") String id) {
    final var feedbacks = feedbackClient.queryFeedbacks().findAll();
    if (feedbacks.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    List<SimilarityRequest.Entry> items = new ArrayList<>(feedbacks.stream().map(fb -> new SimilarityRequest.Entry(
      fb.getSourceId(),
      fb.getLocale(),
      fb.getContent().getQuestion()
    )).toList());

    // if given id is not feedback id, use task id to find feedback questionnaire and add it to items
    if (items.stream().noneMatch(i -> i.getId().equals(id))) {
      final var questionnaire = feedbackClient.queryQuestionnaire().findOneFromTaskById(id);
      if (questionnaire.isEmpty()) {
        return ResponseEntity.notFound().build();
      }
      items.add(new SimilarityRequest.Entry(
        id,
        questionnaire.get().getQuestionnaire().getMetadata().getLanguage(),
        questionnaire.get().getContent().getQuestion()
      ));
    }

    final var similarFeedbacks = feedbackAnalyzerRestClient.findSimilar(new SimilarityRequest(
      id,
      items
    ));
    return new ResponseEntity<>(similarFeedbacks, HttpStatus.OK);
  }
}
