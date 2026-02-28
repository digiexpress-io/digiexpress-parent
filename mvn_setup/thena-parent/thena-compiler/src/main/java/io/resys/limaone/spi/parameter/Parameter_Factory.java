package io.resys.limaone.spi.parameter;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.resys.limaone.model.ImmutableParameter;
import io.resys.limaone.model.Parameter;
import io.resys.limaone.model.Parameter.Deserializer;
import io.resys.limaone.model.Parameter.Direction;
import io.resys.limaone.model.Parameter.Serializer;
import io.resys.limaone.model.Parameter.ValueType;
import io.resys.thena.support.RepoAssert;

public class Parameter_Factory {
  private static Map<ValueType, Deserializer> deserializers;
  private static Map<ValueType, Serializer> serializers;
  private static Parameter.ValueTypeResolver valueTypeResolver;
  
  static {
    final Map<ValueType, Deserializer> deserializers = new HashMap<>();
    Parameter_Factory.deserializers = Collections.unmodifiableMap(deserializers);

    deserializers.put(ValueType.ARRAY, new GenericDataTypeDeserializer(List.class));
    deserializers.put(ValueType.DURATION, null);
    deserializers.put(ValueType.PERIOD, null);
    deserializers.put(ValueType.TIME, new TimeDataTypeDeserializer());

    deserializers.put(ValueType.OBJECT, new JsonObjectDataTypeDeserializer());
    deserializers.put(ValueType.INTL, new IntlJsonObjectDataTypeDeserializer());
    deserializers.put(ValueType.STRING, new GenericDataTypeDeserializer(String.class));
    deserializers.put(ValueType.BOOLEAN, new GenericDataTypeDeserializer(Boolean.class));
    deserializers.put(ValueType.DECIMAL, new GenericDataTypeDeserializer(BigDecimal.class));
    deserializers.put(ValueType.INTEGER, new GenericDataTypeDeserializer(Integer.class));
    deserializers.put(ValueType.LONG, new GenericDataTypeDeserializer(Long.class));
    deserializers.put(ValueType.PERCENT, new GenericDataTypeDeserializer(BigDecimal.class));
    deserializers.put(ValueType.DATE, new DateDataTypeDeserializer());
    deserializers.put(ValueType.DATE_TIME, new DateTimeDataTypeDeserializer());

    final Map<ValueType, Serializer> serializers = new HashMap<>();
    Parameter_Factory.serializers = Collections.unmodifiableMap(serializers);

    Serializer dataTypeSerializer = new GenericDataTypeSerializer();
    serializers.put(ValueType.ARRAY, dataTypeSerializer);
    serializers.put(ValueType.OBJECT, dataTypeSerializer);
    serializers.put(ValueType.DURATION, dataTypeSerializer);
    serializers.put(ValueType.PERIOD, dataTypeSerializer);
    serializers.put(ValueType.TIME, dataTypeSerializer);
    serializers.put(ValueType.STRING, dataTypeSerializer);
    serializers.put(ValueType.BOOLEAN, dataTypeSerializer);
    serializers.put(ValueType.DECIMAL, dataTypeSerializer);
    serializers.put(ValueType.INTEGER, dataTypeSerializer);
    serializers.put(ValueType.LONG, dataTypeSerializer);
    serializers.put(ValueType.PERCENT, dataTypeSerializer);
    serializers.put(ValueType.DATE, dataTypeSerializer);
    serializers.put(ValueType.DATE_TIME, dataTypeSerializer);
    serializers.put(ValueType.INTL, dataTypeSerializer);

    Map<Class<?>, ValueType> valueTypes = new HashMap<>();
    valueTypes.put(List.class, ValueType.ARRAY);
    valueTypes.put(Duration.class, ValueType.DURATION);
    valueTypes.put(Period.class, ValueType.PERIOD);
    valueTypes.put(LocalTime.class, ValueType.TIME);
    valueTypes.put(String.class, ValueType.STRING);
    valueTypes.put(Boolean.class, ValueType.BOOLEAN);
    valueTypes.put(BigDecimal.class, ValueType.DECIMAL);
    valueTypes.put(Integer.class, ValueType.INTEGER);
    valueTypes.put(Long.class, ValueType.LONG);
    valueTypes.put(LocalDate.class, ValueType.DATE);
    valueTypes.put(LocalDateTime.class, ValueType.DATE_TIME);
    
    Parameter_Factory.valueTypeResolver = src -> valueTypes.containsKey(src) ? valueTypes.get(src) : ValueType.OBJECT;
  }
  
  
  public static NewAttribute newParam() {
    return new NewAttribute();
  }
  
  
  public static class NewAttribute {
    private Boolean required = true;
    private String name;
    private String extRef;
    private ValueType valueType;
    private Direction direction;
    private Class<?> beanType;
    private String description;
    private String values;
    private List<Parameter> properties = new ArrayList<>();
    private String ref;
    private Parameter dataType;
    private Integer order;
    private String script;
    private String id;
    private List<String> valueSet;
    private boolean data = true;
  
  
    public NewAttribute required(boolean required) {
      this.required = required;
      return this;
    }
  
