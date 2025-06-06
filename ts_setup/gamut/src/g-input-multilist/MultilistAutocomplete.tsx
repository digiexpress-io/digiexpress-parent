import React from 'react'

import { GInputBaseAnyProps } from '../g-input-base';


import { GInputMultilistProps } from './g-input-multilist-types';
import { GInputAutoComplete } from '../g-input-autocomplete';


export const MultilistAutocomplete: React.FC<GInputBaseAnyProps & GInputMultilistProps> = (props) => {
  const { datasource, id, onChange } = props;

  return (
    <GInputAutoComplete
      id={id}
      datasource={datasource}
      multiple={true}
      onChange={onChange}
      value={props.value} />
  );
}

