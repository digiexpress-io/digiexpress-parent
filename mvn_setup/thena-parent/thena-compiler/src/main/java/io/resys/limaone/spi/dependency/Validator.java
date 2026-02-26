package io.resys.limaone.spi.dependency;

import java.util.List;

import io.resys.limaone.program.Program.ProgramMessage;
import io.resys.limaone.program.Program.ProgramStatus;

public interface Validator {
  List<ProgramMessage> validate();
  
  
  interface ValidatorResult {
    List<ProgramMessage> getMessages();
    ProgramStatus getProgramStatus();
  }
}