import { FilterOptionsState } from "@mui/material";
import { GInputAutoCompleteProps } from "./g-input-autocomplete-types";

import { useIntl } from "react-intl";
import { UNDEFINED_SELECTION_VALUE } from '../g-form-base-element';


export interface MaterialOptionType {
  key: string;
  value: string;
}

export function useOwnerState(props: GInputAutoCompleteProps) {
  const intl = useIntl();
  const placeholderForNoValue = !props.value || (Array.isArray(props.value) && props.value.length === 0) || props.value === '';
  const placeholder = placeholderForNoValue ? intl.formatMessage({ id: UNDEFINED_SELECTION_VALUE }) : '';

  const options: MaterialOptionType[] = props.datasource ? props.datasource.entries : [];
  const optionsByKey = options.reduce<Record<string, MaterialOptionType>>((collector, next) => {
    collector[next.key] = next;
    return collector;
  }, {});



  function filterOptions(_options: MaterialOptionType[], state: FilterOptionsState<MaterialOptionType>): MaterialOptionType[] {
    const inputValue = state.inputValue?.toLocaleLowerCase();
    return (props.datasource?.entries ?? [])
      .filter(v => v.value?.toLocaleLowerCase().indexOf(inputValue) > - 1)
  }


  function getMultiSelected(init: string[]): MaterialOptionType[]  {
    if(!init) {
      return [];
    }
    
    if(!Array.isArray(init)) {
      return options.filter(({key}) => key === init);
    }
    const value: MaterialOptionType[] = init.map(key => optionsByKey[key]).filter(v => !!v);
    return value;
  }

  function getUniSelected(init: string | null): MaterialOptionType | null  {
    if(!init) {
      return null;
    }
    return options.find(({key}) => key === init) ?? null;
  }

  return { filterOptions, placeholder, options, optionsByKey, getMultiSelected, getUniSelected }
}




