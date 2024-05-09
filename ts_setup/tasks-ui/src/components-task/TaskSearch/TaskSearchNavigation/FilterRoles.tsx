import React from 'react';
import { useIntl } from 'react-intl';

import { FlyoutMenu, FlyoutMenuItem, FlyoutMenuTrigger } from 'components-flyout-menu';
import { FilterByRoles, FilterBy, useTasks } from 'descriptor-task';
import { ButtonSearch, LetterIcon } from 'components-generic';
import { useAvatar } from 'descriptor-avatar';

const FilterByMenuItem: React.FC<{
  currentlySelected: readonly FilterBy[],
  displaying: string,
  onClick: (value: string[]) => void;
}> = ({
  currentlySelected, displaying, onClick
}) => {
    const avatar = useAvatar(displaying);
    const found = currentlySelected.find(filter => filter.type === 'FilterByRoles');
    const active = found ? found.type === 'FilterByRoles' && found.roles.includes(displaying) : false

    const intl = useIntl();
    const subtitle: string = intl.formatMessage({ id: `taskSearch.filter.roles.subtitle` }, { id: displaying });

    function handleOnClick() {
      onClick([displaying]);
    }

    return (<FlyoutMenuItem active={active} onClick={handleOnClick} title={avatar?.displayName ?? ''} subtitle={subtitle}>
      <LetterIcon transparent>{avatar?.letterCode}</LetterIcon>
    </FlyoutMenuItem>);
  }



const FilterRoles: React.FC<{
  onChange: (value: string[]) => void;
  value: readonly FilterBy[];
}> = (props) => {
  const ctx = useTasks();
  const filterByRoles = props.value.find(filter => filter.type === 'FilterByRoles') as FilterByRoles | undefined;

  return (
    <FlyoutMenu>
      <FlyoutMenuTrigger>
        <ButtonSearch onClick={() => { }} id='taskSearch.searchBar.filterRoles' values={{ count: filterByRoles?.roles.length }} />
      </FlyoutMenuTrigger>
      {ctx.roles.map(displaying => <FilterByMenuItem key={displaying} currentlySelected={props.value} displaying={displaying} onClick={props.onChange} />)}
    </FlyoutMenu>);
}

export { FilterRoles };