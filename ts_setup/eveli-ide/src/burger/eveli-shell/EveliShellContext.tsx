import React from 'react';

export interface EveliShellContextType {
  open: boolean;
  fullscreen: boolean;
  toggleDrawer(): void;
  toggleFullscreen(): void;
}

export const EveliShellContext = React.createContext<EveliShellContextType>({} as any);


export const EveliShellProvider: React.FC<{ children: React.ReactNode, drawerOpen: boolean }> = (props) => {
  const [open, setOpen] = React.useState(props.drawerOpen ?? true);
  const [fullscreen, setFullscreen] = React.useState(false);


  const contextValue: EveliShellContextType = React.useMemo(() => {
    function toggleDrawer() { setOpen((prev) => !prev) }
    function toggleFullscreen() { setFullscreen((prev) => !prev) }
    return Object.freeze({ open, fullscreen, toggleDrawer, toggleFullscreen })
  }, [open, fullscreen]);

  return (<EveliShellContext.Provider value={contextValue}>
    {props.children}
  </EveliShellContext.Provider >);
}

export const useEveliShell = () => {
  return React.useContext(EveliShellContext);
}

