package io.digiexpress.eveli.client.config;

import java.util.Optional;

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


import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.digiexpress.eveli.client.api.AttachmentCommands;
import io.digiexpress.eveli.client.api.FeedbackClient;
import io.digiexpress.eveli.client.api.GamutAuthClient;
import io.digiexpress.eveli.client.api.GamutClient;
import io.digiexpress.eveli.client.api.ProcessClient;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.spi.dialob.DialobFillEventPublisher;
import io.digiexpress.eveli.client.spi.dialob.SyncDialobAndProcess;
import io.digiexpress.eveli.client.spi.gamut.GamutClientImpl;
import io.digiexpress.eveli.client.spi.mq.MqEventPublisher;
import io.digiexpress.eveli.client.web.resources.gamut.GamutFeedbackController;
import io.digiexpress.eveli.client.web.resources.gamut.GamutIamController;
import io.digiexpress.eveli.client.web.resources.gamut.GamutSiteController;
import io.digiexpress.eveli.client.web.resources.gamut.GamutUserActionsController;
import io.digiexpress.eveli.dialob.api.DialobClient;
import io.digiexpress.eveli.envir.api.EveliEnvirClient;
import io.digiexpress.thena.cockpit.client.api.CockpitClient;
import lombok.extern.slf4j.Slf4j;



@Configuration
@Slf4j
public class EveliAutoConfigGamut {
  
  @Bean
  public GamutClient gamutClient(
      ProcessClient processRepository,
      TaskClient taskclient,
      AttachmentCommands attachmentCommands,
      EveliEnvirClient envir, 
      DialobClient dialobCommands,
      GamutAuthClient authClient,
      MqEventPublisher mqEventPublisher
    ) {
    
    return new GamutClientImpl(
        processRepository, 
        taskclient, 
        mqEventPublisher,
        attachmentCommands, 
        dialobCommands,
        authClient,
        envir
        
    );
  }
  
  @Bean
  public DialobFillEventPublisher dialobFillEventPublisher(
      ApplicationEventPublisher publisher,
      ProcessClient processClient,
      DialobClient dialobClient,
      SyncDialobAndProcess syncDialobAndProcess
  ) {
    return new DialobFillEventPublisher(publisher, processClient, dialobClient, syncDialobAndProcess);
  }
  
  @Bean
  public GamutFeedbackController gamutFeedbackController(
      EveliPropsGamut props, GamutClient gamutClient, 
      DialobClient dialobClient, 
      DialobFillEventPublisher publisher,
      GamutAuthClient auth) {
    return new GamutFeedbackController(gamutClient, dialobClient, publisher, auth);
  }

  @Bean
  public GamutIamController gamutIamController(GamutAuthClient crmClient) {
    return new GamutIamController(crmClient);
  }
  
  @Bean
  public GamutSiteController gamutSiteController(
      EveliEnvirClient envir, FeedbackClient feedback, GamutAuthClient auth, Optional<CockpitClient> cockpitClient) {
    
    return new GamutSiteController(envir, feedback, auth, cockpitClient);
  }
  
  @Bean
  public GamutUserActionsController gamutUserActionsController(
      FeedbackClient feedback,
      GamutClient gamutClient, DialobClient dialobClient, GamutAuthClient crmClient, ProcessClient processRepository,
      DialobFillEventPublisher publisher
      ) {
    return new GamutUserActionsController(publisher, gamutClient, crmClient, dialobClient, processRepository, feedback);
  }
}
