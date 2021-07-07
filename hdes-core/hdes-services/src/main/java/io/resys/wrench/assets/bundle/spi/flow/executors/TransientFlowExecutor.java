package io.resys.wrench.assets.bundle.spi.flow.executors;

/*-
 * #%L
 * wrench-component-assets
 * %%
 * Copyright (C) 2016 - 2018 Copyright 2016 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.resys.wrench.assets.bundle.api.repositories.AssetServiceRepository.Service;
import io.resys.wrench.assets.bundle.api.repositories.AssetServiceRepository.ServiceDataModel;
import io.resys.wrench.assets.bundle.api.repositories.AssetServiceRepository.ServiceResponse;
import io.resys.wrench.assets.bundle.spi.exceptions.AssetErrorCodes;
import io.resys.wrench.assets.bundle.spi.exceptions.DataException;
import io.resys.wrench.assets.bundle.spi.exceptions.Message;
import io.resys.wrench.assets.bundle.spi.exceptions.MessageList;
import io.resys.wrench.assets.datatype.api.DataTypeRepository.DataType;
import io.resys.wrench.assets.datatype.api.DataTypeRepository.Direction;
import io.resys.wrench.assets.datatype.api.DataTypeRepository.ValueType;
import io.resys.wrench.assets.datatype.spi.deserializers.DataTypeException;
import io.resys.wrench.assets.flow.api.model.Flow;
import io.resys.wrench.assets.flow.api.model.Flow.FlowTaskStatus;
import io.resys.wrench.assets.flow.api.model.FlowModel.FlowTaskModel;
import io.resys.wrench.assets.flow.spi.FlowException;

public class TransientFlowExecutor {
  private final ObjectMapper objectMapper;

  public TransientFlowExecutor(ObjectMapper objectMapper) {
    super();
    this.objectMapper = objectMapper;
  }

  public Map.Entry<Flow, ObjectNode> execute(Service service, JsonNode debugInput) {
    ServiceDataModel dataModel = service.getDataModel();

    // Create input
    try {
      Map<String, Serializable> input = createInput(dataModel.getParams(), debugInput);


      // Required params
      List<Message> errors = getErrors(input, dataModel);
      if(!errors.isEmpty()) {
        throw new DataException(new MessageList().setStatus(422).addAll(errors));
      }

      // Run service
      ServiceResponse response = service.newExecution().insert((Serializable) input).run();
      Flow flow = response.get();

      return new AbstractMap.SimpleImmutableEntry<Flow, ObjectNode>(flow, createOutput(flow));
    } catch(FlowException e) {
      Message error = AssetErrorCodes.FLOW_EXEC_ERROR.newMessage(e.getMessage());
      throw new DataException(new MessageList().setStatus(422).add(error));
    } catch(DataTypeException e) {
      Message error = AssetErrorCodes.FLOW_PROPERTY_INVALID.newMessage(e.getDataType().getName());
      throw new DataException(new MessageList().setStatus(422).add(error));
    }
  }

  protected List<Message> getErrors(Map<String, Serializable> input, ServiceDataModel dataModel) {
    List<Message> errors = new ArrayList<>();
    for(DataType param : dataModel.getParams()) {
      if(param.getDirection() != Direction.IN || !param.isRequired()) {
        continue;
      }

      if(input.get(param.getName()) == null) {
        errors.add(AssetErrorCodes.FLOW_PROPERTY_REQUIRED.newMessage(param.getName()));
      }
    }
    return errors;
  }

  protected ObjectNode createOutput(Flow flow) {
    ObjectNode output = objectMapper.createObjectNode();
    flow.getContext().getTasks().stream()
    .filter(t -> t.getStatus() == FlowTaskStatus.COMPLETED)
    .forEach(t -> {

      FlowTaskModel taskModel = flow.getModel().getTask().get(t.getModelId());
      if(taskModel.getBody() != null) {
        JsonNode node = objectMapper.valueToTree(t.getVariables().get(t.getModelId()));
        output.set(taskModel.getId(), node);
      }
    });

    return output;
  }

  protected Map<String, Serializable> createInput(List<DataType> params, JsonNode input) {
    Map<String, Serializable> result = new HashMap<>();

    if(input.isNull() || input == null) {
      return result;
    }
    Assert.isTrue(input.isObject(), "input can only be object node!");

    for(DataType param : params) {
      if(param.getDirection() != Direction.IN) {
        continue;
      }
      JsonNode jsonNode = input.get(param.getName());
      if(jsonNode == null) {
        continue;
      }

      Serializable value;
      if (param.getValueType() == ValueType.ARRAY && jsonNode.isTextual()) {
        try {
          value = param.toValue(objectMapper.readValue(jsonNode.asText(), ArrayNode.class));
        } catch (Exception e) {
          continue;
        }
      } else {
        value = param.toValue(jsonNode);
      }
      result.put(param.getName(), value);
    }
    return result;
  }

}
