package io.dialob.client.spi.composer;

import io.dialob.client.api.DialobClient.EnvirBuilder;
import io.dialob.client.api.DialobClient.ProgramEnvirValue;
import io.dialob.client.api.DialobDocument.FormDocument;
import io.dialob.client.api.DialobDocument.FormReleaseDocument;
import io.dialob.client.api.DialobDocument.FormRevisionDocument;
import io.dialob.client.api.DialobStore.StoreState;
import io.dialob.client.api.ImmutableComposerState;

public class ComposerEntityMapper {

  
  public static EnvirBuilder toEnvir(EnvirBuilder envirBuilder, StoreState source) {
    source.getRevs().values().forEach(v -> envirBuilder.addCommand().id(v.getId()).version(v.getVersion()).rev(v).build());
    source.getForms().values().forEach(v -> envirBuilder.addCommand().id(v.getId()).version(v.getVersion()).form(v).build());
    source.getTags().values().forEach(v -> envirBuilder.addCommand().id(v.getId()).version(v.getVersion()).release(v).build());
    
    return envirBuilder;
  }
  
  public static void toComposer(ImmutableComposerState.Builder builder, ProgramEnvirValue wrapper) {
    switch (wrapper.getSource().getBodyType()) {
    case FORM:
      builder.putForms(wrapper.getDocument().getId(), (FormDocument) wrapper.getDocument());
      break;
    case FORM_REV:
      builder.putRevs(wrapper.getDocument().getId(), (FormRevisionDocument) wrapper.getDocument());
      break;
    case RELEASE:
      builder.putReleases(wrapper.getDocument().getId(), (FormReleaseDocument) wrapper.getDocument());
      break;
    default:
      break;
    }
  }
}
