import React from 'react';
import { Box } from '@mui/material';

import { TaskTable } from './Table'; 

import { TaskSearchProvider, TaskPopperContext, TaskGroupingProvider, TaskPrefsProvider,  useGrouping, GroupByTypes, TaskReloadProvider } from './TableContext';
import { TaskSearchNavigation } from './TaskSearchNavigation';
import { TaskSearchGroupedBy } from './TaskSearchGroupedBy';




const OneTable: React.FC<{ index: number, groupId: string, classifierName: GroupByTypes }> = React.memo((props) => {
  return (<React.Fragment key={props.groupId}>
    {props.index > 0 ? <Box sx={{ p: 2 }} /> : null}

    <Box sx={{ width: '100%' }}>
      <TaskSearchGroupedBy groupId={props.groupId} groupByType={props.classifierName} />
    </Box>
  </React.Fragment>)
})

const ManyTables: React.FC = () => {
  const { collection } = useGrouping();

  return (<>{collection.groups.map((group, index) => <OneTable key={group.id}
    index={index} 
    groupId={group.id} 
    classifierName={collection.classifierName as GroupByTypes}
  />)}</>);
}


export const TaskSearch: React.FC<{}> = () => {

  return (
    <TaskPopperContext>
      <TaskPrefsProvider>
        <TaskSearchProvider>
          <TaskGroupingProvider>
            
            <TaskReloadProvider />
            <TaskSearchNavigation />

            <Box mt={1} />
            <ManyTables />
          
          </TaskGroupingProvider>
        </TaskSearchProvider>
      </TaskPrefsProvider>
    </TaskPopperContext>
  );
}