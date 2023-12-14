import React from 'react';

import { Hdes } from '../deps';
import { HelperContext } from './';
import { AutocompleteInput } from './AutocompleteInput';
import { AutocompleteTask } from './AutocompleteTask';
import { AutocompleteSwitch } from './AutocompleteSwitch';


const Autocomplete: React.FC<{
  value: HelperContext | undefined,
  models: Hdes.ModelAPI.Models,
  fl: Hdes.AstAPI.Fl,
  onClose: () => void,
  onCreate: (props: { name: string, type: Hdes.ModelAPI.ServiceType }) => Promise<void>;
}> = ({ value, onClose, fl, models, onCreate }) => {

  if (!value) {
    return null;
  }

  switch (value.type) {
    case "ADD_INPUT":
      return <AutocompleteInput context={value} onClose={onClose} fl={fl} />
    case "ADD_DT":
      return <AutocompleteTask context={value} onClose={onClose} type='DT' fl={fl} models={models} onCreate={onCreate}/>
    case "ADD_FT":
      return <AutocompleteTask context={value} onClose={onClose} type='FLOW_TASK' fl={fl} models={models} onCreate={onCreate} />
    case "ADD_SWITCH":
      return <AutocompleteSwitch context={value} onClose={onClose} fl={fl} />
    default: return null
  }
}

export * from './HintsVisitor';
export { Autocomplete };
