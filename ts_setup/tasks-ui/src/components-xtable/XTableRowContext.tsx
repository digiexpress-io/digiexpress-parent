import React from 'react';

export interface XTableRowContextType {
  rowId: number;
  hoverItemActive: boolean;
  onStartHover: () => void;
  onEndHover: () => void;
}

export const XTableRowContext = React.createContext<XTableRowContextType>({} as any);

export const XTableRowProvider: React.FC<{
  children: React.ReactNode,
  rowId: number
}> = ({ children, rowId }) => {

  const [hoverItemActive, setHoverItemsActive] = React.useState(false);

  const contextValue: XTableRowContextType = React.useMemo(() => {
    function onStartHover() {
      setHoverItemsActive(true);
    }
    function onEndHover() {
      setHoverItemsActive(false);
    }
    return Object.freeze({ rowId, hoverItemActive, onStartHover, onEndHover });
  }, [rowId, hoverItemActive]);

  return (<XTableRowContext.Provider value={contextValue}>{children}</XTableRowContext.Provider>);
}


export function useXTableRow() {
  const result: XTableRowContextType = React.useContext(XTableRowContext);
  return result;
}