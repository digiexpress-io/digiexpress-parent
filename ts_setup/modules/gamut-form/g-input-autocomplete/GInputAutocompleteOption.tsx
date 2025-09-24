import React from 'react';
import { AutocompleteOwnerState, AutocompleteRenderOptionState, Checkbox } from '@mui/material';
import CheckBoxOutlineBlankIcon from '@mui/icons-material/CheckBoxOutlineBlank';
import CheckBoxIcon from '@mui/icons-material/CheckBox';
import { useOptions } from './GInputAutocompleteProvider';
import { MaterialOptionType } from './useOwnerState';



export interface GInputAutoCompleteOptionProps {
  props: React.HTMLAttributes<HTMLLIElement> & { key: any },
  option: MaterialOptionType,
  state: AutocompleteRenderOptionState
}


const icon = <CheckBoxOutlineBlankIcon fontSize="small" />;
const checkedIcon = <CheckBoxIcon fontSize="small" />;


export function GInputAutoCompleteOption(props: GInputAutoCompleteOptionProps) {
  const options = useOptions();
  const { key, ...optionProps } = props.props;
  const { selected } = props.state;
  const value = options.datasource.entries.find(e => e.key === props.option.key)?.value ?? props.option.value;

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
