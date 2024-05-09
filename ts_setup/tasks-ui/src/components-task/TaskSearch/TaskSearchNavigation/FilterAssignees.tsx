import React from 'react';
import { useIntl } from 'react-intl';

import { FilterByOwners, FilterBy, useTasks } from 'descriptor-task';
import { ButtonSearch, LetterIcon } from 'components-generic';
import { useAvatar } from 'descriptor-avatar';
import { FlyoutMenu, FlyoutMenuItem, FlyoutMenuTrigger } from 'components-flyout-menu';



const FilterByMenuItem: React.FC<{
  currentlySelected: readonly FilterBy[],
  displaying: string,
  onClick: (value: string[]) => void;
}> = ({
  currentlySelected, displaying, onClick
}) => {
    const avatar = useAvatar(displaying);
    const found = currentlySelected.find(filter => filter.type === 'FilterByOwners');
    const active = found ? found.type === 'FilterByOwners' && found.owners.includes(displaying) : false;


    const intl = useIntl();
    const subtitle: string = intl.formatMessage({ id: `taskSearch.filter.owner.subtitle` }, { id: displaying });

    function handleOnClick() {
      onClick([displaying]);
    }

    return (<FlyoutMenuItem active={active} onClick={handleOnClick} title={avatar?.displayName ?? ''} subtitle={subtitle}>
      <LetterIcon transparent>{avatar?.letterCode}</LetterIcon>
    </FlyoutMenuItem>);
  }


const FilterAssignees: React.FC<{
  onChange: (value: string[]) => void;
  value: readonly FilterBy[];
}> = (props) => {

  const ctx = useTasks();
  const filterByOwners = props.value.find(filter => filter.type === 'FilterByOwners') as FilterByOwners | undefined;

  return (
    <FlyoutMenu>
      <FlyoutMenuTrigger>
        <ButtonSearch onClick={() => { }} id='taskSearch.searchBar.filterAssignees' values={{ count: filterByOwners?.owners.length }} />
      </FlyoutMenuTrigger>
      {ctx.owners.map(displaying => <FilterByMenuItem key={displaying} currentlySelected={props.value} displaying={displaying} onClick={props.onChange} />)}
    </FlyoutMenu>);
}

export { FilterAssignees };