package io.resys.limaone.model;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.Model.Body;
import io.resys.limaone.model.Model.BodyType;

@Value.Immutable
@JsonSerialize(as = ImmutablePrintout.class)
@JsonDeserialize(as = ImmutablePrintout.class)
public interface Printout extends Body {
  String getId();
  String getServiceName(); // human readable name, what IS this PDF
  String getOrchestratorName(); // external name/id that will be called to resolve data/ most likely wrench flow name
  
  List<LocaleLabel> getLabels(); // localized labels, human readable names
  default BodyType getBodyType() { return BodyType.PRINTOUT; };

}
