import React from 'react';
import { useFsTheme } from '../fs-theme';


export interface CreateOwnerState {
  isDarkMode: boolean;
  isExpanded: boolean;
  onToggleExpanded: () => void;
}

export const useCreateOwnerState = (): CreateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const [isExpanded, setIsExpanded] = React.useState(false);

  function onToggleExpanded() {
    setIsExpanded(prev => !prev);
  }

  return ({ isDarkMode, isExpanded, onToggleExpanded });
};
