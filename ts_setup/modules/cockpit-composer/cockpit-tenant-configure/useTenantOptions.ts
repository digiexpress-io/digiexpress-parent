
import React from 'react';
import { createFilterOptions, FilterOptionsState } from '@mui/material';

import { CockpitApi } from '@dxs-ts/cockpit-api';


export interface ConfigOptionType {
  inputValue?: string; // user input aka non existent dropdown value
  externalId: string;
}


export function useTenantOptions() {
  const [description, setDescription] = React.useState<string | undefined>(props.selected?.cockpitConfigTenantDesc);
  const [active, setActive] = React.useState<ConfigOptionType | undefined>(() => {
    if(props.selected) {
      return { externalId: props.selected.externalId };
    }
    return undefined;
  });

  const options: ConfigOptionType[] = props.options.map(tenant => ({ externalId: tenant.externalId }));

  const handleActive = (event: React.SyntheticEvent<Element, Event>, newValue: string | ConfigOptionType | null) => {
    if(!newValue) {
      setActive(undefined);
      return;
    }
    
    if (typeof newValue === 'string') {
      setActive({ externalId: newValue });
    } else if (newValue.inputValue) {
      setActive({ externalId: newValue.inputValue });
    } else {
      setActive(newValue);
    }
  }

    function handleDescription(e: React.ChangeEvent<HTMLInputElement>) {
      setDescription(e.target.value);
    }

  return { 
    isValid: active && active.externalId,
    onClose: () => {
      setDescription(undefined),
      setActive(undefined)
    },
    description: {
      value: description, 
      setValue: handleDescription
    },
    active: {
      value: active,
      setValue: handleActive, 
    },
    options: {
      values: options,
      filter: _filterOptions,
      label: _getOptionLabel
    }
  }
}


const _filter = createFilterOptions<ConfigOptionType>();


const _filterOptions = (options: ConfigOptionType[], params: FilterOptionsState<ConfigOptionType>) => {
  const filtered = _filter(options, params);
  const { inputValue } = params;
  const isExisting = options.some((option) => inputValue === option.externalId);

  if (inputValue !== '' && !isExisting) {
    filtered.push({
      inputValue,
      externalId: `Add "${inputValue}"`,
    });
  }
  return filtered;
}

const _getOptionLabel = (option: string | ConfigOptionType) => {
  if (typeof option === 'string') {
    return option;
  }
  if (option.inputValue) {
    return option.inputValue;
  }
  return option.externalId;
}
