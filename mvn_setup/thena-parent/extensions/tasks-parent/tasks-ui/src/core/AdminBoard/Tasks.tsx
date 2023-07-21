import React from 'react';
import { Box } from '@mui/material';

import client from '@taskclient';
import Tools from '../TaskTools';



import TableHeader from './TasksTableHeader';
import TableRows from './TasksTableRow';


const TasksTable: React.FC<{ def: client.Group, loading: boolean }> = (props) => {
  const { loading } = props;

  return (
    <client.Table<client.TaskDescriptor, { def: client.Group }>
      data={{ loading, records: props.def.records, defaultOrderBy: 'created' }}
      render={{
        ext: props,
        Header: TableHeader,
        Rows: TableRows
      }}
    />
  );
}



const Tasks: React.FC<{}> = () => {
  const tasks = client.useTasks();
  const { loading } = tasks;

  return (<Tools><>
    {tasks.state.groups.map((group, index) => (
      <React.Fragment key={group.id}>
        {index > 0 ? <Box sx={{ p: 2 }} /> : null}
        <TasksTable def={group} loading={loading} />
      </React.Fragment>))}
  </>
  </Tools>);
}

export { Tasks };
