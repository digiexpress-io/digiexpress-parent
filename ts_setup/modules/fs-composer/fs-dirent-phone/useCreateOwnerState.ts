import React from 'react';
import { Fs, useFsDirent, useFsNav } from '@dxs-ts/fs-api';


export interface CreateOwnerState {
  isDarkMode: boolean;
  parentFolder: Fs.DirentBase | undefined;
  locales: Fs.SelectOption[];
  isExpanded: boolean;
  onToggleExpanded: () => void;
}

export const useCreateOwnerState = (props: { parentFolder: Fs.DirentBase | undefined; pathToTopParent: string | undefined }): CreateOwnerState => {
  const { isDarkMode } = useFsNav();
  const { selectOptions } = useFsDirent();
  const [isExpanded, setIsExpanded] = React.useState(false);
  const locales = selectOptions.languages;

  function onToggleExpanded() {
    setIsExpanded(prev => !prev);
  }

  return ({ isDarkMode, parentFolder: props.parentFolder, locales, isExpanded, onToggleExpanded });
};
