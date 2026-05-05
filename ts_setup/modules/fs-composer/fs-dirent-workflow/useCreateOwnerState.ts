import React from 'react';
import { Fs, useFsDirent, useFsNav } from '@dxs-ts/fs-api';


export interface CreateOwnerState {
  isDarkMode: boolean;
  locales: Fs.SelectOption[];
  isExpanded: boolean;
  onToggleExpanded: () => void;
}

export const useCreateOwnerState = (): CreateOwnerState => {
  const { isDarkMode } = useFsNav();
  const { selectOptions } = useFsDirent();
  const [isExpanded, setIsExpanded] = React.useState(false);

  const locales = selectOptions.languages;

  function onToggleExpanded() {
    setIsExpanded(prev => !prev);
  }

  return ({ isDarkMode, locales, isExpanded, onToggleExpanded });
};
