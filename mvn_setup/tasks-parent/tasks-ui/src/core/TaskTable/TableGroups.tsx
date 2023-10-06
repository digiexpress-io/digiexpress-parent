
import React from 'react';
import { Box } from '@mui/material';
import Client from '@taskclient';
import { TableConfigProps, CustomTable } from './table-ctx';




interface TableGroupsProps {
  groupBy: Client.GroupBy | undefined,
  orderBy: keyof Client.TaskDescriptor | undefined,

  children: {
    Header: React.ElementType<TableConfigProps>;
    Rows: React.ElementType<TableConfigProps>;
    Tools: React.ElementType<{ children: React.ReactNode }> | undefined
  }
}

const Delegate: React.FC<TableGroupsProps> = ({ groupBy, orderBy, children }) => {
  const tasks = Client.useTasks();
  const { loading, state } = tasks;

  const groups = React.useMemo(() => {
    if (groupBy) {
      return state.withGroupBy(groupBy).groups;
    }
    return state.groups;
  }, [state, groupBy]);

  const defaultOrderBy = React.useMemo(() => {
    if (orderBy) {
      return orderBy;
    }
    return "created";
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

  if(children.Tools) {
    return (<children.Tools children={result}/>);
  }

  return (<>{result}</>);
}


const TableGroups: React.FC<TableGroupsProps> = (props) => {
  return (<Delegate {...props} />);
}

export { TableGroups };


