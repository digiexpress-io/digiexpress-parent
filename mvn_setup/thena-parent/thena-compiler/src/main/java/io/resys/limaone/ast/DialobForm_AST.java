package io.resys.limaone.ast;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.dialob.api.form.Form;


@Value.Immutable
@JsonSerialize(as = ImmutableDialobForm_AST.class)
@JsonDeserialize(as = ImmutableDialobForm_AST.class)
public interface DialobForm_AST extends Simple_AST {
  Form getForm();
}
