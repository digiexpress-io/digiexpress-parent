import React from 'react';

interface FsNavContextType {
  isDarkMode: boolean;
  expandedIds: string[];
  isExpanded: (id: string) => boolean;
  setIsDarkMode: (isDarkMode: boolean) => void;
  setExpanded: (id: string, value: boolean) => void;
  setExpandedBatch: (ids: string[], value: boolean) => void;
  collapseAll: () => void;
}

const FsNavContext = React.createContext<FsNavContextType | undefined>(undefined);

export interface FsNavProviderProps {
  children: React.ReactNode;
}

export const FsNavProvider: React.FC<FsNavProviderProps> = (props) => {
  const [isDarkMode, setIsDarkMode] = React.useState(false);
  const [expandedIds, setExpandedIds] = React.useState<string[]>([]);

  const setExpanded = React.useCallback((id: string, isExpanded: boolean) => {
    setExpandedIds(prev => {
      if (isExpanded) return [...prev, id];
      return prev.filter(i => i !== id);
    });
  }, []);

  const setExpandedBatch = React.useCallback((ids: string[], isExpanded: boolean) => {
    setExpandedIds(prev => {
      if (isExpanded) return [...prev, ...ids];
      return prev.filter(i => !ids.includes(i));
    });
  }, []);

  const collapseAll = React.useCallback(() => {
    setExpandedIds([]);
  }, []);

  const contextValue: FsNavContextType = React.useMemo(() => ({
    isExpanded: (id) => expandedIds.includes(id),
    isDarkMode,
    setIsDarkMode,
    expandedIds,
    collapseAll,
    setExpanded,
    setExpandedBatch,
  }), [isDarkMode, expandedIds, setExpanded, collapseAll, setExpandedBatch]);

  return (
    <FsNavContext.Provider value={contextValue}>
      {props.children}
    </FsNavContext.Provider>
  );
};

export function useFsNavContext(): FsNavContextType {
  const result = React.useContext(FsNavContext);
  if (!result) {
    throw new Error('FsNavContext is not created!');
  }
  return result;
}
