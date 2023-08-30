import React from 'react';
import { TableHead, TableCell, TableRow } from '@mui/material';

import client from '@taskclient';
import TaskTable from '../TaskTable';



const Header: React.FC<TaskTable.RenderProps> = ({ content, setContent, group }) => {
  const columns: (keyof client.TaskDescriptor)[] = React.useMemo(() => [
    'dueDate',
    'priority',
    'status',
  ], []);

  return (
    <TableHead>
      <TableRow>
        <TableCell align='left' padding='none'>
          <TaskTable.Title group={group} />
          <TaskTable.SubTitle values={group.records.length} message='core.teamSpace.taskCount' />
        </TableCell>
        <TaskTable.ColumnHeaders columns={columns} content={content} setContent={setContent} />
      </TableRow>
    </TableHead>
  );
}


const Row: React.FC<{
  rowId: number,
  row: client.TaskDescriptor,
  def: client.Group
}> = (props) => {

  const [hoverItemsActive, setHoverItemsActive] = React.useState(false);
  function handleEndHover() {
    setHoverItemsActive(false);
  }
  function handleStartHover() {
    setHoverItemsActive(true);
  }

  const cells = React.useMemo(() => {
    return (
      <>
        <TaskTable.CellDueDate {...props} />
        <TaskTable.CellPriority {...props} />
        <TaskTable.CellStatus {...props} />
      </>
    );
  }, [props]);

  return (<>
    <TableRow hover tabIndex={-1} key={props.row.id} onMouseEnter={handleStartHover} onMouseLeave={handleEndHover}>
      <TaskTable.CellTitleCrm {...props} active={hoverItemsActive} setDisabled={handleEndHover} />
      {cells}
      <TaskTable.CellMenu {...props} active={hoverItemsActive} setDisabled={handleEndHover} />
    </TableRow>
  </>);
}


const Rows: React.FC<TaskTable.RenderProps> = ({ content, group, loading }) => {
  return (
    <TaskTable.TableBody>
      {content.entries.map((row, rowId) => (<Row key={row.id} rowId={rowId} row={row} def={group} />))}

      <TaskTable.TableFiller content={content} loading={loading} plusColSpan={5} />
    </TaskTable.TableBody>
  )
}


const MyWork: React.FC<{}> = () => {
  return (
    <TaskTable.Groups groupBy='assignee' orderBy='created'>
      {{ Header, Rows, Tools: undefined }}
    </TaskTable.Groups>);
}


export default MyWork;