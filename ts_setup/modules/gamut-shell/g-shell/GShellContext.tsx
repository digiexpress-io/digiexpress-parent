import React from 'react';

export interface GShellContextType {
  open: boolean;
  fullscreen: boolean;
  toggleDrawer(): void;
  toggleFullscreen(): void;
}

export const GShellContext = React.createContext<GShellContextType>({} as any);


export const GShellProvider: React.FC<{ children: React.ReactNode, drawerOpen: boolean }> = (props) => {
  const [open, setOpen] = React.useState(props.drawerOpen ?? true);
  const [fullscreen, setFullscreen] = React.useState(false);


  const contextValue: GShellContextType = React.useMemo(() => {
    function toggleDrawer() { setOpen((prev) => !prev) }
    function toggleFullscreen() { setFullscreen((prev) => !prev) }
    return Object.freeze({ open, fullscreen, toggleDrawer, toggleFullscreen })
  }, [open, fullscreen]);

  return (<GShellContext.Provider value={contextValue}>

    {props.children}

  </GShellContext.Provider >);
}

export const useGShell = () => {
  return React.useContext(GShellContext);
}

