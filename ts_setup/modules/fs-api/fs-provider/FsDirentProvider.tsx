import React from 'react';
import { useIntl } from 'react-intl';
import { useSnackbar } from 'notistack';
import { Fs } from '../fs-types';
import { ItemReferencesEntry, FsWorld } from './FsWorld';
import { FsuChange, FsuCreateChange } from '../fsu-provider';

export type { ItemReferencesEntry };

export interface FsDirentContextType {
  dirents: Fs.DirentBase[];
  selectOptions: Fs.SelectOptions;
  getParentDirent(childId: string): Fs.DirentBase | undefined;
  getDirentName: (id: string) => string | undefined;
  getDirent: (id: string) => Fs.DirentBase | undefined;
  isChildError: (dirent: Fs.DirentBase) => boolean;
  findReferencesToDirent: (dirent: Fs.DirentBase) => ItemReferencesEntry[];
  fetchDirentBody: (id: string, bodyType: Fs.BodyType) => Promise<Fs.WorldFsBody>;
  applyTransientChanges: (change: Fs.WrenchAstBodyChange) => Promise<Fs.WorldFsBody>;
  debugDirent: (debug: { id: string; input?: string; inputCSV?: string }) => Promise<Fs.DebugResponse>;
  compilePrintoutPage: (serviceId: string, localeCode: string) => Promise<Fs.PrintoutCompileResult>;

  updateDirent(change: FsuChange): Promise<void>;
  updateDirentDescription(id: string, text?: string): Promise<void>;
  updateDirentName(id: string, name: string): Promise<void>;
  copyDirent(id: string, newObjectName: string): Promise<Fs.DirentBase>;

  updateDirentLabels(id: string, values: Fs.DescriptionLabel[]): Promise<void>;
  deleteDirent(id: string, bodyType: Fs.BodyType): Promise<void>;
  createDirent(change: FsuCreateChange): Promise<Fs.DirentBase>;
}


const FsDirentContext = React.createContext<FsDirentContextType | undefined>(undefined);

export interface FsDirentProviderProps {
  persistenceUnit: {
    fetchDirents: () => Promise<Fs.DirentBase[]>;
    fetchDirentBody: (id: string, bodyType: Fs.BodyType) => Promise<Fs.WorldFsBody>;
    applyTransientChanges: (change: Fs.WrenchAstBodyChange) => Promise<Fs.WorldFsBody>;
    debugDirent: (debug: { id: string; input?: string; inputCSV?: string }) => Promise<Fs.DebugResponse>;
    compilePrintoutPage: (serviceId: string, localeCode: string) => Promise<Fs.PrintoutCompileResult>;
    pushChange: (change: FsuChange) => Promise<void>;
    pushCreate: (change: FsuCreateChange) => Promise<string>;
    pushDescription: (props: { id: string; text?: string }) => Promise<void>;
    pushName: (props: { id: string, name: string }) => Promise<void>;
    pushCopy: (props: { id: string, newObjectName: string }) => Promise<string>;
    pushLabels: (props: { id: string; values: Fs.DescriptionLabel[] }) => Promise<void>;
    deleteDirent: (id: string, bodyType: Fs.BodyType) => Promise<void>;
  }
  children: React.ReactNode;
}

