import React from 'react';

import GroupsIcon from '@mui/icons-material/Groups';
import FolderOffIcon from '@mui/icons-material/FolderOff';
import Face5Icon from '@mui/icons-material/Face5';
import FlagIcon from '@mui/icons-material/Flag';
import PendingIcon from '@mui/icons-material/Pending';
import { useIntl } from 'react-intl';

import { ButtonSearch } from 'components-generic';
import { GroupByOptions, GroupByTypes } from '../TableContext';
import { FlyoutMenu, FlyoutMenuItem, FlyoutMenuTrigger } from 'components-flyout-menu';


const Icon: React.FC<{ type: GroupByTypes }> = ({ type }) => {
  switch (type) {
    case 'none': return <FolderOffIcon />
    case 'owners': return <Face5Icon />
    case 'priority': return <FlagIcon />
    case 'roles': return <GroupsIcon />
    case 'status': return <PendingIcon />
  }
}


const GroupByMenuItem: React.FC<{
  currentlySelected: GroupByTypes,
  displaying: GroupByTypes,
  onClick: (value: GroupByTypes) => void;
}> = ({
  currentlySelected, displaying, onClick
}) => {
    const active = currentlySelected === displaying;

    const intl = useIntl();
    const selected: string = intl.formatMessage({ id: `taskSearch.filter.groupBy.selected` });
    const title: string = (active ? selected : "") + intl.formatMessage({ id: `taskSearch.filter.groupBy.${displaying}` });
    const subtitle: string = intl.formatMessage({ id: `taskSearch.filter.groupBy.subtitle.${displaying}` });

    function handleOnClick() {
      onClick(displaying);
    }

    return (<FlyoutMenuItem active={active} onClick={handleOnClick} title={title} subtitle={subtitle}><Icon type={displaying} /></FlyoutMenuItem>);
  }

export const GroupBySelect: React.FC<{
  onChange: (value: GroupByTypes) => void;
  value: GroupByTypes;
}> = ({ onChange, value }) => {

  return (<FlyoutMenu>
    <FlyoutMenuTrigger>
      <ButtonSearch onClick={() => { }} id='taskSearch.searchBar.groupBy' values={{ groupBy: value }} />
    </FlyoutMenuTrigger>
    {GroupByOptions.map(displaying => <GroupByMenuItem key={displaying} currentlySelected={value} displaying={displaying} onClick={onChange} />)}
  </FlyoutMenu>
  );
}