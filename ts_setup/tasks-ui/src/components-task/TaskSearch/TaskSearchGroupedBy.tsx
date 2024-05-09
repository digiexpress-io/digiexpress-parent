import React from 'react';

import { useGrouping, GroupByTypes, useTaskPrefs, TaskPagination, initTaskGroup } from './TableContext';


import LoggerFactory from 'logger';
import { TaskSearchTable } from './TaskSearchTable';

const _logger = LoggerFactory.getLogger();

function isCached(prevProps: Readonly<GroupByCacheProps>, nextProps: Readonly<GroupByCacheProps>) {
  const isEqual = (
    prevProps.groupByType == nextProps.groupByType &&
    prevProps.groupId === nextProps.groupId &&
    prevProps.content.equals(nextProps.content)
  );
  if (!isEqual) {
    _logger.debug(`reloading task table - ${nextProps.groupId}`);
  }
  return (isEqual)
}



interface GroupByCacheProps {
  groupByType: GroupByTypes,
  groupId: string,
  content: TaskPagination,
  setContent: React.Dispatch<React.SetStateAction<TaskPagination>>
}

const GroupByCache: React.FC<GroupByCacheProps> = React.memo(({ groupByType, groupId, content, setContent }) => {
  return (<TaskSearchTable groupByType={groupByType} groupId={groupId} content={content} setContent={setContent}/>);
}, isCached)

export const TaskSearchGroupedBy: React.FC<{ groupByType: GroupByTypes, groupId: string }> = ({ groupByType, groupId }) => {
  const prefCtx = useTaskPrefs();
  const grouping = useGrouping();
  const [content, setContent] = React.useState<TaskPagination>(initTaskGroup(groupId, prefCtx));

  React.useEffect(() => {
    const group = grouping.getByGroupId(groupId);
    const records = group?.value.map(index => grouping.collection.origin[index]);

    setContent(prev => prev.withSrc(records))
  }, [grouping]);


  return (<GroupByCache content={content} setContent={setContent} groupByType={groupByType} groupId={groupId} />);

}
