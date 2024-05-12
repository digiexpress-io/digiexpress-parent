import React from 'react';
import { HdesFileId } from './FileSystemTypes';

export interface FileSystemTreeFocusContextType {
  treeFocusedOn: HdesFileId | undefined;
  setTreeFocusOn(id: HdesFileId): void;
  getTreeFocusOn(id: HdesFileId): boolean
}

export const FileSystemTreeFocusContext = React.createContext<FileSystemTreeFocusContextType>({} as any);

export const FileSystemTreeFocusProvider: React.FC<{ children: React.ReactNode; }> = ({children}) => {
  const [treeFocusedOn, setTreeFocusOn] = React.useState<HdesFileId>();

  const contextValue: FileSystemTreeFocusContextType = React.useMemo(() => {
    function getTreeFocusOn(id: HdesFileId) {
      return treeFocusedOn === id;
    }
    return Object.freeze({ treeFocusedOn, getTreeFocusOn, setTreeFocusOn });
  }, [treeFocusedOn]);

  return (<FileSystemTreeFocusContext.Provider value={contextValue}>
    {children}
  </FileSystemTreeFocusContext.Provider>);
}


export function useFileSystemTreeFocus() {
  const result: FileSystemTreeFocusContextType = React.useContext(FileSystemTreeFocusContext);
  return result;
}