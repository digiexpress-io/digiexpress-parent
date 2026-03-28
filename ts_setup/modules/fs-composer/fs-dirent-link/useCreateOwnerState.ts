import React from 'react';
import { FsDirent, mockFsData, useFsNav, FsDirentData, mockFsDirentProperties } from '@dxs-ts/fs-api';


const data = new FsDirentData(mockFsData, mockFsDirentProperties);

export interface CreateOwnerState {
  isDarkMode: boolean;
  parentFolder: FsDirent | undefined;
  locales: string[];
  isExpanded: boolean;
  onToggleExpanded: () => void;
}

export const useCreateOwnerState = (props: { parentFolder: FsDirent | undefined; pathToTopParent: string | undefined }): CreateOwnerState => {
  const { isDarkMode } = useFsNav();
  const locales = data.languages;

  const [isExpanded, setIsExpanded] = React.useState(false);

  function onToggleExpanded() {
    setIsExpanded(prev => !prev);
  }

  return ({ isDarkMode, parentFolder: props.parentFolder, locales, isExpanded, onToggleExpanded });
};
