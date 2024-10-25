package io.digiexpress.eveli.app;

/*-
 * #%L
 * eveli-app
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

import io.digiexpress.eveli.client.config.EveliAssetAutoConfig;
import io.digiexpress.eveli.client.config.EveliAutoConfig;
import io.digiexpress.eveli.client.config.EveliDbAutoConfig;

@SpringBootApplication
@EnableAutoConfiguration
@EnableAsync
@Import(value = { EveliDbAutoConfig.class, EveliAssetAutoConfig.class, EveliAutoConfig.class })
public class Application {
  public static void main(String[] args) throws Exception {
    SpringApplication.run(new Class<?>[]{Application.class}, args);
  }
}
