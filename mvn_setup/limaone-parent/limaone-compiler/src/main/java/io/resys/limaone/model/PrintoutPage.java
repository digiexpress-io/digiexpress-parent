package io.resys.limaone.model;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.Model.Body;
import io.resys.limaone.model.Model.BodyType;

@Value.Immutable
@JsonSerialize(as = ImmutablePrintoutPage.class)
@JsonDeserialize(as = ImmutablePrintoutPage.class)
public interface PrintoutPage extends Body {
  String getId();
  String getContent(); // the markdown definition
  
  String getLocaleId();
  String getServiceId();
  
  default BodyType getBodyType() { return BodyType.ARTICLE_PAGE; };
}
