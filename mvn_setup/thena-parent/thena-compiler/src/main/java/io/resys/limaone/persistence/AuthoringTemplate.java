package io.resys.limaone.persistence;

import java.time.OffsetDateTime;

import io.resys.limaone.authoring.Authoring.AuthorProps;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public abstract class AuthoringTemplate<IMPL, MODEL> {

  protected final AuthoringConfig config;
  protected AuthorProps author;
  abstract Uni<MODEL> build();
  

  @SuppressWarnings("unchecked")
  public IMPL author(AuthorProps author) {
    this.author = author;
    return (IMPL) this;
  }
  
  
  protected String getAuthor() {
    if(author != null && author.getAuthor() != null) {
      return author.getAuthor();  
    }
    return config.getAuthor().get();
  }
  
  protected OffsetDateTime getCreatedAt() {
    if(author != null && author.getCreatedAt() != null) {
      return author.getCreatedAt();  
    }
    return OffsetDateTime.now();
  }
  
  
  public MODEL buildSync() {
    return build()
        .runSubscriptionOn(config.getWorkerPool())
        .await().atMost(config.getWorkerTimeout());
  }
}