    public NewAttribute data(boolean data) {
      this.data = data;
      return this;
    }
  
    public NewAttribute name(String name) {
      this.name = name;
      return this;
    }
  
    public NewAttribute order(Integer order) {
      this.order = order;
      return this;
    }
  
    public NewAttribute script(String script) {
      this.script = script;
      return this;
    }
  
    public NewAttribute id(String id) {
      this.id = id;
      return this;
    }
  
    public NewAttribute extRef(String extRef) {
      this.extRef = extRef;
      return this;
    }
  
    public NewAttribute valueType(ValueType valueType) {
      this.valueType = valueType;
      return this;
    }
  
    public NewAttribute direction(Direction direction) {
      this.direction = direction;
      return this;
    }
  
    public NewAttribute beanType(Class<?> beanType) {
      this.beanType = beanType;
      return this;
    }
  
    public NewAttribute description(String description) {
      this.description = description;
      return this;
    }
  
    public NewAttribute values(String values) {
      this.values = values;
      return this;
    }
  
    public NewAttribute ref(String ref, Parameter dataType) {
      RepoAssert.isTrue(ref != null, () -> "ref can't be null!");
      RepoAssert.isTrue(dataType != null, () -> "dataType can't be null for ref: " + ref + "!");
      this.dataType = dataType;
      return this;
    }
  
    public NewAttribute property() {
      return new NewAttribute() {
        @Override
        public Parameter build() {
          final var property = super.build();
          properties.add(property);
          return property;
        }
      };
    }
  
    public NewAttribute valueSet(List<String> valueSet) {
      this.valueSet = valueSet;
      return this;
    }
    
    public Parameter build() {
      RepoAssert.notNull(name, () -> "name can't be null!");
  
      if(dataType != null) {
        valueType = dataType.getValueType();
        properties.addAll(dataType.getProperties());
  
        Deserializer deserializer = dataType.getDeserializer();
        Serializer serializer = dataType.getSerializer();
        return ImmutableParameter.builder()
            .id(id).script(script).order(order)
            .name(name).ref(ref).description(description)
            .direction(direction)
            .valueType(valueType)
            .beanType(beanType)
            .isRequired(Boolean.TRUE.equals(required))
            .values(values)
            .extRef(extRef)
            .properties(properties)
            .valueSet(valueSet)
            .deserializer(deserializer)
            .serializer(serializer)
            .build();
      }
  
      if(valueType == null) {
        RepoAssert.notNull(beanType, () -> "beanType can't be null!");
        valueType = valueTypeResolver.get(beanType);
      }
  
      Deserializer deserializer = deserializers.get(valueType);
      Serializer serializer = serializers.get(valueType);
  
      RepoAssert.notNull(valueType, () -> "valueType can't be null!");
      return ImmutableParameter.builder()
          .id(id).script(script).order(order)
          .name(name)
          .ref(ref)
          .extRef(extRef)
          .data(data)
          .description(description)
          .direction(direction)
          .valueType(valueType)
          .beanType(beanType)
          .isRequired(Boolean.TRUE.equals(required))
          .values(values)
          .properties(properties)
          .valueSet(valueSet)
          .deserializer(deserializer)
          .serializer(serializer)
          .build();
    }
  }
}