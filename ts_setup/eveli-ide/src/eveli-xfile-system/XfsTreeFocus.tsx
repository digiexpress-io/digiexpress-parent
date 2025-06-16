import React from 'react';
import { HdesFileId } from './HdesFileSystemTypes';

export interface XfsTreeFocusContextType {
  treeFocusedOn: HdesFileId | undefined;
  setTreeFocusOn(id: HdesFileId): void;
  getTreeFocusOn(id: HdesFileId): boolean
}

export const XfsTreeFocusContext = React.createContext<XfsTreeFocusContextType>({} as any);

export const XfsTreeFocusProvider: React.FC<{ children: React.ReactNode; }> = ({children}) => {
  const [treeFocusedOn, setTreeFocusOn] = React.useState<HdesFileId>();

  const contextValue: XfsTreeFocusContextType = React.useMemo(() => {
    function getTreeFocusOn(id: HdesFileId) {
      return treeFocusedOn === id;
    }
    return Object.freeze({ treeFocusedOn, getTreeFocusOn, setTreeFocusOn });
  }, [treeFocusedOn]);

  return (<XfsTreeFocusContext.Provider value={contextValue}>
    {children}
  </XfsTreeFocusContext.Provider>);
}


export function useXfsTreeFocus() {
  const result: XfsTreeFocusContextType = React.useContext(XfsTreeFocusContext);
  return result;
}