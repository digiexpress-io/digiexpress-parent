import React from 'react';
import { TableHead, TableCell, TableRow } from '@mui/material';

import client from '@taskclient';
import TaskTable from '../TaskTable';
import Tools from '../TaskTools';


function getRowBackgroundColor(index: number): string {
  const isOdd = index % 2 === 1;

  if (isOdd) {
    return 'uiElements.light';
  }
  return 'background.paper';
}

const Header: React.FC<TaskTable.TableConfigProps> = ({ content, setContent, group }) => {

  const columns: (keyof client.TaskDescriptor)[] = React.useMemo(() => [
    'assignees',
    'dueDate',
    'priority',
    'roles',
    'status'
  ], []);

  return (
    <TableHead>
      <TableRow>
        <TableCell align='left' padding='none'>
          <TaskTable.Title group={group} />
          <TaskTable.SubTitle values={group.records.length} message='core.teamSpace.taskCount' />
        </TableCell>

        <TaskTable.ColumnHeaders columns={columns} content={content} setContent={setContent} />
        <TableCell></TableCell>
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

  return (<TableRow sx={{ backgroundColor: getRowBackgroundColor(props.rowId) }} hover tabIndex={-1} key={props.row.id} onMouseEnter={() => setHoverItemsActive(true)} onMouseLeave={handleEndHover}>
    <TaskTable.CellTitle {...props} children={hoverItemsActive} />
    <TaskTable.CellAssignees {...props} />
    <TaskTable.CellDueDate {...props} />
    <TaskTable.CellPriority {...props} />
    <TaskTable.CellRoles {...props} />
    <TaskTable.CellStatus {...props} />
    <TaskTable.CellMenu {...props} active={hoverItemsActive} setDisabled={handleEndHover} />
  </TableRow>);
}


const Rows: React.FC<TaskTable.TableConfigProps> = ({ content, group, loading }) => {
  return (
    <TaskTable.TableBody>
      {content.entries.map((row, rowId) => (<Row key={row.id} rowId={rowId} row={row} def={group} />))}

      <TaskTable.TableFiller content={content} loading={loading} plusColSpan={7} />
    </TaskTable.TableBody>
  )
}


const AdminBoard: React.FC<{}> = () => {
  return (
    <TaskTable.Groups groupBy={undefined} orderBy='created'>
      {{ Header, Rows, Tools }}
    </TaskTable.Groups>);
}


export default AdminBoard;