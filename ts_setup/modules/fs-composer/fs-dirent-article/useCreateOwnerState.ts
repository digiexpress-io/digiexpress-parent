import React from 'react';
import { Fs, useFsNav } from '@dxs-ts/fs-api';


export interface CreateOwnerState {
  isDarkMode: boolean;
  parentFolder: Fs.Dirent | undefined;
  parentArticle: Fs.Dirent | undefined;
  parentArticlePath: string | undefined;
  isExpanded: boolean;
  onToggleExpanded: () => void;
}

function getParentArticlePath(pathToTopParent: string | undefined, parentArticle: Fs.Dirent): string {
  if (pathToTopParent) {
    return `${pathToTopParent} / ${parentArticle.name}`;
  }
  return parentArticle.name;
}

export const useCreateOwnerState = (props: { parentFolder: Fs.Dirent | undefined; pathToTopParent: string | undefined }): CreateOwnerState => {
  const { isDarkMode } = useFsNav();
  const [isExpanded, setIsExpanded] = React.useState(false);

  const parentArticle = props.parentFolder?.type === 'article' ? props.parentFolder : undefined;
  const parentArticlePath = parentArticle ? getParentArticlePath(props.pathToTopParent, parentArticle) : undefined;

  function onToggleExpanded() {
    setIsExpanded(prev => !prev);
  }

  return ({ isDarkMode, parentFolder: props.parentFolder, parentArticle, parentArticlePath, isExpanded, onToggleExpanded });
};
