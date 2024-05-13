import React from 'react';
import { XfsProvider as XfsContextProvider } from './XfsContext';
import { XfsExpanderProvider } from './XfsExpanderContext';
import { XfsTreeFocusProvider } from './XfsTreeFocus';
import { XfsFolderFocusProvider } from './XfsFolderFocus';

export interface XfsProviderProps {
  children: React.ReactNode;
}


export const XfsProvider: React.FC<XfsProviderProps> = ({ children }) => {

  return (<XfsContextProvider>
    <XfsExpanderProvider>
      <XfsTreeFocusProvider>
        <XfsFolderFocusProvider>
          {children}
        </XfsFolderFocusProvider>
      </XfsTreeFocusProvider>
    </XfsExpanderProvider>
  </XfsContextProvider>);
}
