import React from 'react';
import Context from 'context';
import { DialobListTabState, DialobList } from './DialobListTabState';
import DialobItemActive from './DialobItemActive';
import DialobItem from './DialobItem';
import { AssigneePalette } from 'descriptor-task';
import { TenantState, Group, GroupBy } from 'descriptor-tenant';



function groupsToRecord(state: Group[]): Record<GroupBy, Group> {
  return state.reduce((acc, item) => ({ ...acc, [item['id']]: item }), {} as Record<GroupBy, Group>);
}

function getTabs(state: TenantState): DialobListTabState[] | any {
  const groupBy: Group[] = state.toGroupsAndFilters().withGroupBy("none").groups;
  const groups = groupsToRecord(groupBy);
  const none = groups["none"];


  return [
    {
      id: 0,
      label: 'core.myWork.tab.task.currentlyWorking',
      color: AssigneePalette.assigneeCurrentlyWorking,
      group: none,
      disabled: true,
      count: undefined
    },
  ]
}

const DialobListLoader: React.FC = () => {
  const entries = Context.useTenants();
  console.log(getTabs(entries.state));

  if (entries.loading) {
    return <>...loading</>
  }
  return (<DialobList state={getTabs(entries.state)}>{{ DialobItem, DialobItemActive }}</DialobList>);
}

export default DialobListLoader;