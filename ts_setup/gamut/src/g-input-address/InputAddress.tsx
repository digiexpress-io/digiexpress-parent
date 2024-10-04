import React from 'react'

import { Autocomplete, TextField, AutocompleteChangeReason, AutocompleteInputChangeReason, debounce, AutocompleteRenderInputParams, Box } from '@mui/material'

import { useMap } from '../api-map'
import { GInputBaseAnyProps } from '../g-input-base'

import { GInputAddressProps } from './GInputAddress';
import { useInput } from './InputProvider';



export const InputAddress: React.FC<GInputBaseAnyProps & GInputAddressProps> = (props) => {
  const backendValue = props.value;

  const map = useMap();
  const input = useInput();
  const [selected, setSelected] = React.useState<string | undefined>(backendValue);
  const [options, setOptions] = React.useState<string[]>([]);


  // sync with backend value, new value can be inputed via map
  React.useEffect(() => {
    if (backendValue != selected) {
      setSelected(backendValue);
    }
  }, [backendValue, selected])


  // skip some inputs by debounce
  const findAll = React.useMemo(() => debounce((newValue: string) => map
    .findAll(newValue)
    .then(geo => geo.map(g => g.formattedAddress))
    .then(setOptions),
    400
  ), [map]);

  // find all matching locations
  function handleFiltering(event: React.SyntheticEvent, newValue: string, reason: AutocompleteInputChangeReason) {
    if (reason === 'input') {
      findAll(newValue)
    }
  }
  // save the selected option
  function handleChange(event: React.SyntheticEvent, newValue: string | null, reason: AutocompleteChangeReason) {
    if (reason === 'selectOption') {
      setSelected(newValue ?? '');
      input.setValue(newValue);
    }
  }

  function handelRenderInput(params: AutocompleteRenderInputParams) {
    return <TextField {...params} error={(props.errors?.length ?? 0) > 0} InputLabelProps={{ shrink: true }} />
  }


  return (
    <Autocomplete fullWidth freeSolo
      options={options}
      value={selected ?? ''}
      onInputChange={handleFiltering}
      onChange={handleChange}
      
      renderInput={handelRenderInput}
    />
  )
}