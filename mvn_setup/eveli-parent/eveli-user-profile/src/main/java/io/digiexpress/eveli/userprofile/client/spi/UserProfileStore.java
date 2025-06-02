package io.digiexpress.eveli.userprofile.client.spi;

/*-
 * #%L
 * eveli-user-profile
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

import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.spi.DocStoreImpl;
import io.resys.thena.spi.ThenaDocConfig;



public class UserProfileStore extends DocStoreImpl<UserProfileStore> {

  public UserProfileStore(ThenaDocConfig config, DocStoreFactory<UserProfileStore> factory) {
    super(config, factory);
  }

  public static Builder<UserProfileStore> builder() {
    final DocStoreFactory<UserProfileStore> factory = (config, delegate) -> new UserProfileStore(config, delegate);
    return new Builder<UserProfileStore>(factory);
  }
  
  @Override
  public StoreTenantQuery<UserProfileStore> query() {
    return super.query().repoType(StructureType.doc);
  }
}
