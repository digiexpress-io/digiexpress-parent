package io.resys.limaone.persistence;

/*-
 * #%L
 * limaone-compiler
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

import java.time.OffsetDateTime;

import io.resys.limaone.authoring.Authoring.AuthorProps;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.smallrye.mutiny.Uni;



public abstract class AuthoringTemplate<IMPL, MODEL> {

  protected final AuthoringConfig config;

  protected AuthorProps author;
  protected String authorCached;
  abstract Uni<MODEL> build();

  public AuthoringTemplate(AuthoringConfig config) {
    super();
    this.config = config;
    
    // resolve/cache author 
    getAuthor();
  }


  @SuppressWarnings("unchecked")
  public IMPL author(AuthorProps author) {
    this.author = author;
    return (IMPL) this;
  }
  
  
  protected String getAuthor() {
    if(authorCached == null) {
      authorCached = getAuthorFromImpl();
    }
    return authorCached;
  }
  
  
  private String getAuthorFromImpl() {
    if(author != null && author.getAuthor() != null) {
      return author.getAuthor();  
    }
    return config.getEnvir().getCurrentUser().get().getUserName();
  }
  
  protected OffsetDateTime getCreatedAt() {
    if(author != null && author.getCreatedAt() != null) {
      return author.getCreatedAt();  
    }
    return OffsetDateTime.now();
  }
  
  
  public MODEL buildSync() {
    return build()
        .runSubscriptionOn(config.getEnvir().getWorkerPool())
        .await().atMost(config.getEnvir().getWorkerPoolMaxTimeout());
  }
}
