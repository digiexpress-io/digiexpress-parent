import React from 'react'

import { GInputBaseAnyProps } from '../g-input-base';


import { GInputMultilistProps } from './g-input-multilist-types';
import { GInputAutoComplete } from '../g-input-autocomplete';
import { UNDEFINED_SELECTION_VALUE } from '../g-form-base-element';





export const MultilistAutocomplete: React.FC<GInputBaseAnyProps & GInputMultilistProps> = (props) => {
  const { datasource, id, onChange } = props;

  return (
    <GInputAutoComplete
      id={id}
      datasource={datasource}
      multiple={true}
      onChange={onChange}
      value={props.value ?? UNDEFINED_SELECTION_VALUE} />
  );
}

