import React from 'react';
import { HdesFileId } from './FileSystemTypes';

export interface FileSystemExpanderContextType {
  expanded: readonly HdesFileId[];
  getExpanded(id: HdesFileId): boolean;
  toggleExpanded(id: HdesFileId): void;
}

export const FileSystemExpanderContext = React.createContext<FileSystemExpanderContextType>({} as any);

export const FileSystemExpanderProvider: React.FC<{ children: React.ReactNode; }> = ({children}) => {
  const [expanded, setExpanded] = React.useState<readonly HdesFileId[]>([]);

  const contextValue: FileSystemExpanderContextType = React.useMemo(() => {

    function getExpanded(id: HdesFileId) {
      return expanded.includes(id);
    }

    function toggleExpanded(id: HdesFileId) {
      setExpanded(prev => {
        if(prev.includes(id)){
          const position = prev.indexOf(id);
          const next = prev.filter((p, index) => position !== index);
          return Object.freeze(next);
        }
        return Object.freeze([...prev, id]);
      });
    }

    return Object.freeze({ 
      expanded, getExpanded, toggleExpanded
    });
  }, [expanded]);

  return (<FileSystemExpanderContext.Provider value={contextValue}>
    {children}
  </FileSystemExpanderContext.Provider>);
}


export function useFileSystemExpander() {
  const result: FileSystemExpanderContextType = React.useContext(FileSystemExpanderContext);
  return result;
}