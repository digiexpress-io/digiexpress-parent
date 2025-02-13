import React from 'react';

export interface FlyoutMenuContextType {
  anchorEl: HTMLElement | undefined
  open: boolean;
  onOpen: (event: React.MouseEvent<HTMLElement>) => void;
  onClose: (event: React.MouseEvent<HTMLElement>) => void;
}

export const FlyoutMenuContext = React.createContext<FlyoutMenuContextType>({} as any);

export const FlyoutMenuProvider: React.FC<{children: React.ReactNode}> = ({children}) => {
  const [anchorEl, setAnchorEl] = React.useState<HTMLElement>();
  
  const onOpen = React.useCallback((event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  }, [setAnchorEl]);

  const onClose = React.useCallback((event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(undefined);
  }, [setAnchorEl]);

  const contextValue: FlyoutMenuContextType = React.useMemo(() => {
    const open = Boolean(anchorEl);
    return Object.freeze({ open, anchorEl, onOpen, onClose });
  }, [anchorEl, onOpen]);

  return (<FlyoutMenuContext.Provider value={contextValue}>{children}</FlyoutMenuContext.Provider>);
}


export function useFlyoutMenu() {
  const result: FlyoutMenuContextType = React.useContext(FlyoutMenuContext);
  return result;
}