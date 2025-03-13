import React from 'react';
import { IconbarContextType } from './iconbar-api';


export const IconbarContext = React.createContext<IconbarContextType>({} as any)

export const IconbarProvider: React.FC<{ children: React.ReactNode}> = ({children}) => {
  const [state, setState] = React.useState<string | undefined>();
  
  const context = React.useMemo(() => {

    function handleActiveId(newActiveId: string | undefined) {
      setState(newActiveId);
    }
    return { handleActiveId, activeId: state }
  }, [setState, state])

  return (<IconbarContext.Provider value={context}>{children}</IconbarContext.Provider>);
}

export const useIconbar = () => {
  const result: IconbarContextType = React.useContext(IconbarContext);
  return result;
}