import React from 'react';
import { useIntl } from 'react-intl';
import { useSnackbar } from 'notistack';
import { Fs } from '../fs-types';
import { ItemReferencesEntry, FsWorld } from './FsWorld';
import { FsuChange, FsuCreateChange } from '../fsu-provider';

export type { ItemReferencesEntry };

export interface FsDirentContextType {
  dirents: Fs.DirentBase[];
  creatableTypes: Fs.BodyType[];
  selectOptions: Fs.SelectOptions;
  getParentDirent(childId: string): Fs.DirentBase | undefined;
  getDirentName: (id: string) => string | undefined;
  getDirent: (id: string) => Fs.DirentBase | undefined;
  isChildError: (dirent: Fs.DirentBase) => boolean;
  findReferencesToDirent: (dirent: Fs.DirentBase) => ItemReferencesEntry[];
  fetchDirentBody: (id: string, bodyType: Fs.BodyType) => Promise<Fs.WorldFsBody>;
  applyTransientChanges: (change: Fs.WrenchAstBodyChange) => Promise<Fs.WorldFsBody>;
  debugDirent: (debug: { id: string; input?: string; inputCSV?: string }) => Promise<Fs.DebugResponse>;

  updateDirent(change: FsuChange): Promise<void>;
  deleteDirent(id: string, bodyType: Fs.BodyType): Promise<void>;
  createDirent(change: FsuCreateChange): Promise<Fs.DirentBase>;
}

const ALL_TYPES: Fs.BodyType[] = [
  'ARTICLE',
  'ARTICLE_PAGE',
  'ARTICLE_LINK',
  'ARTICLE_WORKFLOW',
  'ARTICLE_TEMPLATE',
  'FLOW',
  'FLOW_TASK',
  'DECISION_TABLE',
  'LOCALE',
  'PRINTOUT',
  'PRINTOUT_PAGE',
  'PRINTOUT_RESOURCE',
  'FOLDER',
  'DIALOB_FORM',
];

const FsDirentContext = React.createContext<FsDirentContextType | undefined>(undefined);

export interface FsDirentProviderProps {
  persistenceUnit: {
    fetchDirents: () => Promise<Fs.DirentBase[]>;
    fetchDirentBody: (id: string, bodyType: Fs.BodyType) => Promise<Fs.WorldFsBody>;
    applyTransientChanges: (change: Fs.WrenchAstBodyChange) => Promise<Fs.WorldFsBody>;
    debugDirent: (debug: { id: string; input?: string; inputCSV?: string }) => Promise<Fs.DebugResponse>;
    pushChange: (change: FsuChange) => Promise<void>;
    pushCreate: (change: FsuCreateChange) => Promise<string>;
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
        console.log('dirents', dirents);
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
      refresh();
    } catch (error: any) {
      notify({ id: 'fs.snackbar.saveFailed', error, bodyType: change.bodyType });
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

    getParentDirent: (childId) => dirents.getParentDirent(childId),
    getDirentName: (id) => dirents.getDirentName(id),
    fetchDirentBody: persistenceUnit.fetchDirentBody,
    applyTransientChanges: persistenceUnit.applyTransientChanges,
    debugDirent: persistenceUnit.debugDirent,
    deleteDirent: pushDelete,
    createDirent,
    updateDirent
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