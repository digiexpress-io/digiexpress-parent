import React from 'react';
import { FsDirent, useFsNav } from '@dxs-ts/fs-api';
import { FsDirentCreateLinkProps } from './FsDirentCreateLinkProps';

const MOCK_LOCALES = ['en', 'fi', 'sv'];

export interface OwnerState {
  isDarkMode: boolean;
  parentFolder: FsDirent | undefined;
  locales: string[];
  isExpanded: boolean;
  onToggleExpanded: () => void;
}

export const useOwnerState = (props: FsDirentCreateLinkProps): OwnerState => {
  const { isDarkMode } = useFsNav();
  const [isExpanded, setIsExpanded] = React.useState(false);

  function onToggleExpanded() {
    setIsExpanded(prev => !prev);
  }

  return ({ isDarkMode, parentFolder: props.parentFolder, locales: MOCK_LOCALES, isExpanded, onToggleExpanded });
}
