import React from 'react';
import { FsDirent, FsDirentProps } from '../fs-types';
import { mockFsDirentProperties } from '../mock-fs-dirent-properties';
import { mockFsData } from '../mock-fs-data';

export interface ItemReferencesEntry {
  assetName: string;
  location: string;
}

export interface FsDirentPropsContextType {
  direntPropsLoading: boolean;
  getDirentProps: (id: string) => FsDirentProps;
  isChildError: (dirent: FsDirent) => boolean;
  findReferencesToDirent: (dirent: FsDirent) => ItemReferencesEntry[];
  setExpanded: (id: string, value: boolean) => void;
  setExpandedBatch: (ids: string[], value: boolean) => void;
  collapseAll: () => void;
}

const FsDirentPropsContext = React.createContext<FsDirentPropsContextType | undefined>(undefined);

export interface FsDirentPropsProviderProps {
  children: React.ReactNode;
}

const EMPTY_DIRENT_PROPS: FsDirentProps = {
  id: '',
  expanded: false,
  reference: false,
  locked: false,
  configOptions: [],
  comments: [],
  changes: [],
  permissions: [],
  labels: [],
  errors: [],
};

export const FsDirentPropsProvider: React.FC<FsDirentPropsProviderProps> = (props) => {
  const [propsMap, setPropsMap] = React.useState<Record<string, FsDirentProps>>({});
  const [direntPropsLoading, setDirentPropsLoading] = React.useState(true);
  const propsMapRef = React.useRef(propsMap);
  propsMapRef.current = propsMap;

  React.useEffect(() => {
    Promise.resolve(mockFsDirentProperties).then((data) => {
      setPropsMap(data);
      setDirentPropsLoading(false);
    });
  }, []);

  const getDirentProps = React.useCallback((id: string): FsDirentProps => {
    return propsMapRef.current[id] ?? EMPTY_DIRENT_PROPS;
  }, []);

  const isChildError = React.useCallback((dirent: FsDirent): boolean => {
    const direntProps = propsMap[dirent.id];
    if (direntProps?.errors && direntProps.errors.length > 0) {
      return true;
    }
    if (dirent.children) {
      return dirent.children.some(child => isChildError(child));
    }
    return false;
  }, [propsMap]);

  const findReferencesToDirent = React.useCallback((targetDirent: FsDirent): ItemReferencesEntry[] => {
    const references: ItemReferencesEntry[] = [];

    function searchInDirent(dirent: FsDirent, path: string[] = []): void {
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

  const contextValue: FsDirentPropsContextType = React.useMemo(() => {
    return {
      direntPropsLoading,
      getDirentProps,
      isChildError,
      findReferencesToDirent,
      setExpanded,
      setExpandedBatch,
      collapseAll,
    };
  }, [direntPropsLoading, getDirentProps, isChildError, findReferencesToDirent, setExpanded, setExpandedBatch, collapseAll]);

  return (
    <FsDirentPropsContext.Provider value={contextValue}>
      {props.children}
    </FsDirentPropsContext.Provider>
  );
};

export function useFsDirentProps(): FsDirentPropsContextType {
  const result = React.useContext(FsDirentPropsContext);
  if (!result) {
    throw new Error('FsDirentPropsContext is not created!');
  }
  return result;
}
