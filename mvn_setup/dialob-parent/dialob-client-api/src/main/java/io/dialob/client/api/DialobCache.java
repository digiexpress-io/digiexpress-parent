package io.dialob.client.api;

import java.io.Serializable;
import java.util.Optional;

import org.immutables.value.Value;

import io.dialob.api.form.Form;
import io.dialob.program.DialobProgram;

public interface DialobCache {

  DialobCache withName(String name);
  
  Optional<DialobProgram> getProgram(String id, String version);  
  Optional<Form> getAst(String id, String version);
  
  DialobProgram setProgram(DialobProgram program, Form src);
  Form setAst(Form src);
  
  void flush(String id);
  
  @Value.Immutable
  interface CacheEntry extends Serializable {
    String getId();
    String getRev();
    Form getAst();
    Optional<DialobProgram> getProgram();
  }

}
