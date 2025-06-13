import React from 'react';
import { Autocomplete, TextField } from '@mui/material';

import { GInputAutoCompleteProps } from './g-input-autocomplete-types';
import { InputHiddenMulti } from './InputHidden';
import { MaterialOptionType, useOwnerState } from './useOwnerState';
import { GInputAutoCompleteOption } from './GInputAutocompleteOption';



// single choice
export const GInputAutoCompleteMulti: React.FC<GInputAutoCompleteProps> = (props) => {
  const { filterOptions, placeholder, options, getMultiSelected } = useOwnerState(props);
  const [value, setValue] = React.useState<string[]>(props.value as string[] ?? []);

  function handleOnChange(_ignore: any, selected: MaterialOptionType | MaterialOptionType[] | string) {
    let newValue: MaterialOptionType[];
    if(Array.isArray(selected)) {
      newValue = selected;
    } else if((typeof selected) === 'string') {
      newValue = [{key: selected, value: selected}]
    } else {
      newValue = [selected];
    }
    setValue(newValue.map(({key}) => key));
  }

  function getOptionLabel(option: MaterialOptionType) {
    if(option.value) {
      return option.value;
    }
    return 'undefined type: ' + JSON.stringify(option);
  }
  return (
    <>
      <InputHiddenMulti id={props.id} value={value} onChange={props.onChange} />
      <Autocomplete disablePortal disableCloseOnSelect 
        multiple={true}
        disabled={props.disabled}
        filterOptions={filterOptions}
        options={options}
        
        getOptionKey={option => option.key}
        getOptionLabel={getOptionLabel}
        onChange={handleOnChange}
        
        value={getMultiSelected(value)}
        renderInput={(params) => <TextField {...params} placeholder={placeholder} />}
        renderOption={(props, option, state) => <GInputAutoCompleteOption key={props.key} props={props} option={option} state={state} />}
      />
    </>)
}


