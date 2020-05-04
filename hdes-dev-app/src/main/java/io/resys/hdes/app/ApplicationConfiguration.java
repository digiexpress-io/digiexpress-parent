package io.resys.hdes.app;

/*-
 * #%L
 * hdes-dev-app
 * %%
 * Copyright (C) 2020 Copyright 2020 ReSys OÜ
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

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.arc.DefaultBean;
import io.resys.hdes.app.service.api.ApplicationService;
import io.resys.hdes.app.service.spi.GenericApplicationService;

@Dependent
public class ApplicationConfiguration {
  
  @Inject
  @ConfigProperty(name = "storage-service.folder.path")
  String source;
  
  @Produces
  @Singleton
  @DefaultBean
  public ApplicationService applicationService() {
    
    
    //new File(source)
   
    return new GenericApplicationService(null);
  }
}
