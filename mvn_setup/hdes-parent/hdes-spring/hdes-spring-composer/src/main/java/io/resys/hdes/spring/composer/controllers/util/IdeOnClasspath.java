package io.resys.hdes.spring.composer.controllers.util;

/*-
 * #%L
 * hdes-spring-composer
 * %%
 * Copyright (C) 2020 - 2021 Copyright 2020 ReSys OÜ
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

import java.util.List;

public class IdeOnClasspath {
  private final boolean enabled;
  private final String hash;
  private final String manifest;
  private final List<String> css;
  private final String mainJs;

  public IdeOnClasspath(String hash, List<String> css, String manifest, String mainJs) {
    super();
    this.hash = hash;
    this.enabled = true;
    
    this.manifest = manifest;
    this.css = css;
    this.mainJs = mainJs;
  }

  public IdeOnClasspath() {
    this.hash = null;
    this.enabled = false;
    
    this.manifest = null;
    this.css = null;
    this.mainJs = null;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public String getHash() {
    return hash;
  }

  public String getManifest() {
    return manifest;
  }

  public List<String> getCss() {
    return css;
  }

  public String getMainJs() {
    return mainJs;
  }

  @Override
  public String toString() {
    return "        hash: " + hash + System.lineSeparator() 
        + "        manifest: " + manifest + System.lineSeparator()
        + "        css: " + css + System.lineSeparator()
        + "        mainJs: " + mainJs + System.lineSeparator();
  }
}
