import React from 'react';
import { useFsNav } from '@dxs-ts/fs-nav';


export interface CreateOwnerState {
  isDarkMode: boolean;
  isExpanded: boolean;
  onToggleExpanded: () => void;
}

export const useCreateOwnerState = (): CreateOwnerState => {
  const { isDarkMode } = useFsNav();
  const [isExpanded, setIsExpanded] = React.useState(false);

  function onToggleExpanded() {
    setIsExpanded(prev => !prev);
  }

  return ({ isDarkMode, isExpanded, onToggleExpanded });
};
