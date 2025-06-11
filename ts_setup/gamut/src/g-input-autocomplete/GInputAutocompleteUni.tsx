import React from 'react';
import { Autocomplete, FilterOptionsState, TextField } from '@mui/material';

import { GInputAutoCompleteProps } from './g-input-autocomplete-types';
import { OptionsProvider } from './GInputAutocompleteProvider';
import { InputHiddenUni, InputHiddenMulti } from './InputHidden';

import { MaterialOptionType, useOwnerState } from './useOwnerState';



// single choice
export const GInputAutoCompleteUni: React.FC<GInputAutoCompleteProps> = (props) => {
  const { filterOptions, placeholder, options, getUniSelected } = useOwnerState(props);
  const [value, setValue] = React.useState<string | null>(props.value as string ?? null);

  function handleOnChange(_ignore: any, selected: MaterialOptionType | string | null) {
    let newValue: string | undefined;
    if((typeof selected) === 'string') {
      newValue = getUniSelected(selected)?.key;
    } else {
      newValue = selected?.key;
    }
    setValue(newValue ?? null);
  }

  function getOptionLabel(option: MaterialOptionType) {
    if(option.value) {
      return option.value;
    }
    return 'undefined type: ' + JSON.stringify(option);
  }

  return (
    <>
      <InputHiddenUni id={props.id} value={value} onChange={props.onChange} />
      <Autocomplete disablePortal
        filterOptions={filterOptions}
        options={options}
        
        getOptionKey={option => option.key}
        getOptionLabel={getOptionLabel}
        onChange={handleOnChange}
        
        value={getUniSelected(value)}
        renderInput={(params) => <TextField {...params} placeholder={placeholder} />}
      />
    </>)
}

