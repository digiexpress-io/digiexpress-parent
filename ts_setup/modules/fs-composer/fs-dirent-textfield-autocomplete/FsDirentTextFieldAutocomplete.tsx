import React from 'react';
import { Autocomplete, TextField } from '@mui/material';
import { FsDirentTextFieldAutocompleteProps } from './FsDirentTextFieldAutocompleteProps';
import { FsDirentTextFieldAutocompleteRoot, useUtilityClasses } from './useUtilityClasses';

export const FsDirentTextFieldAutocomplete: React.FC<FsDirentTextFieldAutocompleteProps> = (props) => {
  const classes = useUtilityClasses();

  return (
    <FsDirentTextFieldAutocompleteRoot className={classes.root}>
      <Autocomplete
        multiple
        freeSolo
        options={props.options}
        value={props.value}
        onChange={(_event, newValue) => props.onChange(newValue as string[])}
        renderInput={(params) => <TextField {...params} size='small' placeholder={props.placeholder} />}
      />
    </FsDirentTextFieldAutocompleteRoot>
  );
};
