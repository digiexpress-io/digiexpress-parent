import React from 'react';
import { IconButton, useTheme, Collapse } from '@mui/material';

import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown';
import KeyboardArrowUpIcon from '@mui/icons-material/KeyboardArrowUp';


export interface XPanderContextType {
  open: boolean;
  setOpen: (open: boolean) => void;
}

export const XPanderContext = React.createContext<XPanderContextType>({} as any);

export const XPanderProvider: React.FC<{children: React.ReactNode}> = ({children}) => {
  const [open, setOpen] = React.useState(false);

  const contextValue: XPanderContextType = React.useMemo(() => {  
    return Object.freeze({ open, setOpen });
  }, [open, setOpen]);
  return (<XPanderContext.Provider value={contextValue}>{children}</XPanderContext.Provider>);
}

export function useXPander() {
  const result: XPanderContextType = React.useContext(XPanderContext);
  return result;
}


export const XCollpase: React.FC<{children: React.ReactNode}> = ({ children }) => {
  const { open } = useXPander();
  return (<Collapse in={open} timeout="auto" unmountOnExit sx={{width: '100%'}}>
    {children}
  </Collapse>)
}

export const XPanderButton: React.FC<{}> = ({}) => {
  const { open, setOpen } = useXPander();

  return (<IconButton
    size="small"
    onClick={() => setOpen(!open)}>
    {open ? <KeyboardArrowUpIcon /> : <KeyboardArrowDownIcon />}
  </IconButton>);
}