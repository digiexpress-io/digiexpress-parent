import React from 'react';
import { useIntl } from 'react-intl';
import { useSnackbar } from 'notistack';
import { Fs } from '../fs-types';
import { ALL_TYPES, getConfigOptionsForType, getExtension } from './helpers';
import { ItemReferencesEntry, FsWorld } from './FsWorld';
import { FsuProvider, FsuChange } from '../fsu-provider';

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
  fetchDirentBody: (id: string, bodyType: Fs.BodyType) => Promise<Fs.WorldFsBody>;
  applyTransientChanges: (change: Fs.WrenchAstBodyChange) => Promise<Fs.WorldFsBody>;
  debugDirent: (debug: { id: string; input?: string; inputCSV?: string }) => Promise<Fs.DebugResponse>;
}

const FsDirentContext = React.createContext<FsDirentContextType | undefined>(undefined);

export interface FsDirentProviderProps {
  persistenceUnit: {
    fetchDirents: () => Promise<Fs.DirentBase[]>;
    fetchDirentBody: (id: string, bodyType: Fs.BodyType) => Promise<Fs.WorldFsBody>;
    applyTransientChanges: (change: Fs.WrenchAstBodyChange) => Promise<Fs.WorldFsBody>;
    debugDirent: (debug: { id: string; input?: string; inputCSV?: string }) => Promise<Fs.DebugResponse>;
    pushChange: (change: FsuChange) => Promise<void>;
  }
  children: React.ReactNode;
}

export const FsDirentProvider: React.FC<FsDirentProviderProps> = (props) => {

  const intl = useIntl();
  const { enqueueSnackbar } = useSnackbar();
  const [dirents, setDirents] = React.useState<FsWorld>(() => new FsWorld({ dirents: [] }));

  React.useEffect(() => {
    props.persistenceUnit.fetchDirents()
      .then(dirents => {
        console.log('dirents', dirents);
        return new FsWorld({ dirents })
      })
      .then(setDirents)

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
      getParentDirent: (childId) => dirents.getParentDirent(childId),
      getArticleName: (id) => dirents.getArticleName(id),
      fetchDirentBody: props.persistenceUnit.fetchDirentBody,
      applyTransientChanges: props.persistenceUnit.applyTransientChanges,
      debugDirent: props.persistenceUnit.debugDirent,
    };
  }, [dirents]);

  async function handlePushChange(change: FsuChange): Promise<void> {
    try {
      await props.persistenceUnit.pushChange(change);
      const name = dirents.getDirent(change.id)?.name ?? change.id;
      const type = change.bodyType.toLowerCase().replace(/_/g, ' ');
      enqueueSnackbar(intl.formatMessage({ id: 'fs.snackbar.saveSuccess' }, { name, type }), { variant: 'success' });
      props.persistenceUnit.fetchDirents()
        .then(dirents => new FsWorld({ dirents }))
        .then(setDirents);
    } catch (error: any) {
      enqueueSnackbar(intl.formatMessage({ id: 'fs.snackbar.saveFailed' }, { cause: error?.message ?? 'N/A' }), { variant: 'error' });
    }
  }

  return (
    <FsDirentContext.Provider value={contextValue}>
      <FsuProvider pushChange={handlePushChange}>
        {props.children}
      </FsuProvider>
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
