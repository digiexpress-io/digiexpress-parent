import React from 'react'
import { Chip } from '@mui/material';

import { GInputBaseAnyProps } from '../g-input-base';

import { GInputMultilistProps } from './g-input-multilist-types';
import { GInputAutoComplete } from '../g-input-autocomplete';
import { useUtilityClasses } from './useUtilityClasses';


export const ReadOnlyMultilist: React.FC<GInputBaseAnyProps & GInputMultilistProps> = (props) => {
  const { datasource, id, variant } = props;
  const classes = useUtilityClasses(id, variant);
  const selectedValues = props.value ?? [];
  const selectedLabels = selectedValues.map(key => datasource?.entries.find(e => e.key === key)?.value ?? key);

  return (
    <div className={classes.tags}>
      {selectedLabels.map((label, i) => (
        <Chip key={i} label={label} />
      ))}
    </div>
  );
}


export const MultilistAutocomplete: React.FC<GInputBaseAnyProps & GInputMultilistProps> = (props) => {
  const { datasource, id, onChange } = props;

  return (
    <GInputAutoComplete
      id={id}
      disabled={props.disabled}
      datasource={datasource}
      multiple={true}
      onChange={onChange}
      value={props.value} />
  );
}

