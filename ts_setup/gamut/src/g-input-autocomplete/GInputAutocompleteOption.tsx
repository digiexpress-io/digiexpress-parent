import React from 'react';
import { AutocompleteOwnerState, AutocompleteRenderOptionState, Checkbox } from '@mui/material';
import CheckBoxOutlineBlankIcon from '@mui/icons-material/CheckBoxOutlineBlank';
import CheckBoxIcon from '@mui/icons-material/CheckBox';
import { useOptions } from './GInputAutocompleteProvider';



export interface GInputAutoCompleteOptionProps {
  props: React.HTMLAttributes<HTMLLIElement> & { key: any },
  option: string,
  state: AutocompleteRenderOptionState,
  ownerState: AutocompleteOwnerState<string, true, false, false, any>,
}


const icon = <CheckBoxOutlineBlankIcon fontSize="small" />;
const checkedIcon = <CheckBoxIcon fontSize="small" />;


export function GInputAutoCompleteOption(props: GInputAutoCompleteOptionProps) {
  const options = useOptions();

  const { key, ...optionProps } = props.props;
  const { selected } = props.state;
  const value = options.datasource.entries.find(e => e.key === props.option)?.value ?? props.option;

  return (
    <li key={key} {...optionProps}>
      <Checkbox
        icon={icon}
        checkedIcon={checkedIcon}
        style={{ marginRight: 8 }}
        checked={selected}
      />
      {value}
    </li>
  );
}
