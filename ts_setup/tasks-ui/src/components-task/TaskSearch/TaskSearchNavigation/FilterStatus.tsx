import React from 'react';
import { useIntl } from 'react-intl';

import PendingIcon from '@mui/icons-material/Pending';
import ThumbUpIcon from '@mui/icons-material/ThumbUp';
import ThumbDownIcon from '@mui/icons-material/ThumbDown';
import HailIcon from '@mui/icons-material/Hail';

import { FlyoutMenu, FlyoutMenuItem, FlyoutMenuTrigger } from 'components-flyout-menu';
import { FilterByStatus, FilterBy, TaskStatus } from 'descriptor-task';
import { ButtonSearch } from 'components-generic';


const statustypes: TaskStatus[] = ['CREATED', 'IN_PROGRESS', 'COMPLETED', 'REJECTED'];

const Icon: React.FC<{ type: TaskStatus }> = ({ type }) => {
  switch (type) {
    case 'CREATED': return <HailIcon />
    case 'IN_PROGRESS': return <PendingIcon />
    case 'COMPLETED': return <ThumbUpIcon />
    case 'REJECTED': return <ThumbDownIcon />
  }
}


const FilterByMenuItem: React.FC<{
  currentlySelected: readonly FilterBy[],
  displaying: TaskStatus,
  onClick: (value: TaskStatus[]) => void;
}> = ({
  currentlySelected, displaying, onClick
}) => {

    const found = currentlySelected.find(filter => filter.type === 'FilterByStatus');
    const active = found ? found.type === 'FilterByStatus' && found.status.includes(displaying) : false

    const intl = useIntl();
    const selected: string = intl.formatMessage({ id: `taskSearch.filter.status.selected` });
    const title: string = (active ? selected : "") + intl.formatMessage({ id: `taskSearch.filter.status.${displaying}` });
    const subtitle: string = intl.formatMessage({ id: `taskSearch.filter.status.subtitle.${displaying}` });

    function handleOnClick() {
      onClick([displaying]);
    }

    return (<FlyoutMenuItem active={active} onClick={handleOnClick} title={title} subtitle={subtitle}><Icon type={displaying} /></FlyoutMenuItem>);
  }



const FilterStatus: React.FC<{
  onChange: (value: TaskStatus[]) => void;
  value: readonly FilterBy[];
}> = (props) => {

  const filterByStatus = props.value.find(filter => filter.type === 'FilterByStatus') as FilterByStatus | undefined;

  return (
    <FlyoutMenu>
      <FlyoutMenuTrigger>
        <ButtonSearch onClick={() => { }} id='taskSearch.searchBar.filterStatus' values={{ count: filterByStatus?.status.length }} />
      </FlyoutMenuTrigger>
      {statustypes.map(displaying => <FilterByMenuItem key={displaying} currentlySelected={props.value} displaying={displaying} onClick={props.onChange} />)}
    </FlyoutMenu>
  );
}

export { FilterStatus };