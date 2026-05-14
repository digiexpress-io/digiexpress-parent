import React from 'react';
import { Fs } from '../fs-types';
import { ALL_TYPES, getConfigOptionsForType, getExtension } from './helpers';
import { ItemReferencesEntry, FsWorld } from './FsWorld';

export type { ItemReferencesEntry };

export interface FsDirentContextType {
  dirents: Fs.DirentBase[];
  creatableTypes: Fs.BodyType[];
  selectOptions: Fs.SelectOptions;
  getParentDirent(childId: string): Fs.DirentBase | undefined;
  getArticleName: (id: string) => string | undefined;
  getExtension: (type: Fs.BodyType) => string | undefined;
  getConfigOptionsForType: (type: Fs.BodyType) => Fs.SelectOption[];
  getDirent: (id: string) => Fs.DirentBase | undefined;
  isChildError: (dirent: Fs.DirentBase) => boolean;
  findReferencesToDirent: (dirent: Fs.DirentBase) => ItemReferencesEntry[];
  updateDirent: (id: string, updated: Partial<Fs.DirentBase>) => void;
  fetchDirentBody: (id: string, bodyType: Fs.BodyType) => Promise<Fs.WorldFsBody>;
  applyTransientChanges: (change: Fs.WrenchAstBodyChange) => Promise<Fs.WorldFsBody>;
  debugDirent: (params: { id: string; input?: string; inputCSV?: string }) => Promise<Fs.DebugResponse>;
}

const FsDirentContext = React.createContext<FsDirentContextType | undefined>(undefined);

export interface FsDirentProviderProps {
  persistenceUnit: {
    fetchDirents: () => Promise<Fs.DirentBase[]>;
    fetchDirentBody: (id: string, bodyType: Fs.BodyType) => Promise<Fs.WorldFsBody>;
    applyTransientChanges: (change: Fs.WrenchAstBodyChange) => Promise<Fs.WorldFsBody>;
    debugDirent?: (params: { id: string; input?: string; inputCSV?: string }) => Promise<Fs.DebugResponse>;
  }
  children: React.ReactNode;
}

export const FsDirentProvider: React.FC<FsDirentProviderProps> = (props) => {

  const [dirents, setDirents] = React.useState<FsWorld>(() => new FsWorld({ dirents: [] }));

  React.useEffect(() => {
    props.persistenceUnit.fetchDirents()
      .then(dirents => {
        console.log('dirents', dirents);
        return new FsWorld({ dirents })
      })
      .then(setDirents)

  }, []);

  const updateDirent = React.useCallback((_id: string, _updated: Partial<Fs.DirentBase>) => {
    // TODO: implement
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

      getExtension,
      getConfigOptionsForType,
      updateDirent,
      getParentDirent: (childId) => dirents.getParentDirent(childId),
      getArticleName: (id) => dirents.getArticleName(id),
      fetchDirentBody: props.persistenceUnit.fetchDirentBody,
      applyTransientChanges: props.persistenceUnit.applyTransientChanges,
      debugDirent: props.persistenceUnit.debugDirent ?? (() => Promise.resolve({})),
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
    throw new Error('FsDirentPropsContext is not created!')
  }
  return result;
}
