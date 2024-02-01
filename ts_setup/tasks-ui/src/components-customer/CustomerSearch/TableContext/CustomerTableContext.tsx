import React from 'react';

import { PopperProvider, usePopper } from 'descriptor-popper';
import { CustomerPrefsProvider } from './CustomerPrefsContext';

export const useTable = usePopper;


export const CustomerTableProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  return (
    <CustomerPrefsProvider><PopperProvider><>{children}</></PopperProvider></CustomerPrefsProvider>
  );
}



