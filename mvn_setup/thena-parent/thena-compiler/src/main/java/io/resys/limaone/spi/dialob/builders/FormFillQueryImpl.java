package io.resys.limaone.spi.dialob.builders;

import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import io.resys.limaone.spi.dialob.FormDb.FormFillQuery;
import io.resys.limaone.spi.dialob.FormDbImpl.FormDbProps;
import io.resys.limaone.spi.http.HttpClient.RawResponse;
import io.resys.limaone.spi.http.HttpClientImpl.RawResponseImpl;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class FormFillQueryImpl implements FormFillQuery {
  private final FormDbProps db;
  
  @Override
  public Uni<RawResponse> getOne(String formInstanceId) {
    Objects.requireNonNull(formInstanceId, () -> "formInstanceId can't be null");;
    if(FormFillBuilderImpl.isInvalidId(formInstanceId)) {
      return Uni.createFrom().item(new RawResponseImpl(ResponseEntity.status(HttpStatus.BAD_REQUEST).build()));
    }
    return db.getQuestionnaireHttp().httpQuery()
        .uri(uri -> uri.append(formInstanceId).build())
        .method(String.class)
        .getOneAsRaw();
  }

}
