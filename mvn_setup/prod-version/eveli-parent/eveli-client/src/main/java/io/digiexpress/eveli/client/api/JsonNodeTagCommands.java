package io.digiexpress.eveli.client.api;

import com.fasterxml.jackson.databind.JsonNode;

public interface JsonNodeTagCommands<T> extends AssetTagCommands<T> {
  JsonNode getTagAssets(String tag);
}
