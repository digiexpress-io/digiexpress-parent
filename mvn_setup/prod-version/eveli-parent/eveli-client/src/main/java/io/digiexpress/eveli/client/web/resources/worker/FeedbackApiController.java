package io.digiexpress.eveli.client.web.resources.worker;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

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

import io.digiexpress.eveli.client.api.AuthClient;
import io.digiexpress.eveli.client.api.FeedbackClient;
import io.digiexpress.eveli.client.api.FeedbackClient.Feedback;
import io.digiexpress.eveli.client.api.FeedbackClient.ModifyOneFeedbackReplyCommand;
import io.digiexpress.eveli.client.api.ImmutableDeleteReplyCommand;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@RestController
@RequestMapping("/worker/rest/api/feedback")
@Slf4j
@RequiredArgsConstructor
public class FeedbackApiController {
  private final AuthClient securityClient;
  private final FeedbackClient feedbackClient;
  

  @GetMapping
  public ResponseEntity<List<Feedback>> findAllFeedback()
  {
    final var feedbacks = feedbackClient.queryFeedbacks().findAll();
    return new ResponseEntity<>(feedbacks, HttpStatus.OK);
  }
  
  @GetMapping("/{taskIdOrFeedbackId}")
  public ResponseEntity<Feedback> getOneFeedback(@PathVariable("taskIdOrFeedbackId") String id)
  {
    final var feedback = feedbackClient.queryFeedbacks().findOneById(id);
    if(feedback.isEmpty()) {
     return ResponseEntity.notFound().build(); 
    }
    
    return new ResponseEntity<>(feedback.get(), HttpStatus.OK);
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
  
  @PutMapping("/{taskIdOrFeedbackId}")
  public ResponseEntity<Feedback> modifyOneFeedback(@PathVariable("taskIdOrFeedbackId") String id, @RequestBody ModifyOneFeedbackReplyCommand body)
  {
    final var feedback = feedbackClient.modifyOneFeedback(body, securityClient.getUser().getPrincipal().getUsername());
    return new ResponseEntity<>(feedback, HttpStatus.OK);
  }
}
