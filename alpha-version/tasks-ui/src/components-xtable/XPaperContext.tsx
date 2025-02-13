import React from 'react';

export interface XPaperContextType {
  uuid: string
}

export const XPaperContext = React.createContext<XPaperContextType>({} as any);

export const XPaperProvider: React.FC<{children: React.ReactNode, uuid: string}> = ({children, uuid}) => {
  
  const contextValue: XPaperContextType = React.useMemo(() => {  
    return Object.freeze({ uuid });
  }, [uuid]);
  return (<XPaperContext.Provider value={contextValue}>{children}</XPaperContext.Provider>);
}


export function useXPaper() {
  const result: XPaperContextType = React.useContext(XPaperContext);
  return result;
}