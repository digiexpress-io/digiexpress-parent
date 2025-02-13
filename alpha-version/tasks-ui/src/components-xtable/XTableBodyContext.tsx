import React from 'react';

export interface XTableBodyContextType {
  padding: number | undefined
  alternate: boolean
}

export const XTableBodyContext = React.createContext<XTableBodyContextType>({} as any);

export const XTableBodyProvider: React.FC<{
  children: React.ReactNode, 
  padding: number | undefined,
  alternate: boolean | undefined
}> = ({children, padding, alternate}) => {
  
  const contextValue: XTableBodyContextType = React.useMemo(() => {  
    return Object.freeze({ padding, alternate: alternate === true});
  }, [padding, alternate]);

  return (<XTableBodyContext.Provider value={contextValue}>{children}</XTableBodyContext.Provider>);
}


export function useXTableBody() {
  const result: XTableBodyContextType = React.useContext(XTableBodyContext);
  return result;
}