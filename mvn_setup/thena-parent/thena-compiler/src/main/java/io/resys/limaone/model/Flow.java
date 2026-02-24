package io.resys.limaone.model;

import io.resys.limaone.model.Model.Body;

public interface Flow extends Body {
  String getFlowName();
  String getFlowValue();
}
