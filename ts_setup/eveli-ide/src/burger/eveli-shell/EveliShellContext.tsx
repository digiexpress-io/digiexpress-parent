import React from 'react';
import { EveliShellToolbarHeightOptions } from './EveliShellProps';

export interface EveliShellContextType {
  open: boolean;
  toolbarHeight: EveliShellToolbarHeightOptions;
  footerHeight: number;
  drawerWidth: number;
  fullscreen: boolean;
  minibarWidth: number;
  toggleDrawer(): void;
  toggleFullscreen(): void;
}

export const EveliShellContext = React.createContext<EveliShellContextType>({} as any);


export const EveliShellProvider: React.FC<{ 
  
  children: React.ReactNode, drawerOpen: boolean 
  toolbarHeight: EveliShellToolbarHeightOptions;
  footerHeight: number;
  drawerWidth: number;
  minibarWidth: number;

}> = (props) => {
  const [open, setOpen] = React.useState(props.drawerOpen ?? true);
  const [fullscreen, setFullscreen] = React.useState(false);
  
  const contextValue: EveliShellContextType = React.useMemo(() => {
    function toggleDrawer() { setOpen((prev) => !prev) }
    function toggleFullscreen() { setFullscreen((prev) => !prev) }
    return Object.freeze({ 
      open, fullscreen, 
      toolbarHeight: props.toolbarHeight,
      footerHeight: props.footerHeight,
      drawerWidth: props.drawerWidth,
      minibarWidth: props.minibarWidth,
      
      toggleDrawer, toggleFullscreen
     })
  }, [open, fullscreen, props]);

  return (<EveliShellContext.Provider value={contextValue}>
    {props.children}
  </EveliShellContext.Provider >);
}

export const useEveliShell = () => {
  return React.useContext(EveliShellContext);
}

