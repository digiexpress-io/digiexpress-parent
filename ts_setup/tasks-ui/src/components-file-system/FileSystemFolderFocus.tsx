import React from 'react';
import { HdesFileId } from './FileSystemTypes';

export interface FileSystemFolderFocusContextType {
  folderFocusedOn: HdesFileId | undefined;
  setFolderFocusOn(id: HdesFileId): void;
  getFolderFocusOn(id: HdesFileId): boolean
}

export const FileSystemFolderFocusContext = React.createContext<FileSystemFolderFocusContextType>({} as any);

export const FileSystemFolderFocusProvider: React.FC<{ children: React.ReactNode; }> = ({children}) => {
  const [folderFocusedOn, setFolderFocusOn] = React.useState<HdesFileId>();

  const contextValue: FileSystemFolderFocusContextType = React.useMemo(() => {
    function getFolderFocusOn(id: HdesFileId) {
      return folderFocusedOn === id;
    }
    return Object.freeze({ folderFocusedOn, getFolderFocusOn, setFolderFocusOn });
  }, [folderFocusedOn]);

  return (<FileSystemFolderFocusContext.Provider value={contextValue}>
    {children}
  </FileSystemFolderFocusContext.Provider>);
}


export function useFileSystemFolderFocus() {
  const result: FileSystemFolderFocusContextType = React.useContext(FileSystemFolderFocusContext);
  return result;
}