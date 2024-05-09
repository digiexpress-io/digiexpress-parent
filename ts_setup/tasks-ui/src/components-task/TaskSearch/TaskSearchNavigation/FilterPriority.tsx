import React from 'react';

import { useIntl } from 'react-intl';


import { FilterByPriority, FilterBy, TaskPriority } from 'descriptor-task';
import { ButtonSearch, LetterIcon } from 'components-generic';
import { FlyoutMenu, FlyoutMenuItem, FlyoutMenuTrigger } from 'components-flyout-menu';

const prioritytypes: TaskPriority[] = ['HIGH', 'MEDIUM', 'LOW'];

const Icon: React.FC<{ type: TaskPriority }> = ({ type }) => {
  switch (type) {
    case 'LOW': return <LetterIcon transparent>L</LetterIcon>
    case 'MEDIUM': return <LetterIcon transparent>M</LetterIcon>
    case 'HIGH': return <LetterIcon transparent>H</LetterIcon>
  }
}

const FilterByMenuItem: React.FC<{
  currentlySelected: readonly FilterBy[],
  displaying: TaskPriority,
  onClick: (value: TaskPriority[]) => void;
}> = ({
  currentlySelected, displaying, onClick
}) => {

    const found = currentlySelected.find(filter => filter.type === 'FilterByPriority');
    const active = found ? found.type === 'FilterByPriority' && found.priority.includes(displaying) : false

    const intl = useIntl();
    const selected: string = intl.formatMessage({ id: `taskSearch.filter.priority.selected` });
    const title: string = (active ? selected : "") + intl.formatMessage({ id: `taskSearch.filter.priority.${displaying}` });
    const subtitle: string = intl.formatMessage({ id: `taskSearch.filter.priority.subtitle.${displaying}` });

    function handleOnClick() {
      onClick([displaying]);
    }

    return (<FlyoutMenuItem active={active} onClick={handleOnClick} title={title} subtitle={subtitle}><Icon type={displaying} /></FlyoutMenuItem>);
  }


const FilterPriority: React.FC<{
  onChange: (value: TaskPriority[]) => void;
  value: readonly FilterBy[];
}> = (props) => {


  const filterByPriority = props.value.find(filter => filter.type === 'FilterByPriority') as FilterByPriority | undefined;

  return (
    <FlyoutMenu>
      <FlyoutMenuTrigger>
        <ButtonSearch onClick={() => { }} id='taskSearch.searchBar.filterPriority' values={{ count: filterByPriority?.priority.length }} />
      </FlyoutMenuTrigger>
      {prioritytypes.map(displaying => <FilterByMenuItem key={displaying} currentlySelected={props.value} displaying={displaying} onClick={props.onChange} />)}
    </FlyoutMenu>
  );
}
export { FilterPriority };