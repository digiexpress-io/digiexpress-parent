import React from 'react';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { useFsTheme } from '../fs-theme';


export interface CreateOwnerState {
  isDarkMode: boolean;
  locales: Fs.SelectOption[];
  contentType: Fs.LinkType;
  isExpanded: boolean;
  onChangeContentType: (value: string) => void;
  onToggleExpanded: () => void;
}

export const useCreateOwnerState = (): CreateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { selectOptions } = useFsDirent();
  const locales = selectOptions.languages;

  const [contentType, setContentType] = React.useState<Fs.LinkType>('internal');
  const [isExpanded, setIsExpanded] = React.useState(false);

  function onChangeContentType(value: string) {
    setContentType(value as Fs.LinkType);
  }

  function onToggleExpanded() {
    setIsExpanded(prev => !prev);
  }

  return ({ isDarkMode, locales, contentType, isExpanded, onChangeContentType, onToggleExpanded });
};
