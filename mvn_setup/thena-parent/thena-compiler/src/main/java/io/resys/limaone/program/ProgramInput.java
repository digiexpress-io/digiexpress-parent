package io.resys.limaone.program;

import java.io.Serializable;

import io.resys.limaone.model.Parameter;

public interface ProgramInput extends Serializable {
  Serializable getValue(Parameter parameter);
}