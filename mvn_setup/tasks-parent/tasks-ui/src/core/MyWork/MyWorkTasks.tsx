import React from 'react';
import { Box } from '@mui/material';
import client from '@taskclient';
import Styles from '@styles';
import MyWorkTasksTableHeader from './MyWorkTasksTableHeader';
import MyWorkTasksTableRow from './MyWorkTasksTableRow';



const Rows: React.FC<{
  content: client.TablePagination<client.TaskDescriptor>,
  def: client.Group,
  loading: boolean
}> = ({ content, def, loading }) => {
  return (
    <Styles.TaskTable.TableBody>
      {content.entries.map((row, rowId) => (<MyWorkTasksTableRow key={row.id} rowId={rowId} row={row} def={def} />))}
      <Styles.TaskTable.TableRowEmpty content={content} loading={loading} plusColSpan={5} />
    </Styles.TaskTable.TableBody>
  )
}


const TasksTable: React.FC<{ def: client.Group, loading: boolean }> = (props) => {
  const { loading } = props;

  return (
    <client.Table<client.TaskDescriptor, { def: client.Group }>
      data={{ loading, records: props.def.records, defaultOrderBy: 'created' }}
      render={{
        ext: props,
        Header: MyWorkTasksTableHeader,
        Rows: Rows
      }}
    />
  );
}


const MyWorkTasks: React.FC<{}> = () => {
  const tasks = client.useTasks();
  const { loading } = tasks;
  const groupByMyWork =  tasks.state.withGroupBy('myWorkType');

  console.log(groupByMyWork);


  return (
    <>
      {groupByMyWork.groups.map((group, index) => (
        <React.Fragment key={group.id}>
          {index > 0 ? <Box sx={{ p: 2 }} /> : null}
          <TasksTable def={group} loading={loading} />
        </React.Fragment>
      ))}
    </>
  );
}

export { MyWorkTasks };
