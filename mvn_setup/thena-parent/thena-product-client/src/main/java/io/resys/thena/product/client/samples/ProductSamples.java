package io.resys.thena.product.client.samples;

/*-
 * #%L
 * thena-product-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import io.resys.thena.product.client.api.Product;

public class ProductSamples {
  
  public static Product feemiSavingsInsurance() {
    return Product_Feemi_Savings.create();
  }
  
  public static Product feemiPensionInsurance() {
    return Product_Feemi_Pension.create();
  }
  
  public static Product feemiPsInsurance() {
    return Product_Feemi_PS.create();
  }
  
  public static Product novaVirtusEndowment() {
    return Product_Nova_Virtus.create();
  }
  
  protected static String loadResourceText(String resourcePath) {
    try (InputStream is = ProductSamples.class.getClassLoader().getResourceAsStream(resourcePath)) {
      if (is == null) {
        throw new IllegalArgumentException("Resource not found: " + resourcePath);
      }
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException("Failed to load resource: " + resourcePath, e);
    }
  }
}