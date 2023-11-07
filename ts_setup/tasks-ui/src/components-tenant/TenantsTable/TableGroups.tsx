
import React from 'react';
import { Box } from '@mui/material';
import Context from 'context';
import { TableConfigProps, CustomTable } from './table-ctx';
import { Group, TenantEntryDescriptor } from 'descriptor-tenant';



interface TableGroupsProps {
  groups: Group[];
  orderBy: keyof TenantEntryDescriptor | undefined,

  children: {
    Header: React.ElementType<TableConfigProps>;
    Rows: React.ElementType<TableConfigProps>;
  }
}

const Delegate: React.FC<TableGroupsProps> = ({ groups, orderBy, children }) => {
  const projects = Context.useTenants();
  const { loading } = projects;

  const defaultOrderBy = React.useMemo(() => {
    if (orderBy) {
      return orderBy;
    }
    return "lastSaved";
  }, [orderBy]);

  const result = groups.map((group, index) => (
    <React.Fragment key={group.id}>
      {index > 0 ? <Box sx={{ p: 2 }} /> : null}
      <CustomTable
        data={{ loading, group, defaultOrderBy }}
        config={{
          Header: children.Header,
          Rows: children.Rows
        }}
      />
    </React.Fragment>
  ));
  return (<>{result}</>);
}


const TableGroups: React.FC<TableGroupsProps> = (props) => {
  return (<Delegate {...props} />);
}

export { TableGroups };


