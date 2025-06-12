package io.digiexpress.eveli.dialob.api;

import io.dialob.api.form.Form;
import io.dialob.api.proto.Actions;
import io.dialob.api.questionnaire.Questionnaire;

public interface DialobReviewClient {

  ReviewBuilder createReview();
  
  interface ReviewBuilder {
    ReviewBuilder form(Form form);
    ReviewBuilder formData(Questionnaire formData);
    Actions build();
  }
}