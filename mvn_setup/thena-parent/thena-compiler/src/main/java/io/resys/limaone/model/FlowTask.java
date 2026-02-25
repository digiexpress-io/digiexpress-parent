package io.resys.limaone.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.resys.limaone.model.Model.Body;

public interface FlowTask extends Body {
  String getTaskName();
  String getTaskValue();
  
  
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @interface ServiceData {
    String value() default "";
  }

  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @interface ServiceRefs {
    ServiceRef[] value();
  }
  
  @Repeatable(ServiceRefs.class)
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @interface ServiceRef {
    String value();
    Model.BodyType type();
  }
}
