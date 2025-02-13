import React from 'react';

import { useTasks } from 'descriptor-task';
import { useSearch } from './TaskSearchContext';
import { useGrouping } from './TaskGroupingContext';


import LoggerFactory from 'logger';
const _logger = LoggerFactory.getLogger();


export const ReloadSearchCtx: React.FC = () => {

  const { tasks } = useTasks();
  const searchCtx = useSearch();

  React.useEffect(() => {
    _logger.debug("reloading task search context");
    searchCtx.withData(tasks);
  }, [tasks]);

  return (null);
}


export const ReloadGroupCtx: React.FC = () => {
  const searchCtx = useSearch();
  const groupCtx = useGrouping();

  React.useEffect(() => {
    _logger.debug("reloading task grouping context");
    groupCtx.reducer.withData(searchCtx.state.filtered);
  }, [searchCtx.state.filtered]);

  return (null);
}

export const TaskReloadProvider: React.FC = () => {
  return (<>
    <ReloadSearchCtx />
    <ReloadGroupCtx />
  </>);
}