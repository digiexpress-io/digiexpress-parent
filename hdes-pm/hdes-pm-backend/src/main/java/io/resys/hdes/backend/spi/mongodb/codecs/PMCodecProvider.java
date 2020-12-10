package io.resys.hdes.backend.spi.mongodb.codecs;

/*-
 * #%L
 * hdes-storage-mongodb
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

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

import io.resys.hdes.backend.api.PmRepository.Access;
import io.resys.hdes.backend.api.PmRepository.Project;
import io.resys.hdes.backend.api.PmRepository.User;

public class PMCodecProvider implements CodecProvider {

  private final ProjectCodec project = new ProjectCodec();
  private final ProjectCodec user = new ProjectCodec();
  private final ProjectCodec access = new ProjectCodec();
  
  
  @SuppressWarnings("unchecked")
  @Override
  public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry)  {
    
    if(Project.class.isAssignableFrom(clazz)) {
      return (Codec<T>) project;
    }
    if(User.class.isAssignableFrom(clazz)) {
      return (Codec<T>) user;
    }
    if(Access.class.isAssignableFrom(clazz)) {
      return (Codec<T>) access;
    }
    
    return null;
  }
}
