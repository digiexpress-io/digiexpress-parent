import React from 'react';

import { GInputAutoCompleteProps } from './g-input-autocomplete-types';
import { OptionsProvider } from './GInputAutocompleteProvider';
import { GInputAutoCompleteMulti } from './GInputAutocompleteMulti';
import { GInputAutoCompleteUni } from './GInputAutocompleteUni';


export const GInputAutoComplete: React.FC<GInputAutoCompleteProps> = (props) => {
  return (
    <OptionsProvider {...props}>
      {props.multiple ?
        <GInputAutoCompleteMulti {...props}/> :
        <GInputAutoCompleteUni {...props}/>
      }
    </OptionsProvider>)
}

