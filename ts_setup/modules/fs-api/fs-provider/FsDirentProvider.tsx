import React from 'react';
import { Fs } from '../fs-types';
import { mockFsDirentProperties } from '../mock-fs-dirent-properties';
import { mockFsData } from '../mock-fs-data';
import { ALL_DIRENTS, collectArticles, collectDialobs, collectFlows, collectLanguages, getConfigOptionsForType } from './helpers';


export interface ItemReferencesEntry {
  assetName: string;
  location: string;
}

export interface FsDirentContextType {
  direntPropsLoading: boolean;
  dirents: Fs.DirentBase[];
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
  children: React.ReactNode;
}

export const FsDirentProvider: React.FC<FsDirentProviderProps> = (props) => {
  const [propsMap, setPropsMap] = React.useState<Record<string, Fs.Props>>({});
  const [direntPropsLoading, setDirentPropsLoading] = React.useState(true);

  React.useEffect(() => {
    Promise.resolve(mockFsDirentProperties).then((data) => {
      setPropsMap(data);
      setDirentPropsLoading(false);
    });
  }, []);

  const getDirent = React.useCallback(<T extends Fs.DirentAsset>(id: string): T | undefined => {
    const dirent = ALL_DIRENTS[id];
    const direntProps = propsMap[id];
    if (!dirent || !direntProps) {
      return undefined;
    }
    return { ...dirent, ...direntProps } as unknown as T;
  }, [propsMap]);

  const selectOptions = React.useMemo((): Fs.SelectOptions => ({
    articles: collectArticles(mockFsData),
    flows: collectFlows(mockFsData),
    dialobs: collectDialobs(mockFsData),
    languages: collectLanguages(mockFsData),
    direntProps: propsMap,
    collectDialobTags: (dialobId: string): Fs.SelectOption[] => {
      const entry = propsMap[dialobId];
      if (!entry || entry.type !== 'dialob') { return []; }
      const tags = (entry as Fs.DialobProps).versionTags;
      if (!tags || tags.length === 0) { return []; }
      return tags.map(tag => ({ value: tag, label: tag }));
    },
    getActiveDialobTag: (props: Fs.DialobProps): string => {
      const tags = props.versionTags;
      if (!tags || tags.length === 0) { return 'LATEST'; }
      return tags[tags.length - 1];
    },
  }), [propsMap]);

  const isChildError = React.useCallback((dirent: Fs.DirentBase): boolean => {
    const direntProps = propsMap[dirent.id];
    if (direntProps?.errors && direntProps.errors.length > 0) {
      return true;
    }
    if (dirent.children) {
      return dirent.children.some(child => isChildError(child));
    }
    return false;
  }, [propsMap]);

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

    mockFsData.forEach(rootDirent => searchInDirent(rootDirent));

    return references;
  }, [propsMap]);

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
      dirents: mockFsData,
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
  }, [direntPropsLoading, selectOptions, getDirent, isChildError, findReferencesToDirent, updateDirent, setExpanded, setExpandedBatch, collapseAll]);

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

