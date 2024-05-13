import React from 'react';
import { HdesFileId } from './HdesFileSystemTypes';

export interface XfsExpanderContextType {
  expanded: readonly HdesFileId[];
  getExpanded(id: HdesFileId): boolean;
  toggleExpanded(id: HdesFileId): void;
}

export const XfsExpanderContext = React.createContext<XfsExpanderContextType>({} as any);

export const XfsExpanderProvider: React.FC<{ children: React.ReactNode; }> = ({children}) => {
  const [expanded, setExpanded] = React.useState<readonly HdesFileId[]>([]);

  const contextValue: XfsExpanderContextType = React.useMemo(() => {

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

  return (<XfsExpanderContext.Provider value={contextValue}>
    {children}
  </XfsExpanderContext.Provider>);
}


export function useXfsExpander() {
  const result: XfsExpanderContextType = React.useContext(XfsExpanderContext);
  return result;
}