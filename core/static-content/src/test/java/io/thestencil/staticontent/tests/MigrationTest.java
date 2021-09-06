package io.thestencil.staticontent.tests;

/*-
 * #%L
 * stencil-static-content
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.thestencil.staticontent.api.StaticContentClient;
import io.thestencil.staticontent.spi.StaticContentClientDefault;
import io.thestencil.staticontent.tests.config.TestUtils;

public class MigrationTest {  
  final StaticContentClient client = StaticContentClientDefault.builder().build();
  
  @Test
  public void buildSite() throws IOException {
    final var src = new File("src/test/resources/migration").toPath();
    final var absPath = src.toAbsolutePath().toString();
    final var builder = StaticContentClientDefault.builder().build().markdown();
    Files.walk(src).filter(Files::isRegularFile).forEach(file -> {
      try {
        String absolutePath = file.toAbsolutePath().toString();
        String path = absolutePath.substring(absPath.length() + 1);
        byte[] bytes = FileUtils.readFileToByteArray(file.toFile());
        builder.md(path, bytes);
      } catch(IOException e) {
        throw new RuntimeException("Failed to read file: '" + file + "'!");
      }
    });
    final var md = builder.build();
    final var sites = StaticContentClientDefault.builder().build().sites().created(1l).source(md)
        .imagePath("/")
        .build();
    
    String expected = TestUtils.toString("migration-output.json");
    String actual = TestUtils.prettyPrint(sites);
    Assertions.assertEquals(expected, actual);   
  }
  
  //@Test
  public void migration() throws IOException {
    final var src = new File("src/test/resources/migration-1").toPath();
    final var absPath = src.toAbsolutePath().toString();
    final var builder = StaticContentClientDefault.builder().build().markdown();
    Files.walk(src).filter(Files::isRegularFile).forEach(file -> {
      try {
        String absolutePath = file.toAbsolutePath().toString();
        String path = absolutePath.substring(absPath.length() + 1);
        byte[] bytes = FileUtils.readFileToByteArray(file.toFile());
        builder.md(path, bytes);
      } catch(IOException e) {
        throw new RuntimeException("Failed to read file: '" + file + "'!");
      }
    });
    final var md = builder.build();
    final var sites = StaticContentClientDefault.builder().build().sites().created(1l).source(md)
        .imagePath("/")
        .build();
    
    String actual = TestUtils.prettyPrint(sites);
    
    FileUtils.writeByteArrayToFile(
        new File("src/test/resources/output-1.json"), 
        actual.getBytes(StandardCharsets.UTF_8));
    
  }
}
