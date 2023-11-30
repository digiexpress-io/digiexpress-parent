package io.dialob.client.spi.program;

import io.dialob.client.api.DialobClient;
import io.dialob.client.api.DialobClient.ProgramBuilder;
import io.dialob.client.api.DialobDocument.FormDocument;
import io.dialob.client.spi.support.DialobAssert;
import io.dialob.compiler.DialobProgramFromFormCompiler;
import io.dialob.program.DialobProgram;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class ProgramBuilderImpl implements DialobClient.ProgramBuilder {

  private final DialobProgramFromFormCompiler compiler;

  private FormDocument form;

  @Override
  public ProgramBuilder form(FormDocument form) {
    this.form = form;
    return this;
  }

  @Override
  public DialobProgram build() {
    DialobAssert.notNull(form, () -> "form can't be null!");

    log.debug("Compiling form document {} rev {}", form.getData().getId(), form.getData().getRev());
    return compiler.compileForm(form.getData());
  }

}
