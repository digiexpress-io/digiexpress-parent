import React from 'react';
import { GInputAutoCompleteProps } from './g-input-autocomplete-types';
import { DialobApi } from '@dxs-ts/gamut-api';



export interface OptionsContextType {
  datasource: DialobApi.ActionValueSet
}

const OptionsContext = React.createContext<OptionsContextType>({} as any);

export const OptionsProvider: React.FC<GInputAutoCompleteProps & { children: React.ReactNode }> = (props) => {
  
  const contextValue: OptionsContextType = React.useMemo(() => {
    return Object.freeze({ 
      datasource: props.datasource ?? { id: '', entries: [] }
    });
  }, [props.datasource]);

  return (<OptionsContext.Provider value={contextValue}>{props.children}</OptionsContext.Provider>);
}

export const useOptions = () => {
  const result: OptionsContextType = React.useContext(OptionsContext);
  return result;
}




