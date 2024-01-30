import React from 'react';


import Context from 'context';
import { PopperProvider, usePopper } from 'descriptor-popper';

export const useTaskPopper = usePopper;

export const TaskPopperContext: React.FC<{children: React.ReactNode}> = ({children}) => {
  return (<PopperProvider><>{children}</></PopperProvider>);
}