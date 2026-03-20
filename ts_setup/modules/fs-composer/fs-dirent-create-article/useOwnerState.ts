import React from 'react';
import { FsDirent, useFsNav } from '@dxs-ts/fs-api';
import { FsDirentCreateArticleProps } from './FsDirentCreateArticleProps';


export interface OwnerState {
  isDarkMode: boolean;
  parentFolder: FsDirent | undefined;
  isExpanded: boolean;
  onToggleExpanded: () => void;
}

export const useOwnerState = (props: FsDirentCreateArticleProps): OwnerState => {
  const { isDarkMode } = useFsNav();
  const [isExpanded, setIsExpanded] = React.useState(false);

  function onToggleExpanded() {
    setIsExpanded(prev => !prev);
  }

  return ({ isDarkMode, parentFolder: props.parentFolder, isExpanded, onToggleExpanded });
}