import React from 'react';
import { useIntl } from 'react-intl';
import { TaskDescriptor } from 'descriptor-task';
import { ButtonSearch, LetterIcon } from 'components-generic';
import { FlyoutMenu, FlyoutMenuItem, FlyoutMenuTrigger } from 'components-flyout-menu';




const FilterByMenuItem: React.FC<{
  currentlySelected: readonly (keyof TaskDescriptor)[],
  displaying: keyof TaskDescriptor,
  onClick: (value: (keyof TaskDescriptor)[]) => void;
}> = ({
  currentlySelected, displaying, onClick
}) => {
    const active = currentlySelected.includes(displaying)

    const intl = useIntl();
    const selected: string = intl.formatMessage({ id: `taskSearch.filter.column.selected` });
    const title: string = (active ? selected : "") + intl.formatMessage({ id: `taskSearch.filter.column.${displaying}` });
    const subtitle: string = intl.formatMessage({ id: `taskSearch.filter.column.subtitle.${displaying}` });

    function handleOnClick() {
      onClick([displaying]);
    }

    return (
    <FlyoutMenuItem active={active} onClick={handleOnClick} title={title} subtitle={subtitle}>
      <LetterIcon transparent>{displaying.substring(0, 2).toUpperCase()}</LetterIcon>
    </FlyoutMenuItem>);
  }


const FilterColumns: React.FC<{
  onChange: (value: (keyof TaskDescriptor)[]) => void;
  value: (keyof TaskDescriptor)[];
  types: (keyof TaskDescriptor)[];
}> = (props) => {
  return (
    <FlyoutMenu>
      <FlyoutMenuTrigger>
        <ButtonSearch onClick={() => { }} id='taskSearch.searchBar.filterColumns' values={undefined} />
      </FlyoutMenuTrigger>
      {props.types.map(displaying => <FilterByMenuItem key={displaying} currentlySelected={props.value} displaying={displaying} onClick={props.onChange} />)}
    </FlyoutMenu>
  );
}
export { FilterColumns };