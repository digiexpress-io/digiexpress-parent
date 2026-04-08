package io.digiexpress.eveli.client.config;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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


import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.digiexpress.eveli.client.api.AttachmentCommands;
import io.digiexpress.eveli.client.api.FeedbackClient;
import io.digiexpress.eveli.client.api.GamutAuthClient;
import io.digiexpress.eveli.client.api.GamutClient;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.config.EveliAutoConfigAssets.EveliEditEnvir;
import io.digiexpress.eveli.client.spi.dialob.DialobFillEventPublisher;
import io.digiexpress.eveli.client.spi.dialob.SyncDialobAndProcess;
import io.digiexpress.eveli.client.spi.gamut.GamutClientImpl;
import io.digiexpress.eveli.client.spi.mq.MqEventPublisher;
import io.digiexpress.eveli.client.web.resources.gamut.GamutFeedbackController;
import io.digiexpress.eveli.client.web.resources.gamut.GamutIamController;
import io.digiexpress.eveli.client.web.resources.gamut.GamutSiteController;
import io.digiexpress.eveli.client.web.resources.gamut.GamutUserActionsController;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Configuration
public class EveliAutoConfigGamut {

  @Bean
  public GamutClient gamutClient(
      io.resys.limaone.program.Runtime envir,
      TaskClient taskclient,
      AttachmentCommands attachmentCommands, 
      MqEventPublisher mqEventPublisher) {
    
    return new GamutClientImpl(taskclient, mqEventPublisher, attachmentCommands, envir);
  }
  
  @Bean
  public DialobFillEventPublisher dialobFillEventPublisher(
      io.resys.limaone.program.Runtime envir, 
      ApplicationEventPublisher publisher,
      TaskClient taskClient,
      SyncDialobAndProcess syncDialobAndProcess) {
    return new DialobFillEventPublisher(publisher, envir, syncDialobAndProcess, taskClient);
  }

  @Bean
  public GamutFeedbackController gamutFeedbackController(
      io.resys.limaone.program.Runtime runtime, 
      EveliPropsGamut props,  GamutClient gamutClient, 
      DialobFillEventPublisher publisher,
      GamutAuthClient auth) {
    return new GamutFeedbackController(gamutClient, runtime, publisher, auth);
  }

  @Bean
  public GamutIamController gamutIamController(GamutAuthClient crmClient) {
    return new GamutIamController(crmClient);
  }
  
  @Bean
  public GamutSiteController gamutSiteController(
      io.resys.limaone.program.Runtime envir, 
      FeedbackClient feedback, GamutAuthClient auth, 
      Optional<EveliEditEnvir> cockpitClient) {
    return new GamutSiteController(envir, cockpitClient.map(e -> e.getAuthoring()), feedback, auth);
  }

  @Bean
  public GamutUserActionsController gamutUserActionsController(
      io.resys.limaone.program.Runtime envir, 
      FeedbackClient feedback,
      GamutClient gamutClient,  
      GamutAuthClient crmClient,
      DialobFillEventPublisher publisher) {

    return new GamutUserActionsController(publisher, gamutClient, crmClient, envir, feedback);
  }
}
