import React from 'react';
import { useBackend } from 'descriptor-backend';
import { HdesFileSystemCommand, HdesFileSystemStore } from './HdesFileSystemTypes';
import { Xfs, getXfsInstance } from './XfsTypes';


export interface XfsContextType {
  fs: Xfs;
  handleUpdate: (update: HdesFileSystemCommand<any>) => Promise<void>;
}

export const XfsContext = React.createContext<XfsContextType>({} as any);

export const XfsProvider: React.FC<{ children: React.ReactNode; }> = ({ children }) => {
  const backend = useBackend();
  const store = React.useMemo(() => new HdesFileSystemStore(backend.store), [backend]);
  const [fs, setFs] = React.useState<Xfs>();

  React.useEffect(() => {
    store.get().then(data => setFs(getXfsInstance(data)))
  }, []);

  const handleUpdate = React.useCallback(async (update: HdesFileSystemCommand<any>) => {
    const next = await store.update(update.id, [update]);
    setFs(getXfsInstance(next));
  }, [setFs]);

  const contextValue: XfsContextType = React.useMemo(() => {
    return Object.freeze({ fs: fs ?? {} as any, handleUpdate });
  }, [fs, handleUpdate]);

  return (<XfsContext.Provider value={contextValue}>
    {fs && children}
  </XfsContext.Provider>);
}


export function useXfs() {
  const result: XfsContextType = React.useContext(XfsContext);
  return result;
}