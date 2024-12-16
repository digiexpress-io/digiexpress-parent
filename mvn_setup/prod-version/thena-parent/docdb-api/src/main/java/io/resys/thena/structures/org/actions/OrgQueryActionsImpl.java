package io.resys.thena.structures.org.actions;

/*-
 * #%L
 * thena-docdb-api
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

import io.resys.thena.api.actions.OrgQueryActions;
import io.resys.thena.spi.DbState;
import io.resys.thena.structures.org.queries.MemberHierarchyQueryImpl;
import io.resys.thena.structures.org.queries.MemberObjectsQueryImpl;
import io.resys.thena.structures.org.queries.PartyHierarchyQueryImpl;
import io.resys.thena.structures.org.queries.RightHierarchyQueryImpl;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrgQueryActionsImpl implements OrgQueryActions {
  private final DbState state;
  private final String repoId;
  
  @Override
  public MemberObjectsQuery memberQuery() {
    return new MemberObjectsQueryImpl(state, repoId);
  }

	@Override
	public MemberHierarchyQuery memberHierarchyQuery() {
		return new MemberHierarchyQueryImpl(state, repoId);
	}

  @Override
  public PartyHierarchyQuery partyHierarchyQuery() {
    return new PartyHierarchyQueryImpl(state, repoId);
  }

  @Override
  public RightHierarchyQuery rightHierarchyQuery() {
    return new RightHierarchyQueryImpl(state, repoId);
  }
}
