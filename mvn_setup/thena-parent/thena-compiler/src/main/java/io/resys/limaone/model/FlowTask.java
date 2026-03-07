package io.resys.limaone.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.Model.Body;
import io.resys.limaone.model.Model.BodyType;



@Value.Immutable
@JsonSerialize(as = ImmutableFlowTask.class)
@JsonDeserialize(as = ImmutableFlowTask.class)
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
  
  interface FlowTaskExecutable {}
  enum FlowTaskPropType { TYPE_0, TYPE_1, TYPE_2 }
  
  default BodyType getBodyType() {
    return BodyType.FLOW_TASK;
  }
}
