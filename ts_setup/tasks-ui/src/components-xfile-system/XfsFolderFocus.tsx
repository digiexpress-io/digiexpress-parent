import React from 'react';
import { HdesFileId } from './HdesFileSystemTypes';

export interface XfsFolderFocusContextType {
  folderFocusedOn: HdesFileId | undefined;
  setFolderFocusOn(id: HdesFileId): void;
  getFolderFocusOn(id: HdesFileId): boolean
}

export const XfsFolderFocusContext = React.createContext<XfsFolderFocusContextType>({} as any);

export const XfsFolderFocusProvider: React.FC<{ children: React.ReactNode; }> = ({children}) => {
  const [folderFocusedOn, setFolderFocusOn] = React.useState<HdesFileId>();

  const contextValue: XfsFolderFocusContextType = React.useMemo(() => {
    function getFolderFocusOn(id: HdesFileId) {
      return folderFocusedOn === id;
    }
    return Object.freeze({ folderFocusedOn, getFolderFocusOn, setFolderFocusOn });
  }, [folderFocusedOn]);

  return (<XfsFolderFocusContext.Provider value={contextValue}>
    {children}
  </XfsFolderFocusContext.Provider>);
}


export function useXfsFolderFocus() {
  const result: XfsFolderFocusContextType = React.useContext(XfsFolderFocusContext);
  return result;
}