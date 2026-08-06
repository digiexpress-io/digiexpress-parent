package io.digiexpress.eveli.app.assets.demo_object;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.digiexpress.eveli.client.assets.property_object.api.PropertyObjectDescriptorFactory;
import io.digiexpress.eveli.client.assets.property_object.spi.DefaultPropertyObject;
import io.digiexpress.eveli.client.config.EveliAutoConfigAssets.EveliEditEnvir;
import io.resys.limaone.model.Model.BodyType;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/worker/rest/api/demo/default-property")
@RequiredArgsConstructor
public class DefaultPropertyController {
  private final EveliEditEnvir context;
  private final PropertyObjectDescriptorFactory descriptorFactory;
  @GetMapping
  public Uni<List<DefaultPropertyObject>> getDefaultProperties() 
  {
    var descriptor = descriptorFactory.getDefaultDescriptor();
    return context.getAuthoring().worldQuery()
        .docs(
          BodyType.PROPERTY_OBJECT
        )
        .findAll()
        .map(wm->wm.getPropertyObjects().values().stream()
            .filter(me-> "default".equals(me.getBody().getObjectType()))
            .map(model -> (DefaultPropertyObject)descriptor.convertToObject(model))
            .toList()
            );
  }
}
