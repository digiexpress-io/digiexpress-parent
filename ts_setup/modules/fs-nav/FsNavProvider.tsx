import React from 'react';
import { useSearch, useNavigate } from '@tanstack/react-router';

interface FsNavContextType {
  expandedIds: string[];
  isExpanded: (id: string) => boolean;
  setExpanded: (id: string, value: boolean) => void;
  setExpandedBatch: (ids: string[], value: boolean) => void;
  collapseAll: () => void;
}

const FsNavContext = React.createContext<FsNavContextType | undefined>(undefined);

export interface FsNavProviderProps {
  children: React.ReactNode;
}

export const FsNavProvider: React.FC<FsNavProviderProps> = (props) => {
  const search = useSearch({ from: '/secured/$locale/worker/filesystem/' });
  const navigate = useNavigate();
  const [expandedIds, setExpandedIds] = React.useState<string[]>(search.expandedIds ?? []);

  const isMounted = React.useRef(false);

  React.useEffect(() => {
    if (!isMounted.current) {
      isMounted.current = true;
      return;
    }
    navigate({
      from: '/secured/$locale/worker/filesystem',
      to: '.',
      search: (prev: any) => ({ ...prev, expandedIds }),
      replace: true,
    });
  }, [expandedIds]);

  const setExpanded = React.useCallback((id: string, isExpanded: boolean) => {
    setExpandedIds(prev => {
      if (isExpanded) {
        return [...prev, id];
      }
      return prev.filter(i => i !== id);
    });
  }, []);

  const setExpandedBatch = React.useCallback((ids: string[], isExpanded: boolean) => {
    setExpandedIds(prev => {
      if (isExpanded) {
        return [...prev, ...ids];
      }
      return prev.filter(i => !ids.includes(i));
    });
  }, []);

  const collapseAll = React.useCallback(() => {
    setExpandedIds([]);
  }, []);

  const contextValue: FsNavContextType = React.useMemo(() => ({
    isExpanded: (id) => expandedIds.includes(id),
    expandedIds,
    collapseAll,
    setExpanded,
    setExpandedBatch,
  }), [expandedIds, setExpanded, collapseAll, setExpandedBatch]);

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
