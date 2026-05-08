import React from 'react';
import { Fs } from '@dxs-ts/fs-api';
import { useFsNav } from '@dxs-ts/fs-nav';
import { useFsTheme } from '../fs-theme';


export interface CreateOwnerState {
  isDarkMode: boolean;
  parentArticle: Fs.DirentBase | undefined;
  parentArticlePath: string | undefined;
  isExpanded: boolean;
  onToggleExpanded: () => void;
}

export const useCreateOwnerState = (): CreateOwnerState => {
  const { isDarkMode } = useFsTheme();
  const { activeTabPath, openTabs, activeTabIndex } = useFsNav();
  const [isExpanded, setIsExpanded] = React.useState(false);

  const activeTab = openTabs[activeTabIndex];
  const parentFolder = activeTab?.type === 'create' ? activeTab.parentFolder : undefined;
  const parentArticle = parentFolder?.type === 'ARTICLE' ? parentFolder : undefined;
  const parentArticlePath = parentArticle ? activeTabPath : undefined;

  function onToggleExpanded() {
    setIsExpanded(prev => !prev);
  }

  return ({ isDarkMode, parentArticle, parentArticlePath, isExpanded, onToggleExpanded });
};
