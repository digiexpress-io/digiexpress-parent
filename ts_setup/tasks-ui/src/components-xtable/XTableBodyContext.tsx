import React from 'react';

export interface XTableBodyContextType {
  padding: number | undefined
}

export const XTableBodyContext = React.createContext<XTableBodyContextType>({} as any);

export const XTableBodyProvider: React.FC<{
  children: React.ReactNode, 
  padding: number | undefined
}> = ({children, padding}) => {
  
  const contextValue: XTableBodyContextType = React.useMemo(() => {  
    return Object.freeze({ padding });
  }, [padding]);

  return (<XTableBodyContext.Provider value={contextValue}>{children}</XTableBodyContext.Provider>);
}


export function useXTableBody() {
  const result: XTableBodyContextType = React.useContext(XTableBodyContext);
  return result;
}