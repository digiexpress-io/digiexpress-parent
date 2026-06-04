import React from 'react';
import { FsDirentMenuNewProps } from './FsDirentMenuNewProps';
import { useFsTheme } from '../fs-theme';

export interface OwnerState {
  isDarkMode: boolean;
}

export const useOwnerState = (_props: FsDirentMenuNewProps): OwnerState => {
  const { isDarkMode } = useFsTheme();

  return React.useMemo(() => ({ isDarkMode }), [isDarkMode]);
};
