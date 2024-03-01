package io.resys.thena.docdb.store.sql.queries;

import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroupAndRoleFlattened;
import io.resys.thena.docdb.models.org.OrgQueries;
import io.smallrye.mutiny.Multi;

public class OrgGroupAndRoleFlattenedQuerySqlPool implements OrgQueries.GroupAndRoleFlattenedQuery {

	@Override
	public Multi<OrgGroupAndRoleFlattened> findAllByUserId(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

}
