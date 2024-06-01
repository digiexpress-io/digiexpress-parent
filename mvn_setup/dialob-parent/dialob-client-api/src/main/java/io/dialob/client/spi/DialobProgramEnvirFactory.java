package io.dialob.client.spi;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.exception.ExceptionUtils;

import io.dialob.api.form.Form;
import io.dialob.client.api.DialobCache;
import io.dialob.client.api.DialobClient.ProgramEnvir;
import io.dialob.client.api.DialobClient.ProgramMessage;
import io.dialob.client.api.DialobClient.ProgramStatus;
import io.dialob.client.api.DialobClient.ProgramWrapper;
import io.dialob.client.api.DialobClientConfig;
import io.dialob.client.api.ImmutableProgramMessage;
import io.dialob.client.api.ImmutableProgramWrapper;
import io.dialob.client.spi.program.ImmutableProgramEnvir;
import io.dialob.client.spi.program.ProgramBuilderImpl;
import io.dialob.compiler.DialobProgramErrorsException;
import io.dialob.compiler.DialobProgramFromFormCompiler;
import io.dialob.program.DialobProgram;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DialobProgramEnvirFactory {
  private final DialobClientConfig config;
  private final DialobProgramFromFormCompiler compiler;
  private final DialobCache cache;

  private final List<String> visitedIds = new ArrayList<>();
  private final List<String> cachlessIds = new ArrayList<>();
  private final StringBuilder treelog = new StringBuilder();
  private final List<ProgramWrapper> programs = new ArrayList<>();
  private final List<ProgramWrapper> envirValue = new ArrayList<>();

  private ProgramEnvir baseEnvir;

  public DialobProgramEnvirFactory(DialobClientConfig config) {
    super();
    this.cache = config.getCache();
    this.compiler = config.getCompiler();
    this.config = config;
  }

  public DialobProgramEnvirFactory add(ProgramEnvir envir) {
    this.baseEnvir = envir;
    return this;
  }
  
  public DialobProgramEnvirFactory add(String id, String version, String json, boolean cachless) {
    if(cachless) {
      cachlessIds.add(id);
    }

    final var form = visitForm(id, version, json);
    visitWrapper(form);
    visitedIds.add(id);
    return this;
  }

  public ProgramEnvir build() {
    final var envir = ImmutableProgramEnvir.builder();
    if(baseEnvir != null) {
      baseEnvir.findAll().stream()
        .filter(wrapper -> !visitedIds.contains(wrapper.getId()))
        .forEach(wrapper -> visitWrapper(wrapper));
    }

    for(final var wrapper : programs) {
      visitTreeLog(wrapper);
      envir.add(wrapper);
    }
    for(final var wrapper : envirValue) {
      visitTreeLog(wrapper);
      envir.add(wrapper);
    }

    /*
    log.info(new StringBuilder()
        .append("Envir status: " + treelog.length()).append(System.lineSeparator())
        .append(treelog.toString())
        .toString());
*/
    return envir.build();
  }

  private void visitWrapper(ProgramWrapper value) {
    if(value instanceof ProgramWrapper) {
      this.programs.add((ProgramWrapper) value);
    } else {
      this.envirValue.add(value);
    }
  }

  private void visitTreeLog(ProgramWrapper value) {
    final var wrapper = (ProgramWrapper) value;
    final String name = wrapper.getDocument().getName();

    treelog.append("  - ").append(name).append(": ").append(wrapper.getStatus()).append(System.lineSeparator());
    if(wrapper.getStatus() != ProgramStatus.UP) {

      for(final var error : wrapper.getErrors()) {
        treelog.append("    - ").append(error.getId()).append(": ").append(error.getMsg()).append(System.lineSeparator());
        if(error.getException() != null) {
          String stack = ExceptionUtils.getStackTrace(error.getException());
          if(stack.length() > 100) {
            stack = stack.substring(0, 100);
          }
          treelog.append("      ").append(stack).append(System.lineSeparator());
        }
      }
    }
  }

  private ProgramWrapper visitForm(String id, String version, String json) {
    final var builder = ImmutableProgramWrapper.builder();
    builder.status(ProgramStatus.UP);

    Form ast = null;
    if(cachlessIds.contains(id)) {
      ast = new JsonObject(json).mapTo(Form.class);
    } else {
      final var cached = cache.getAst(id, version);
      if(cached.isPresent()) {
        ast = (Form) cached.get();
      } else {
        ast = new JsonObject(json).mapTo(Form.class);
        cache.setAst(ast);
      }
    }



    DialobProgram program = null;
    if(ast != null) {
      try {
        if(cachlessIds.contains(id)) {
          program = new ProgramBuilderImpl(compiler).form(ast).build();
        } else {
          final var cached = cache.getProgram(id, version);
          if(cached.isPresent()) {
            program = cached.get();
          } else {
            program = new ProgramBuilderImpl(compiler).form(ast).build();
            cache.setProgram(program, ast);
          }
        }
      } catch(Exception e) {
        log.error(new StringBuilder()
            .append(e.getMessage()).append(System.lineSeparator())
            .append("  - form source: ").append(json)
            .toString(), e);


        builder.status(ProgramStatus.PROGRAM_ERROR).addAllErrors(visitException(e));
      }
    }
    return builder.id(id)
        .document(ast)
        .program(Optional.ofNullable(program))
        .build();
  }

  private List<ProgramMessage> visitException(Exception e) {
    final var msgs = new ArrayList<ProgramMessage>();
    if(e instanceof DialobProgramErrorsException) {
      ((DialobProgramErrorsException) e).getErrors().stream().map(error ->
        ImmutableProgramMessage.builder()
        .id("compiler-error")
        .src(error)
        .build()
      ).forEach(msgs::add);

    }

    msgs.add(ImmutableProgramMessage.builder()
      .id("exception")
      .msg(e.getMessage() == null ? "no-desc-available": e.getMessage().replaceAll("\"", "'"))
      .exception(e)
      .build());
    return msgs;
  }

  public DialobClientConfig getConfig() {
    return config;
  }

}
