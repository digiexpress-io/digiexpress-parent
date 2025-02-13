import React from 'react';

export interface XTableContextType {
  rows: number;
  columns: number;
  hiddenColumns?: string[];
}

export const XTableContext = React.createContext<XTableContextType>({} as any);

export const XTableProvider: React.FC<{
  children: React.ReactNode, 
  rows: number, 
  columns: number, 
  hiddenColumns: string[] | undefined
}> = ({children, rows, columns, hiddenColumns}) => {
  
  const contextValue: XTableContextType = React.useMemo(() => {  
    return Object.freeze({ rows, columns, hiddenColumns });
  }, [rows, columns, hiddenColumns]);

  return (<XTableContext.Provider value={contextValue}>{children}</XTableContext.Provider>);
}


export function useXTable() {
  const result: XTableContextType = React.useContext(XTableContext);
  return result;
}