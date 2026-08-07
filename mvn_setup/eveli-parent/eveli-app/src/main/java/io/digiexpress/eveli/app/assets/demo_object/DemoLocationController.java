package io.digiexpress.eveli.app.assets.demo_object;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.digiexpress.eveli.client.assets.property_object.api.PropertyObjectDescriptorFactory;
import io.digiexpress.eveli.client.config.EveliAutoConfigAssets.EveliEditEnvir;
import io.resys.limaone.model.Model.BodyType;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/worker/rest/api/demo/demo-location")
@RequiredArgsConstructor
public class DemoLocationController {
  private final EveliEditEnvir context;
  private final PropertyObjectDescriptorFactory descriptorFactory;
  @GetMapping
  public Uni<List<DemoLocationPropertyObject>> getDemoLocations() 
  {
    var descriptor = descriptorFactory.getDescriptor(DemoLocationPropertyObjectDescriptor.OBJECT_TYPE);
    return context.getAuthoring().worldQuery()
        .docs(
          BodyType.PROPERTY_OBJECT
        )
        .findAll()
        .map(wm->wm.getPropertyObjects().values().stream()
            .filter(me-> DemoLocationPropertyObjectDescriptor.OBJECT_TYPE.equals(me.getBody().getObjectType()))
            .map(model -> (DemoLocationPropertyObject)descriptor.get().convertToObject(model))
            .toList()
            );
  }
}
