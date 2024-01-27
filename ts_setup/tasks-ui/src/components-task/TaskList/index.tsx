import React from 'react';
import { TablePagination } from '@mui/material';


import { NavigationButton, LayoutList, LayoutListItem, LayoutListFiller } from 'components-generic';
import { TaskDescriptor } from 'descriptor-task';

import { TaskListState, initTable, initTabs, TaskListTabState } from './types';
import { StyledEditTaskButton, StyledStartTaskButton } from './TaskListStyles';
import TaskCreateDialog from '../TaskCreate';
import { cyan } from 'components-colors';


const TaskList: React.FC<{
  state: TaskListTabState[]
  children: {
    TaskItem: React.ElementType<{ task: TaskDescriptor }>;
    TaskItemActive: React.ElementType<{ task: TaskDescriptor | undefined }>;
  }
}> = ({ state: initTabsState, children }) => {

  const [state, setState] = React.useState<TaskListState>(initTabs([]));
  const [table, setTable] = React.useState(initTable([]));
  const [createOpen, setCreateOpen] = React.useState(false);

  function handleTaskCreate() {
    setCreateOpen(prev => !prev)
  }

  function handleActiveTab(newValue: number) {
    setState(prev => prev.withActiveTab(newValue));
  }

  function handleActiveTask(task: TaskDescriptor | undefined) {
    setState(prev => prev.withActiveTask(task));
  }

  function handleOnPageChange(_garbageEvent: any, newPage: number) {
    setTable((state) => state.withPage(newPage));
  }

  function handleOnRowsPerPageChange(event: React.ChangeEvent<HTMLInputElement>) {
    setTable((state) => state.withRowsPerPage(parseInt(event.target.value, 10)))
  }

  React.useEffect(() => {
    if (state.tabs.length === 0) {
      return;
    }
    const { activeTab } = state;
    const { records } = state.tabs[activeTab].group;
    setTable((src) => src.withSrc(records).withPage(0));
  }, [state, setTable])

  React.useEffect(() => {
    setState(prev => prev.withTabs(initTabsState))
  }, [initTabsState]);

  if (state.tabs.length === 0) {
    return null;
  }

  const { TaskItem, TaskItemActive } = children;

  const navigation = (
  <>
    {state.tabs.map(tab => (
      <NavigationButton
        id={tab.label}
        values={{ count: tab.count }}
        key={tab.id}
        active={tab.selected}
        color={tab.color}
        onClick={() => handleActiveTab(tab.id)} />
    ))
    }

    <NavigationButton
      id='core.taskCreate.newTask'
      onClick={handleTaskCreate}
      values={undefined}
      active={createOpen}
      color={cyan}/>
  </>)

  const active = (<TaskItemActive task={state.activeTask} />);
  const items = (<>
      {table.entries.map((task, index) => (
      <LayoutListItem key={task.id} index={index} active={state.activeTask?.id === task.id} onClick={() => handleActiveTask(task)}>
        <TaskItem key={task.id} task={task} />
      </LayoutListItem>)
    )}
    <LayoutListFiller value={table} />
  </>);

  const pagination = (<TablePagination component="div"
    rowsPerPageOptions={table.rowsPerPageOptions}
    count={table.src.length}
    rowsPerPage={table.rowsPerPage}
    page={table.page}
    onPageChange={handleOnPageChange}
    onRowsPerPageChange={handleOnRowsPerPageChange} />);

  return (<>
    <TaskCreateDialog open={createOpen} onClose={handleTaskCreate} />
    <LayoutList slots={{navigation, active, items, pagination}} />
  </>);
}

export type { TaskListTabState };
export { TaskList, StyledEditTaskButton, StyledStartTaskButton };



