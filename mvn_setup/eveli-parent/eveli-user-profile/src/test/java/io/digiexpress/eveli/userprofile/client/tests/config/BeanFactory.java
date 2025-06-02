package io.digiexpress.eveli.userprofile.client.tests.config;

import io.digiexpress.eveli.userprofile.client.api.model.ImmutableArchiveUserProfile;
import io.digiexpress.eveli.userprofile.client.api.model.ImmutableChangeNotificationSetting;
import io.digiexpress.eveli.userprofile.client.api.model.ImmutableChangeUserDetailsEmail;
import io.digiexpress.eveli.userprofile.client.api.model.ImmutableChangeUserDetailsFirstName;
import io.digiexpress.eveli.userprofile.client.api.model.ImmutableChangeUserDetailsLastName;
import io.digiexpress.eveli.userprofile.client.api.model.ImmutableCreateUserProfile;
import io.digiexpress.eveli.userprofile.client.api.model.ImmutableNotificationSetting;
import io.digiexpress.eveli.userprofile.client.api.model.ImmutableUpsertUserProfile;
import io.digiexpress.eveli.userprofile.client.api.model.ImmutableUserDetails;
import io.digiexpress.eveli.userprofile.client.api.model.ImmutableUserProfile;
import io.quarkus.runtime.annotations.RegisterForReflection;
/*-
 * #%L
 * thena-quarkus-dev-app
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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
import jakarta.enterprise.context.Dependent;

@Dependent
@RegisterForReflection(targets = {
    ImmutableUserProfile.class,
    ImmutableCreateUserProfile.class,
    ImmutableUpsertUserProfile.class,    
    ImmutableUserDetails.class,
    ImmutableNotificationSetting.class,
    ImmutableChangeUserDetailsFirstName.class,
    ImmutableChangeUserDetailsLastName.class,
    ImmutableChangeUserDetailsEmail.class,
    ImmutableChangeNotificationSetting.class,
    ImmutableArchiveUserProfile.class
})
public class BeanFactory {


}
