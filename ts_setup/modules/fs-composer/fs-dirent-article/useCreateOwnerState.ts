import React from 'react';
import { Fs, useFsNav } from '@dxs-ts/fs-api';
import { useFsRouteNav } from '@dxs-ts/fs-nav';


export interface CreateOwnerState {
  isDarkMode: boolean;
  parentArticle: Fs.DirentBase | undefined;
  parentArticlePath: string | undefined;
  isExpanded: boolean;
  onToggleExpanded: () => void;
}

export const useCreateOwnerState = (): CreateOwnerState => {
  const { isDarkMode } = useFsNav();
  const { activeTabPath, openTabs, activeTabIndex } = useFsRouteNav();
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
