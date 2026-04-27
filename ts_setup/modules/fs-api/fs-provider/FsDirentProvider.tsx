import React from 'react';
import { Fs } from '../fs-types';
import { ALL_TYPES, getConfigOptionsForType } from './helpers';
import { ItemReferencesEntry, FsWorld } from './FsWorld';

export type { ItemReferencesEntry };

export interface FsDirentContextType {
  dirents: Fs.DirentBase[];
  creatableTypes: Fs.BodyType[];
  selectOptions: Fs.SelectOptions;
  getConfigOptionsForType: (type: Fs.Type) => Fs.SelectOption[];
  getDirent: (id: string) => Fs.Dirent | undefined;
  isChildError: (dirent: Fs.DirentBase) => boolean;
  findReferencesToDirent: (dirent: Fs.DirentBase) => ItemReferencesEntry[];
  updateDirent: (id: string, updated: Partial<Fs.Props>) => void;
}

const FsDirentContext = React.createContext<FsDirentContextType | undefined>(undefined);

export interface FsDirentProviderProps {
  persistenceUnit: {
    fetchDirents: () => Promise<Fs.DirentBase[]>;
  }
  children: React.ReactNode;
}

export const FsDirentProvider: React.FC<FsDirentProviderProps> = (props) => {

  const [dirents, setDirents] = React.useState<FsWorld>(() => new FsWorld({ dirents: [] }));

  React.useEffect(() => {
    props.persistenceUnit.fetchDirents()
      .then(dirents => {
        console.log("dirents", dirents)
        return new FsWorld({ dirents })
      })
      .then(setDirents)

  }, []);

  const updateDirent = React.useCallback((id: string, updated: Partial<Fs.Props>) => {

    /*
    setPropsMap(prev => {
      if (!prev[id]) {
        return prev;
      }
      return { ...prev, [id]: { ...prev[id], ...updated } as Fs.Props };
    });
    */
  }, []);


  const contextValue: FsDirentContextType = React.useMemo(() => {
    return {

      creatableTypes: ALL_TYPES,
      get selectOptions() {
        return dirents.selectOptions;
      },
      get dirents() {
        return dirents.dirents
      },
      getDirent: (id) => dirents.getDirent(id),
      findReferencesToDirent: (dirent) => dirents.findReferencesToDirent(dirent),
      isChildError: (dirent) => {
        return dirents.isChildError(dirent);
      },

      getConfigOptionsForType,
      updateDirent
    };
  }, [dirents, updateDirent]);

  return (
    <FsDirentContext.Provider value={contextValue}>
      {props.children}
    </FsDirentContext.Provider>
  );
};

export function useFsDirent(): FsDirentContextType {
  const result = React.useContext(FsDirentContext);
  if (!result) {
    throw new Error('FsDirentPropsContext is not created!');
  }
  return result;
}

