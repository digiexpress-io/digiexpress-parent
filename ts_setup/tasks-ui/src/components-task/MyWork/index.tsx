import React from 'react';
import { TablePagination } from '@mui/material';


import { LayoutList, NavigationButton, LayoutListItem, LayoutListFiller  } from 'components-generic';
import { moss, cyan } from 'components-colors';
import { AssigneePalette, AssigneeGroupType } from 'descriptor-task';

import TaskCreateDialog from '../TaskCreate';
import { MyWorkProvider, useMyWork } from './MyWorkContext';
import TaskItemActive from './TaskItemActive';
import TaskItem from './TaskItem';


const MyWorkPagination: React.FC = () => {
  const { setTabRowsPerPage, setTabPageNo, table } = useMyWork();
  return (<TablePagination component="div"
    rowsPerPageOptions={table.rowsPerPageOptions}
    count={table.src.length}
    rowsPerPage={table.rowsPerPage}
    page={table.page}
    onPageChange={setTabPageNo}
    onRowsPerPageChange={setTabRowsPerPage} />);
}


const MyWorkNavigation: React.FC = () => {
  const { setActiveTab, activeTab, getTabItemCount } = useMyWork();
  const { id } = activeTab;
  const [createOpen, setCreateOpen] = React.useState(false);

  function handleTaskCreate() {
    setCreateOpen(prev => !prev)
  }

  function getGroupCount(id: AssigneeGroupType) {
    return { count: getTabItemCount(id) };
  }

  return (<>
    <NavigationButton id='core.myWork.tab.task.currentlyWorking' 
      values={getGroupCount('assigneeCurrentlyWorking')} 
      color={AssigneePalette.assigneeCurrentlyWorking}
      active={id === 'assigneeCurrentlyWorking'}
      onClick={() => setActiveTab("assigneeCurrentlyWorking")} />

    <NavigationButton id='core.myWork.tab.task.overdue' 
      values={getGroupCount('assigneeOverdue')} 
      color={AssigneePalette.assigneeOverdue}
      active={id === 'assigneeOverdue'}
      onClick={() => setActiveTab("assigneeOverdue")} />

    <NavigationButton id='core.myWork.tab.task.startsToday' 
      values={getGroupCount('assigneeStartsToday')} 
      color={AssigneePalette.assigneeStartsToday}
      active={id === 'assigneeStartsToday'}
      onClick={() => setActiveTab("assigneeStartsToday")} />

    <NavigationButton id='core.myWork.tab.task.available' 
      values={getGroupCount('assigneeOther')} 
      color={AssigneePalette.assigneeOther}
      active={id === 'assigneeOther'}
      onClick={() => setActiveTab("assigneeOther")} />
    
    <NavigationButton id='core.myWork.tab.recentActivities' 
      values={getGroupCount('assigneeOther')} 
      color={moss}
      active={id === 'recentActivities'}
      onClick={() => setActiveTab("recentActivities")} />

    <TaskCreateDialog open={createOpen} onClose={handleTaskCreate} />

    <NavigationButton
      id='core.taskCreate.newTask'
      onClick={handleTaskCreate}
      values={undefined}
      active={createOpen}
      color={cyan}/>
  </>);
}

const MyWorkItems: React.FC = () => {
  const { activeTab, setActiveTask, activeTask } = useMyWork();

  return (<>
      {activeTab.body.entries.map((task, index) => (
      <LayoutListItem key={task.id} index={index} active={activeTask?.id === task.id} onClick={() => setActiveTask(task)}>
        <TaskItem key={task.id} task={task} />
      </LayoutListItem>)
    )}
    <LayoutListFiller value={activeTab.body} />
  </>);
}

const MyWorkActive: React.FC = () => {
  const { activeTask } = useMyWork();
  return (<TaskItemActive task={activeTask} />);
}

const MyWorkLayout: React.FC = () => {
  const navigation = <MyWorkNavigation />;
  const pagination = <MyWorkPagination />;
  const active = <MyWorkActive />;
  const items = <MyWorkItems />;

  return (<LayoutList slots={{ navigation, active, items, pagination }} />)
}

const MyWork: React.FC = () => {
  return <MyWorkProvider><MyWorkLayout /></MyWorkProvider>;
}

export default MyWork;