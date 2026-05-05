import React from 'react';
import { Fs, useFsNav } from '@dxs-ts/fs-api';


export interface CreateOwnerState {
  isDarkMode: boolean;
  parentFolder: Fs.DirentBase | undefined;
  parentArticle: Fs.DirentBase | undefined;
  parentArticlePath: string | undefined;
  isExpanded: boolean;
  onToggleExpanded: () => void;
}

export const useCreateOwnerState = (props: { parentFolder: Fs.DirentBase | undefined }): CreateOwnerState => {
  const { isDarkMode, activeTabPath } = useFsNav();
  const [isExpanded, setIsExpanded] = React.useState(false);

  const parentArticle = props.parentFolder?.type === 'ARTICLE' ? props.parentFolder : undefined;
  const parentArticlePath = parentArticle ? activeTabPath : undefined;

  function onToggleExpanded() {
    setIsExpanded(prev => !prev);
  }

  return ({ isDarkMode, parentFolder: props.parentFolder, parentArticle, parentArticlePath, isExpanded, onToggleExpanded });
};
