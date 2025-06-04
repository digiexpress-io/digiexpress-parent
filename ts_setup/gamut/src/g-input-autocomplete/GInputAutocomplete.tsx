import React from 'react';
import { Autocomplete, AutocompleteProps, FilterOptionsState, TextField } from '@mui/material';

import { GInputAutoCompleteProps } from './g-input-autocomplete-types';
import { OptionsProvider } from './GInputAutocompleteProvider';
import { GInputAutoCompleteOption } from './GInputAutocompleteOption';
import { InputHiddenUni, InputHiddenMulti } from './InputHidden';
import { UNDEFINED_SELECTION_VALUE } from '../g-form-base-element';
import { useIntl } from 'react-intl';



// single choice
export const GInputAutoComplete: React.FC<GInputAutoCompleteProps> = (props) => {
  const ownerState = useOwnerState(props);
  const [value, setValue] = React.useState(props.multiple ? (props.value ?? []) : (props.value ?? ''));
  const intl = useIntl();
  const placeholderForNoValue = !value || (Array.isArray(value) && value.length === 0) || value === '';

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
        renderInput={(params) => <TextField {...params} placeholder={placeholderForNoValue ? intl.formatMessage({ id: UNDEFINED_SELECTION_VALUE }) : ''} />}
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
    const value = Array.isArray(props.value) ? props.value : [];
    return {
      options,
      disableCloseOnSelect: true,
      multiple: true,
      renderOption: (props, option, state, ownerState) => <GInputAutoCompleteOption key={props.key} props={props} option={option} state={state} ownerState={ownerState} />,
      value: value
    }
  }
  return { options, value: props.value as any ?? '' }
}

