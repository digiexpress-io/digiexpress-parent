package io.digiexpress.eveli.appwrenchonly;

/*-
 * #%L
 * eveli-app-wrench-only
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

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.resys.hdes.client.spi.spring.GitAutoConfig;
import io.resys.hdes.client.spi.spring.GitConfigProps;



@ConditionalOnProperty(prefix = "wrench.assets.git", name = "enabled", havingValue = "true")
@Configuration
@EnableConfigurationProperties(value = {
  GitConfigProps.class 
})
@Import(GitAutoConfig.class)
public class GitAutoConfiguration {

}
