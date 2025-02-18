package io.digiexpress.mig.client.spi.converters;

import java.util.Collection;

import io.digiexpress.mig.client.api.MigClient.StencilEntityConverter;
import io.digiexpress.mig.client.api.SourceTasks.SourceWorkflow;
import io.thestencil.client.api.StencilClient.EntityType;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class StencilEntityConverterImpl implements StencilEntityConverter {

  private final Collection<SourceWorkflow> source;  
  
  @Override
  public JsonObject convertValue(JsonObject blob) {    
    if(EntityType.WORKFLOW.name().equals(blob.getValue("type"))) {
      return convertWorkflowValue(blob);
    }

    return blob;
  }

  public JsonObject convertWorkflowValue(JsonObject blob) {
    final var body = blob.getJsonObject("body");
    final var value = body.getString("value");
    
    final var found = source.stream().filter(f -> f.getName().equals(value)).findFirst();
    if(found.isEmpty()) {
      return blob;
    }
    
    final var src = found.get();
    
    body.put("formId", src.getForm_id());
    body.put("formName", src.getForm_name());
    body.put("formTag", src.getForm_tag());
    body.put("flowName", src.getFlow_name().orElse(null));
    blob.put("body", body);
    return blob;
    
  }  
}
