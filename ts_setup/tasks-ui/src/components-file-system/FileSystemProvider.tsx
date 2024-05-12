import React from 'react';
import { FileSystemProvider as FsProvider } from './FileSystemContext';
import { FileSystemExpanderProvider } from './FileSystemExpanderContext';
import { FileSystemTreeFocusProvider } from './FileSystemTreeFocus';
import { FileSystemFolderFocusProvider } from './FileSystemFolderFocus';

export interface FileSystemProviderProps {
  children: React.ReactNode;
}


export const FileSystemProvider: React.FC<FileSystemProviderProps> = ({ children }) => {


  return (<FsProvider>
    <FileSystemExpanderProvider>
      <FileSystemTreeFocusProvider>
        <FileSystemFolderFocusProvider>
          {children}
        </FileSystemFolderFocusProvider>
      </FileSystemTreeFocusProvider>
    </FileSystemExpanderProvider>
  </FsProvider>);
}
