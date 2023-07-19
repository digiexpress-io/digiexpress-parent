package io.dialob.client.spi;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.exception.ExceptionUtils;

import io.dialob.client.api.DialobCache;
import io.dialob.client.api.DialobClient.ProgramEnvir;
import io.dialob.client.api.DialobClient.ProgramEnvirValue;
import io.dialob.client.api.DialobClient.ProgramMessage;
import io.dialob.client.api.DialobClient.ProgramStatus;
import io.dialob.client.api.DialobClient.ProgramWrapper;
import io.dialob.client.api.DialobClient.ReleaseWrapper;
import io.dialob.client.api.DialobClient.RevisionWrapper;
import io.dialob.client.api.DialobClient.TypesMapper;
import io.dialob.client.api.DialobClientConfig;
import io.dialob.client.api.DialobDocument.DocumentType;
import io.dialob.client.api.DialobDocument.FormDocument;
import io.dialob.client.api.DialobDocument.FormReleaseDocument;
import io.dialob.client.api.DialobDocument.FormRevisionDocument;
import io.dialob.client.api.DialobStore.StoreEntity;
import io.dialob.client.api.ImmutableProgramMessage;
import io.dialob.client.api.ImmutableProgramWrapper;
import io.dialob.client.api.ImmutableReleaseWrapper;
import io.dialob.client.api.ImmutableRevisionWrapper;
import io.dialob.client.spi.program.ImmutableProgramEnvir;
import io.dialob.client.spi.program.ProgramBuilderImpl;
import io.dialob.compiler.DialobProgramErrorsException;
import io.dialob.compiler.DialobProgramFromFormCompiler;
import io.dialob.program.DialobProgram;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DialobProgramEnvirFactory {
  private final DialobClientConfig config;
  private final TypesMapper mapper;
  private final DialobProgramFromFormCompiler compiler;
  private final DialobCache cache;
  
  private final List<String> visitedIds = new ArrayList<>();
  private final List<String> cachlessIds = new ArrayList<>();
  private final StringBuilder treelog = new StringBuilder();
  private final List<ProgramWrapper> programs = new ArrayList<>();
  private final List<ProgramEnvirValue<?>> envirValue = new ArrayList<>();
  
  private ProgramEnvir baseEnvir;
  
  public DialobProgramEnvirFactory(DialobClientConfig config) {
    super();
    this.mapper = config.getMapper();
    this.cache = config.getCache();
    this.compiler = config.getCompiler();
    this.config = config;
  }
  
  public DialobProgramEnvirFactory add(ProgramEnvir envir) {
    this.baseEnvir = envir;
    return this;
  }
  public DialobProgramEnvirFactory add(StoreEntity entity, boolean cachless) {
    if(cachless) {
      cachlessIds.add(entity.getId());
    }
    
    switch (entity.getBodyType()) {
    case FORM: {
      final var form = visitForm(entity);
      visitWrapper(form);
      break;
    }
    case FORM_REV: {
      final var rev = visitRevision(entity); 
      visitWrapper(rev);
      break;
    }
    case RELEASE: {
      final var rel = visitRelease(entity); 
      visitWrapper(rel);
      break;      
    }
    default: throw new IllegalArgumentException("unknown command format type: '" + entity.getBodyType() + "'!");
    }
    
    visitedIds.add(entity.getId());
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
    LOGGER.info(new StringBuilder()
        .append("Envir status: " + treelog.length()).append(System.lineSeparator())
        .append(treelog.toString())
        .toString());
*/
    return envir.build();
  }
  
  private void visitWrapper(ProgramEnvirValue<?> value) {
    if(value instanceof ProgramWrapper) {
      this.programs.add((ProgramWrapper) value);
    } else {
      this.envirValue.add(value);
    }
  }
  
  private void visitTreeLog(ProgramEnvirValue<?> value) {
    if(value.getSource().getBodyType() == DocumentType.FORM) {
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

  }
  
  private ProgramWrapper visitForm(StoreEntity src) {
    final var builder = ImmutableProgramWrapper.builder();
    builder.status(ProgramStatus.UP);
    
    FormDocument ast = null;
    if(cachlessIds.contains(src.getId())) {
      ast = mapper.toFormDoc(src);
    } else {
      final var cached = cache.getAst(src);
      if(cached.isPresent()) {
        ast = (FormDocument) cached.get();
      } else {
        ast = mapper.toFormDoc(src);
        cache.setAst(ast, src);
      }
    }
    
    
    
    DialobProgram program = null;
    if(ast != null) {
      try {
        if(cachlessIds.contains(src.getId())) {
          program = new ProgramBuilderImpl(compiler).form(ast).build();
        } else {
          final var cached = cache.getProgram(src);
          if(cached.isPresent()) {
            program = cached.get();
          } else {
            program = new ProgramBuilderImpl(compiler).form(ast).build();
            cache.setProgram(program, src);
          }
        }
      } catch(Exception e) {
        LOGGER.error(new StringBuilder()
            .append(e.getMessage()).append(System.lineSeparator())
            .append("  - form source: ").append(src.getBody())
            .toString(), e);
        

        builder.status(ProgramStatus.PROGRAM_ERROR).addAllErrors(visitException(e));
      }
    }
    return builder.id(src.getId())
        .document(ast)
        .program(Optional.ofNullable(program))
        .source(src)
        .build(); 
  }
  
  private RevisionWrapper visitRevision(StoreEntity src) {
    
    FormRevisionDocument ast = null;
    if(cachlessIds.contains(src.getId())) {
      ast = mapper.toFormRevDoc(src);
    } else {
      final var cached = cache.getAst(src);
      if(cached.isPresent()) {
        ast = (FormRevisionDocument) cached.get();
      } else {
        ast = mapper.toFormRevDoc(src);
        cache.setAst(ast, src);
      }
    }
    
    return ImmutableRevisionWrapper.builder().document(ast).source(src).build();

  }
  
  private ReleaseWrapper visitRelease(StoreEntity src) {
    
    FormReleaseDocument ast = null;
    if(cachlessIds.contains(src.getId())) {
      ast = mapper.toFormReleaseDoc(src);
    } else {
      final var cached = cache.getAst(src);
      if(cached.isPresent()) {
        ast = (FormReleaseDocument) cached.get();
      } else {
        ast = mapper.toFormReleaseDoc(src);
        cache.setAst(ast, src);
      }
    }
    
    return ImmutableReleaseWrapper.builder().document(ast).source(src).build();
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
