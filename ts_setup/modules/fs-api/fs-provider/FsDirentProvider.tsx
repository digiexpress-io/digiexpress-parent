import React from 'react';
import { FsDirent, FsDirentEntry, FsDirentProps, FsDirentType, SelectOption } from '../fs-types';
import { FsDirentData } from '../FsDirentData';
import { mockFsDirentProperties } from '../mock-fs-dirent-properties';
import { mockFsData } from '../mock-fs-data';


function collectDirents(result: Record<string, FsDirent>, node: FsDirent): void {
  result[node.id] = node;
  node.children.forEach(child => collectDirents(result, child));
}

function flattenDirents(nodes: FsDirent[]): Record<string, FsDirent> {
  const result: Record<string, FsDirent> = {};
  nodes.forEach(node => collectDirents(result, node));
  return result;
}

const ALL_DIRENTS = flattenDirents(mockFsData);

const ALL_CONFIG_OPTIONS: SelectOption[] = [
  { value: 'devMode', label: 'Development mode' },
  { value: 'assignableMode', label: 'Assignable mode' },
  { value: 'disabledMode', label: 'Disabled mode' },
  { value: 'anonymousMode', label: 'Anonymous mode' },
];

function getConfigOptionsForType(type: FsDirentType): SelectOption[] {
  switch (type) {
    case 'link':
    case 'phone': {
      return ALL_CONFIG_OPTIONS.filter(o => o.value === 'devMode' || o.value === 'disabledMode');
    }
    case 'service':
    case 'article': {
      return ALL_CONFIG_OPTIONS;
    }
    case 'language': {
      return ALL_CONFIG_OPTIONS.filter(o => o.value === 'disabledMode');
    }
    case 'page': {
      return ALL_CONFIG_OPTIONS.filter(o => o.value === 'disabledMode' || o.value === 'devMode');
    }
    case 'printout': {
      return ALL_CONFIG_OPTIONS.filter(o => o.value === 'devMode');
    }
    default: {
      return [];
    }
  }
}

export interface ItemReferencesEntry {
  assetName: string;
  location: string;
}

export interface FsDirentContextType {
  direntPropsLoading: boolean;
  dirents: FsDirent[];
  selectOptions: FsDirentData;
  getConfigOptionsForType: (type: FsDirentType) => SelectOption[];
  getDirent: <T extends FsDirentEntry>(id: string) => T | undefined;
  isChildError: (dirent: FsDirent) => boolean;
  findReferencesToDirent: (dirent: FsDirent) => ItemReferencesEntry[];
  setExpanded: (id: string, value: boolean) => void;
  setExpandedBatch: (ids: string[], value: boolean) => void;
  collapseAll: () => void;
}

const FsDirentContext = React.createContext<FsDirentContextType | undefined>(undefined);

export interface FsDirentProviderProps {
  children: React.ReactNode;
}

export const FsDirentProvider: React.FC<FsDirentProviderProps> = (props) => {
  const [propsMap, setPropsMap] = React.useState<Record<string, FsDirentProps>>({});
  const [direntPropsLoading, setDirentPropsLoading] = React.useState(true);

  React.useEffect(() => {
    Promise.resolve(mockFsDirentProperties).then((data) => {
      setPropsMap(data);
      setDirentPropsLoading(false);
    });
  }, []);

  const getDirent = React.useCallback(<T extends FsDirentEntry>(id: string): T | undefined => {
    const dirent = ALL_DIRENTS[id];
    const direntProps = propsMap[id];
    if (!dirent || !direntProps) {
      return undefined;
    }
    return { ...dirent, ...direntProps } as unknown as T;
  }, [propsMap]);

  const selectOptions = React.useMemo(
    () => new FsDirentData(mockFsData, propsMap),
    [propsMap]
  );

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

  const contextValue: FsDirentContextType = React.useMemo(() => {
    return {
      direntPropsLoading,
      dirents: mockFsData,
      selectOptions,
      getConfigOptionsForType,
      getDirent,
      isChildError,
      findReferencesToDirent,
      setExpanded,
      setExpandedBatch,
      collapseAll,
    };
  }, [direntPropsLoading, selectOptions, getDirent, isChildError, findReferencesToDirent, setExpanded, setExpandedBatch, collapseAll]);

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
