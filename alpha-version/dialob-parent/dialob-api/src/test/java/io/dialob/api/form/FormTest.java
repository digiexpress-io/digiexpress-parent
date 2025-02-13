package io.dialob.api.form;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

public class FormTest {

  ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void metadataIsRequired() {
    ConstraintViolationException exception = Assertions.assertThrows(ConstraintViolationException.class, () -> ImmutableForm.builder().build());
    assertEquals(1, exception.getConstraintViolations().size());
    ConstraintViolation constraintViolation = exception.getConstraintViolations().iterator().next();

    assertEquals("must not be null", constraintViolation.getMessage());
    assertEquals("metadata", constraintViolation.getPropertyPath().toString());
  }


  @Test
  public void metadataLabelIsRequired() {
    ConstraintViolationException exception = Assertions.assertThrows(ConstraintViolationException.class, () ->
      ImmutableForm.builder().metadata(ImmutableFormMetadata.builder().build()).build());
    assertEquals(1, exception.getConstraintViolations().size());
    ConstraintViolation constraintViolation = exception.getConstraintViolations().iterator().next();

    assertEquals("must not be null", constraintViolation.getMessage());
    assertEquals("metadata.label", constraintViolation.getPropertyPath().toString());
  }

  @Test
  public void testFormItemAdditionalProperties() throws Exception {
    Form form = ImmutableForm.builder().metadata(ImmutableFormMetadata.builder().label("laabeli").putAdditionalProperties("extra","value").build())
      .addValueSets(ImmutableFormValueSet.builder()
        .id("vs1")
        .addEntries(ImmutableFormValueSetEntry.builder().id("id1").putLabel("fi","ota1").putAdditionalProperties("selite","extravalue").build())
        .putAdditionalProperties("extraItem","valuee")
        .build()).build();
    String expected = "{\"metadata\":{\"label\":\"laabeli\",\"extra\":\"value\"},\"valueSets\":[{\"id\":\"vs1\",\"entries\":[{\"id\":\"id1\",\"label\":{\"fi\":\"ota1\"},\"selite\":\"extravalue\"}],\"extraItem\":\"valuee\"}]}";
    assertEquals(expected, objectMapper.writeValueAsString(form));

    Form form2 = objectMapper.readValue(expected, Form.class);
    assertNotSame(form, form2);
    assertEquals(form, form2);

  }

}
