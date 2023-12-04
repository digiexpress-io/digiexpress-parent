import React from 'react';
import { TableHead, TableCell, TableRow, Box, Stack } from '@mui/material';

import Context from 'context';
import { TaskDescriptor, Group } from 'descriptor-task';
import TaskTable from '../TaskTable';
import { NavigationSticky } from '../NavigationSticky';
import { FilterStatus } from './FilterStatus';
import { FilterAssignees } from './FilterAssignees';
import { FilterRoles } from './FilterRoles';
import { FilterPriority } from './FilterPriority';
import { FilterColumns } from './FilterColumns';
import { FilterByString } from 'components-generic';
import { GroupBySelect } from './GroupBy';



function getRowBackgroundColor(index: number): string {
  const isOdd = index % 2 === 1;

  if (isOdd) {
    return 'uiElements.light';
  }
  return 'background.paper';
}

const Header: React.FC<TaskTable.TableConfigProps & { columns: (keyof TaskDescriptor)[] }> = ({ content, setContent, group, columns }) => {

  const includesTitle = columns.includes("title");

  const headersToShow = includesTitle ?
    columns.filter(c => c !== 'title') :
    columns.slice(1);

  return (
    <TableHead>
      <TableRow>

        { /* reserved title column */}
        <TableCell align='left' padding='none'>
          <TaskTable.Title group={group} />
          <TaskTable.SubTitle values={group.records.length} message='core.teamSpace.taskCount' />
        </TableCell>

        { /* without title */}
        <TaskTable.ColumnHeaders columns={headersToShow} content={content} setContent={setContent} />

        {/* menu column */}
        {columns.length > 0 && <TableCell></TableCell>}
      </TableRow>
    </TableHead>
  );
}

const Row: React.FC<{
  rowId: number,
  row: TaskDescriptor,
  def: Group,
  columns: (keyof TaskDescriptor)[]
}> = (props) => {

  const [hoverItemsActive, setHoverItemsActive] = React.useState(false);
  function handleEndHover() {
    setHoverItemsActive(false);
  }
  return (<TableRow sx={{ backgroundColor: getRowBackgroundColor(props.rowId) }} hover tabIndex={-1} key={props.row.id}
    onMouseEnter={() => setHoverItemsActive(true)} onMouseLeave={handleEndHover}>
    {props.columns.includes("title") && <TaskTable.CellTitle {...props} children={hoverItemsActive} />}
    {props.columns.includes("assignees") && <TaskTable.CellAssignees {...props} />}
    {props.columns.includes("dueDate") && <TaskTable.CellDueDate {...props} />}
    {props.columns.includes("priority") && <TaskTable.CellPriority {...props} />}
    {props.columns.includes("roles") && <TaskTable.CellRoles {...props} />}
    {props.columns.includes("status") && <TaskTable.CellStatus {...props} />}

    <TaskTable.CellMenu {...props} active={hoverItemsActive} setDisabled={handleEndHover} />
  </TableRow>);
}

const columnTypes: (keyof TaskDescriptor)[] = [
  'title',
  'assignees',
  'dueDate',
  'priority',
  'roles',
  'status']

const TaskSearch: React.FC<{}> = () => {
  const tasks = Context.useTasks();
  const [state, setState] = React.useState(tasks.state.toGroupsAndFilters());
  const [columns, setColumns] = React.useState([
    ...columnTypes
  ]);

  React.useEffect(() => {
    setState(prev => prev.withTasks(tasks.state))
  }, [tasks.state]);

  return (<Box>
    <NavigationSticky>
      <FilterByString onChange={({ target }) => setState(prev => prev.withSearchString(target.value))} />
      <Stack direction='row' spacing={1}>
        <GroupBySelect value={state.groupBy} onChange={(value) => setState(prev => prev.withGroupBy(value))} />
        <FilterStatus value={state.filterBy} onChange={(value) => setState(prev => prev.withFilterByStatus(value))} />
        <FilterPriority value={state.filterBy} onChange={(value) => setState(prev => prev.withFilterByPriority(value))} />
        <FilterAssignees value={state.filterBy} onChange={(value) => setState(prev => prev.withFilterByOwner(value))} />
        <FilterRoles value={state.filterBy} onChange={(value) => setState(prev => prev.withFilterByRoles(value))} />
        <FilterColumns types={columnTypes} value={columns} onChange={(value) => setColumns(value)} />
      </Stack>
    </NavigationSticky>
    <Box mt={1} />
    <TaskTable.Groups groups={state.groups} orderBy='created'>
      {{
        Header: (props) => <Header columns={columns} {...props} />,
        Rows: ({ content, group, loading }) => (
          <TaskTable.TableBody>
            {content.entries.map((row, rowId) => (<Row key={row.id} rowId={rowId} row={row} def={group} columns={columns} />))}
            <TaskTable.TableFiller content={content} loading={loading} plusColSpan={7} />
          </TaskTable.TableBody>
        )
      }}
    </TaskTable.Groups>
  </Box>
  );
}


export default TaskSearch;