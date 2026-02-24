package io.resys.limaone.model;

import io.resys.limaone.model.Model.Body;

public interface FlowTask extends Body {
  String getTaskName();
  String getTaskValue();
}
