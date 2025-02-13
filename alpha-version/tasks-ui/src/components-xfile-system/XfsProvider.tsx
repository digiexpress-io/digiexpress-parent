import React from 'react';
import { XfsProvider as XfsContextProvider } from './XfsContext';
import { XfsTreeFocusProvider } from './XfsTreeFocus';
import { XfsFolderFocusProvider } from './XfsFolderFocus';

export interface XfsProviderProps {
  children: React.ReactNode;
}


export const XfsProvider: React.FC<XfsProviderProps> = ({ children }) => {

  return (<XfsContextProvider>

    <XfsTreeFocusProvider>
      <XfsFolderFocusProvider>
        {children}
      </XfsFolderFocusProvider>
    </XfsTreeFocusProvider>

  </XfsContextProvider>);
}
