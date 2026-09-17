package io.digiexpress.eveli.client.test.metis.search;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import java.util.ArrayList;
import java.util.Locale;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;

/**
 * Hashed bag of words in place of a real embedding model. Texts that share words end up
 * close to each other under cosine distance, which is all the search queries rely on, and
 * no test needs a running Ollama.
 */
public class StubEmbeddingModel implements EmbeddingModel {

  public static final int DIMENSIONS = 1024;

  @Override
  public EmbeddingResponse call(EmbeddingRequest request) {
    final var embeddings = new ArrayList<Embedding>();
    for (int index = 0; index < request.getInstructions().size(); index++) {
      embeddings.add(new Embedding(vectorOf(request.getInstructions().get(index)), index));
    }
    return new EmbeddingResponse(embeddings);
  }

  @Override
  public float[] embed(Document document) {
    return vectorOf(document.getText());
  }

  @Override
  public int dimensions() {
    return DIMENSIONS;
  }

  private float[] vectorOf(String text) {
    final var vector = new float[DIMENSIONS];
    if (text == null || text.isBlank()) {
      vector[0] = 1f;
      return vector;
    }
    for (final var word : text.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+")) {
      if (word.isEmpty()) {
        continue;
      }
      vector[Math.floorMod(word.hashCode(), DIMENSIONS)] += 1f;
    }

    var length = 0d;
    for (final var value : vector) {
      length += value * value;
    }
    length = Math.sqrt(length);
    if (length == 0) {
      vector[0] = 1f;
      return vector;
    }
    for (int index = 0; index < vector.length; index++) {
      vector[index] /= (float) length;
    }
    return vector;
  }
}
