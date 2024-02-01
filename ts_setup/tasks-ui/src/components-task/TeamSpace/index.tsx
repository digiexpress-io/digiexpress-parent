import React from 'react';
import { TablePagination } from '@mui/material';


import { cyan } from 'components-colors';
import { LayoutList, NavigationButton, LayoutListItem, LayoutListFiller  } from 'components-generic';
import { Palette, TeamGroupType } from 'descriptor-task';

import TaskCreateDialog from '../TaskCreate';
import { TeamSpaceProvider, useTeamSpace } from './TeamSpaceContext';
import TaskItemActive from './TaskItemActive';
import TaskItem from './TaskItem';


const TeamSpacePagination: React.FC = () => {
  const { setTabRowsPerPage, setTabPageNo, table } = useTeamSpace();
  return (<TablePagination component="div"
    rowsPerPageOptions={table.rowsPerPageOptions}
    count={table.src.length}
    rowsPerPage={table.rowsPerPage}
    page={table.page}
    onPageChange={setTabPageNo}
    onRowsPerPageChange={setTabRowsPerPage} />);
}


const TeamSpaceNavigation: React.FC = () => {
  const { setActiveTab, activeTab, getTabItemCount } = useTeamSpace();
  const { id } = activeTab;
  const [createOpen, setCreateOpen] = React.useState(false);

  function handleTaskCreate() {
    setCreateOpen(prev => !prev)
  }

  function getGroupCount(id: TeamGroupType) {
    return { count: getTabItemCount(id) };
  }

  return (<>
    <NavigationButton id='core.teamSpace.tab.task.overdue' 
      values={getGroupCount('groupOverdue')} 
      color={Palette.teamGroupType.groupOverdue}
      active={id === 'groupOverdue'}
      onClick={() => setActiveTab("groupOverdue")} />

    <NavigationButton id='core.teamSpace.tab.task.dueSoon' 
      values={getGroupCount('groupDueSoon')} 
      color={Palette.teamGroupType.groupDueSoon}
      active={id === 'groupDueSoon'}
      onClick={() => setActiveTab("groupDueSoon")} />

    <NavigationButton id='core.teamSpace.tab.task.available' 
      values={getGroupCount('groupAvailable')} 
      color={Palette.teamGroupType.groupAvailable}
      active={id === 'groupAvailable'}
      onClick={() => setActiveTab("groupAvailable")} />

    <TaskCreateDialog open={createOpen} onClose={handleTaskCreate} />

    <NavigationButton
      id='core.taskCreate.newTask'
      onClick={handleTaskCreate}
      values={undefined}
      active={createOpen}
      color={cyan}/>
  </>);
}

const TeamSpaceItems: React.FC = () => {
  const { activeTab, setActiveTask, activeTask } = useTeamSpace();

  return (<>
      {activeTab.body.entries.map((task, index) => (
      <LayoutListItem key={task.id} index={index} active={activeTask?.id === task.id} onClick={() => setActiveTask(task)}>
        <TaskItem key={task.id} task={task} />
      </LayoutListItem>)
    )}
    <LayoutListFiller value={activeTab.body} />
  </>);
}

const TeamSpaceActive: React.FC = () => {
  const { activeTask } = useTeamSpace();
  return (<TaskItemActive task={activeTask} />);
}

const TeamSpaceLayout: React.FC = () => {
  const navigation = <TeamSpaceNavigation />;
  const pagination = <TeamSpacePagination />;
  const active = <TeamSpaceActive />;
  const items = <TeamSpaceItems />;

  return (<LayoutList slots={{ navigation, active, items, pagination }} />)
}

const TeamSpace: React.FC = () => {
  return <TeamSpaceProvider><TeamSpaceLayout /></TeamSpaceProvider>;
}

export default TeamSpace;