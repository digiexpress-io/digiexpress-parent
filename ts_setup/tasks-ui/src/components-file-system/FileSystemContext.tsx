import React from 'react';
import { HdesFileSystem, HdesFileSystemCommand, HdesFileSystemStore } from './FileSystemTypes';
import { useBackend } from 'descriptor-backend';

export interface FileSystemContextType {
  fs: HdesFileSystem;
  handleUpdate: (update: HdesFileSystemCommand<any>) => Promise<void>;
}

export const FileSystemContext = React.createContext<FileSystemContextType>({} as any);

export const FileSystemProvider: React.FC<{ children: React.ReactNode; }> = ({children}) => {
  const backend = useBackend();
  const store = React.useMemo(() => new HdesFileSystemStore(backend.store), [backend]);
  const [fs, setFs] = React.useState<HdesFileSystem>();
  
  const handleUpdate = React.useCallback(async (update: HdesFileSystemCommand<any>) => {
    const next = await store.update(update.id, [update]);
    setFs(next);
  }, [setFs]);

  const contextValue: FileSystemContextType = React.useMemo(() => {
    return Object.freeze({ fs: fs ?? {} as any, handleUpdate });
  }, [fs, handleUpdate]);

  return (<FileSystemContext.Provider value={contextValue}>
    {fs && children}
  </FileSystemContext.Provider>);
}


export function useFileSystem() {
  const result: FileSystemContextType = React.useContext(FileSystemContext);
  return result;
}