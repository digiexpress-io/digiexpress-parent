import React from 'react';


import { PopperContextType, Popper } from './popper-types';
import { ImmutablePopper } from './ImmutablePopper';

export const PopperContext = React.createContext<PopperContextType>({} as any);


type WithPopperOpen = (popperId: string, popperOpen: boolean, anchorEl?: HTMLElement) => void;
type WithPopperToggle = (popperId: string, anchorEl?: HTMLElement) => void;


export const PopperProvider: React.FC<{ children: React.ReactElement }> = ({ children }) => {
  const [state, setState] = React.useState(new ImmutablePopper({popperOpen: false}));

  const withPopperOpen: WithPopperOpen = React.useCallback(
    (popperId, popperOpen, anchorEl) => setState(prev => prev.withPopperOpen(popperId, popperOpen, anchorEl)), 
    [setState]);

  const withPopperToggle: WithPopperToggle = React.useCallback(
    (popperId, anchorEl) => setState(prev => prev.withPopperToggle(popperId, anchorEl)), 
    [setState]);

  const contextValue: PopperContextType = React.useMemo(() => {
    return { state, withPopperOpen, withPopperToggle };
  }, [state, withPopperOpen]);

  return (<PopperContext.Provider value={contextValue}>{children}</PopperContext.Provider>);
}

export function usePopper() {
  const ctx: PopperContextType = React.useContext(PopperContext);
  return ctx;
}