package io.digiexpress.client.api;

import java.io.Serializable;
import java.util.Optional;

import io.digiexpress.client.api.ComposerEntity.DefinitionState;
import io.digiexpress.client.api.ComposerEntity.HeadState;
import io.digiexpress.client.api.ComposerEntity.TagState;

public interface ComposerCache {


  ComposerCache withName(String name);
  void flush(String id);
  void flush();
  
  
  ComposerCacheEntry save(TagState src);
  Optional<TagState> getTagState(String id);  
  
  ComposerCacheEntry save(HeadState src);
  Optional<HeadState> getHeadState(String id);  
  
  ComposerCacheEntry save(DefinitionState src);
  Optional<DefinitionState> getDefinitionState(String id);
  
  interface ComposerCacheEntry extends Serializable {
    String getId();
    ComposerCacheEntryType getType();
    <T extends ComposerEntity> T getValue();
  }
  
  
  enum ComposerCacheEntryType {
    DEF_STATE, HEAD_STATE, TAG_STATE
  }
}