export const FsDirentProvider: React.FC<FsDirentProviderProps> = (props) => {

  const { persistenceUnit } = props;
  const notify = useNotify();
  const [dirents, setDirents] = React.useState<FsWorld>(() => new FsWorld({ dirents: [] }));

  React.useEffect(() => {
    persistenceUnit.fetchDirents()
      .then(dirents => {
        return new FsWorld({ dirents })
      })
      .then(setDirents)

  }, []);

  const refresh = React.useCallback(async () => {
    const updated = await persistenceUnit.fetchDirents();
    const next = new FsWorld({ dirents: updated });
    setDirents(next);
    return next;
  }, []);

  const contextValue: FsDirentContextType = React.useMemo(() => _initCtx({
    dirents, persistenceUnit, notify, refresh
  }), [dirents]);

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


function _initCtx(initProps: {
  dirents: FsWorld,
  persistenceUnit: FsDirentProviderProps['persistenceUnit'],
  notify: Notify,
  refresh: () => Promise<FsWorld>;
}): FsDirentContextType {

  const { dirents, persistenceUnit, notify, refresh } = initProps;

  async function updateDirent(change: FsuChange): Promise<void> {
    try {
      await persistenceUnit.pushChange(change);
      const bodyName = dirents.getDirentName(change.id) ?? change.id;
      notify({ id: 'fs.snackbar.saveSuccess', bodyName, bodyType: change.bodyType });
      await refresh();
    } catch (error: any) {
      notify({ id: 'fs.snackbar.saveFailed', error, bodyType: change.bodyType });
    }
  }

  async function updateDirentDescription(id: string, text?: string): Promise<void> {
    const bodyType = dirents.getDirent(id)?.type ?? 'ARTICLE';
    try {
      await persistenceUnit.pushDescription({ id, text });
      const bodyName = dirents.getDirentName(id) ?? id;
      notify({ id: 'fs.snackbar.saveSuccess', bodyName, bodyType });
      refresh();
    } catch (error: any) {
      notify({ id: 'fs.snackbar.saveFailed', error, bodyType });
    }
  }

  async function updateDirentName(id: string, name: string): Promise<void> {
    const bodyType = dirents.getDirent(id)?.type ?? 'ARTICLE';
    try {
      await persistenceUnit.pushName({ id, name });
      const bodyName = dirents.getDirentName(id) ?? id;
      notify({ id: 'fs.snackbar.saveSuccess', bodyName, bodyType });
      refresh();
    } catch (error: any) {
      notify({ id: 'fs.snackbar.saveFailed', error, bodyType });
    }
  }

  async function copyDirent(id: string, newObjectName: string): Promise<Fs.DirentBase> {
    const bodyType = dirents.getDirent(id)?.type ?? 'ARTICLE';
    try {
      const newId = await persistenceUnit.pushCopy({ id, newObjectName });
      notify({ id: 'fs.snackbar.saveSuccess', bodyType });
      const updated = await refresh();
      return updated.getDirent(newId)!;
    } catch (error: any) {
      notify({ id: 'fs.snackbar.saveFailed', error, bodyType });
      throw error;
    }
  }

  async function updateDirentLabels(id: string, values: Fs.DescriptionLabel[]): Promise<void> {
    const bodyType = dirents.getDirent(id)?.type ?? 'ARTICLE';
    try {
      await persistenceUnit.pushLabels({ id, values });
      const bodyName = dirents.getDirentName(id) ?? id;
      notify({ id: 'fs.snackbar.saveSuccess', bodyName, bodyType });
      refresh();
    } catch (error: any) {
      notify({ id: 'fs.snackbar.saveFailed', error, bodyType });
    }
  }

  async function pushDelete(id: string, bodyType: Fs.BodyType): Promise<void> {
    try {
      await persistenceUnit.deleteDirent(id, bodyType);
      const bodyName = dirents.getDirent(id)?.name ?? id;
      notify({ id: 'fs.snackbar.deleteSuccess', bodyName, bodyType });
      refresh();
    } catch (error: any) {
      notify({ id: 'fs.snackbar.deleteFailed', error, bodyType });
    }
  }

  async function createDirent(change: FsuCreateChange): Promise<Fs.DirentBase> {
    try {
      const newId = await persistenceUnit.pushCreate(change);
      notify({ id: 'fs.snackbar.saveSuccess', bodyType: change.bodyType });

      const updated = await refresh();
      return updated.getDirent(newId)!;
    } catch (error: any) {
      notify({ id: 'fs.snackbar.saveFailed', error, bodyType: change.bodyType });
      throw error;
    }
  }

  return {

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

    getParentDirent: (childId) => dirents.getParentDirent(childId),
    getDirentName: (id) => dirents.getDirentName(id),
    fetchDirentBody: persistenceUnit.fetchDirentBody,
    applyTransientChanges: persistenceUnit.applyTransientChanges,
    debugDirent: persistenceUnit.debugDirent,
    compilePrintoutPage: persistenceUnit.compilePrintoutPage,
    deleteDirent: pushDelete,
    createDirent,
    updateDirent,
    updateDirentDescription,
    updateDirentName,
    copyDirent,
    updateDirentLabels,
  };
}



type Notify = (props: {
  id: string,
  bodyType: Fs.BodyType,
  bodyName?: string,
  error?: any
}) => void;

function useNotify(): Notify {
  const intl = useIntl();
  const { enqueueSnackbar } = useSnackbar();

  return React.useCallback((props) => {

    const type: string = intl.formatMessage({ id: `fs.bodyType.${props.bodyType}` });
    const name: string = props.bodyName ?? type;
    const cause: string | undefined = (props.error ? props.error.message : undefined) ?? 'N/A';

    // trigger alarm
    if (props.error) {
      const msg = intl.formatMessage({ id: props.id }, { cause: cause });
      enqueueSnackbar(msg, { variant: 'error' });
    } else {
      const msg = intl.formatMessage({ id: 'fs.snackbar.saveSuccess' }, { name, type });
      enqueueSnackbar(msg, { variant: 'success' });
    }
  }, [intl, enqueueSnackbar])
}