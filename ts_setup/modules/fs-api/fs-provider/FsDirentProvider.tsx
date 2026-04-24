import React from 'react';
import { Fs } from '../fs-types';
import { mockFsDirentProperties } from '../mock-fs-dirent-properties';
import { ALL_TYPES, flattenDirents, collectArticles, collectDialobs, collectFlows, collectLanguages, collectLabels, getConfigOptionsForType } from './helpers';


export interface ItemReferencesEntry {
  assetName: string;
  location: string;
}

export interface FsDirentContextType {
  direntPropsLoading: boolean;
  dirents: Fs.DirentBase[];
  creatableTypes: Fs.Type[];
  selectOptions: Fs.SelectOptions;
  getConfigOptionsForType: (type: Fs.Type) => Fs.SelectOption[];
  getDirent: <T extends Fs.DirentAsset>(id: string) => T | undefined;
  isChildError: (dirent: Fs.DirentBase) => boolean;
  findReferencesToDirent: (dirent: Fs.DirentBase) => ItemReferencesEntry[];
  updateDirent: (id: string, updated: Partial<Fs.Props>) => void;
  setExpanded: (id: string, value: boolean) => void;
  setExpandedBatch: (ids: string[], value: boolean) => void;
  collapseAll: () => void;
}

const FsDirentContext = React.createContext<FsDirentContextType | undefined>(undefined);

export interface FsDirentProviderProps {
  persistenceUnit: {
    fetchDirents: () => Promise<Fs.DirentBase[]>;
  }
  children: React.ReactNode;
}

export const FsDirentProvider: React.FC<FsDirentProviderProps> = (props) => {
  const [dirents, setDirents] = React.useState<Fs.DirentBase[]>([]);
  const [allDirents, setAllDirents] = React.useState<Record<string, Fs.DirentBase>>({});
  const [propsMap, setPropsMap] = React.useState<Record<string, Fs.Props>>({});



  React.useEffect(() => {

    props.persistenceUnit.fetchDirents()
      .then(data => {
        setDirents(data);
        setAllDirents(flattenDirents(data));

        // setPropsMap(data);
      })
    console.log("dirents", dirents)
  }, []);


  const getDirent = React.useCallback(<T extends Fs.DirentAsset>(id: string): T | undefined => {

    const dirent = allDirents[id];
    const direntProps = propsMap[id];
    if (!dirent || !direntProps) {
      return undefined;
    }
    return { ...dirent, ...direntProps } as unknown as T;
  }, [propsMap, dirents]);

  const selectOptions = React.useMemo((): Fs.SelectOptions => ({
    articles: collectArticles(dirents),
    flows: collectFlows(dirents),
    dialobs: collectDialobs(dirents),
    languages: collectLanguages(dirents),
    labels: collectLabels(propsMap),
    direntProps: propsMap,
    collectDialobTags: (dialobId: string): Fs.SelectOption[] => {
      const entry = propsMap[dialobId];
      if (!entry || entry.type !== 'DIALOB_FORM') { return []; }

      const tags = (entry as Fs.DialobProps).versionTags;

      if (!tags || tags.length === 0) { return []; }
      return tags.map(tag => ({ value: tag, label: tag }));
    },
    getActiveDialobTag: (props: Fs.DialobProps): string => {
      const tags = props.versionTags;
      if (!tags || tags.length === 0) { return 'LATEST'; }
      return tags[tags.length - 1];
    },
  }), [propsMap, dirents]);

  const isChildError = React.useCallback((dirent: Fs.DirentBase): boolean => {
    const direntProps = propsMap[dirent.id];
    if (direntProps?.errors && direntProps.errors.length > 0) {
      return true;
    }
    if (dirent.children) {
      return dirent.children.some(child => isChildError(child));
    }
    return false;
  }, [propsMap, dirents]);

  const findReferencesToDirent = React.useCallback((targetDirent: Fs.DirentBase): ItemReferencesEntry[] => {
    const references: ItemReferencesEntry[] = [];

    function searchInDirent(dirent: Fs.DirentBase, path: string[] = []): void {
      const currentPath = [...path, dirent.name];
      const direntProps = propsMap[dirent.id];

      if (direntProps?.reference && dirent.name === targetDirent.name && dirent.id !== targetDirent.id) {
        references.push({
          assetName: dirent.name,
          location: currentPath.slice(0, -1).join(' / ')
        });
      }

      if (dirent.children) {
        dirent.children.forEach(child => searchInDirent(child, currentPath));
      }
    }

    dirents.forEach(rootDirent => searchInDirent(rootDirent));

    return references;
  }, [propsMap, dirents]);

  const updateDirent = React.useCallback((id: string, updated: Partial<Fs.Props>) => {
    setPropsMap(prev => {
      if (!prev[id]) {
        return prev;
      }
      return { ...prev, [id]: { ...prev[id], ...updated } as Fs.Props };
    });
  }, []);

  const setExpanded = React.useCallback((id: string, value: boolean) => {
    setPropsMap(prev => {
      if (!prev[id]) return prev;
      return { ...prev, [id]: { ...prev[id], expanded: value } };
    });
  }, []);

  const setExpandedBatch = React.useCallback((ids: string[], value: boolean) => {
    setPropsMap(prev => {
      const next = { ...prev };
      ids.forEach(id => {
        if (next[id]) {
          next[id] = { ...next[id], expanded: value };
        }
      });
      return next;
    });
  }, []);

  const collapseAll = React.useCallback(() => {
    setPropsMap(prev => {
      const next = { ...prev };
      Object.keys(next).forEach(id => {
        next[id] = { ...next[id], expanded: false };
      });
      return next;
    });
  }, []);

  const contextValue: FsDirentContextType = React.useMemo(() => {
    return {
      direntPropsLoading,
      dirents: dirents,
      creatableTypes: ALL_TYPES,
      selectOptions,
      getConfigOptionsForType,
      getDirent,
      isChildError,
      findReferencesToDirent,
      updateDirent,
      setExpanded,
      setExpandedBatch,
      collapseAll,
    };
  }, [direntPropsLoading, selectOptions, dirents, getDirent, isChildError, findReferencesToDirent, updateDirent, setExpanded, setExpandedBatch, collapseAll]);

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

