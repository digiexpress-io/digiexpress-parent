import React from 'react';
import { FsDirentCommentsProps } from './FsDirentCommentsProps';
import { useFsTheme } from '../fs-theme';

export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDirentCommentsProps): OwnerState => {
  const { isDarkMode } = useFsTheme();

  return React.useMemo(() => ({ isDarkMode }), [isDarkMode]);
}