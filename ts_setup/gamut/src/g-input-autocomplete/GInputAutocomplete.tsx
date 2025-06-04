import React from 'react';
import { Autocomplete, AutocompleteProps, FilterOptionsState, TextField } from '@mui/material';

import { GInputAutoCompleteProps } from './g-input-autocomplete-types';
import { OptionsProvider } from './GInputAutocompleteProvider';
import { GInputAutoCompleteOption } from './GInputAutocompleteOption';
import { InputHiddenUni, InputHiddenMulti } from './InputHidden';



// single choice
export const GInputAutoComplete: React.FC<GInputAutoCompleteProps> = (props) => {
  const ownerState = useOwnerState(props);
  const [value, setValue] = React.useState(props.value);

  return (
    <OptionsProvider {...props}>
      {props.multiple ?
        <InputHiddenMulti id={props.id} value={value as string[]} onChange={props.onChange} /> :
        <InputHiddenUni id={props.id} value={value as string} onChange={props.onChange} />
      }
      <Autocomplete
        filterOptions={(options, state) => filterOptions(props, options, state)}
        disablePortal {...ownerState}
        onChange={(_ignore, newValue) => setValue(newValue)}
        renderInput={(params) => <TextField {...params} hiddenLabel />}
      />
    </OptionsProvider>)
}

function filterOptions(props: GInputAutoCompleteProps, _options: string[], state: FilterOptionsState<string>): string[] {
  const inputValue = state.inputValue.toLocaleLowerCase();
  return (props.datasource?.entries ?? [])
    .filter(v => v.value.toLocaleLowerCase().indexOf(inputValue) > - 1)
    .map(v => v.value)
}

function useOwnerState(props: GInputAutoCompleteProps): Partial<AutocompleteProps<string, true, false, false, any>> & { options: string[] } {
  const options = props.datasource ? props.datasource.entries.map(e => e.key) : [];

  if (props.multiple) {
    return {
      options,
      disableCloseOnSelect: true,
      multiple: true,
      renderOption: (props, option, state, ownerState) => <GInputAutoCompleteOption key={props.key} props={props} option={option} state={state} ownerState={ownerState} />,
      value: props.value as any
    }
  }
  return { options, value: props.value as any }
}

