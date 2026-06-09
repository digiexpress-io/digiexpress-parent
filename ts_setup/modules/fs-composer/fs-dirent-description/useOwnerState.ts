import React from 'react';
import { FsDirentDescriptionProps } from './FsDirentDescriptionProps';
import { useFsTheme } from '../fs-theme';

export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDirentDescriptionProps): OwnerState => {
  const { isDarkMode } = useFsTheme();

  return React.useMemo(() => ({ isDarkMode }), [isDarkMode]);
};
