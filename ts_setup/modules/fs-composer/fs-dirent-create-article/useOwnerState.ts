import React from 'react';
import { FsDirent, useFsNav } from '@dxs-ts/fs-api';
import { FsDirentCreateArticleProps } from './FsDirentCreateArticleProps';


export interface OwnerState {
  isDarkMode: boolean;
  parentFolder: FsDirent | undefined;
  parentArticle: FsDirent | undefined;
  parentArticlePath: string | undefined;
  isExpanded: boolean;
  onToggleExpanded: () => void;
}

function getParentArticlePath(pathToTopParent: string | undefined, parentArticle: FsDirent): string {
  if (pathToTopParent) {
    return `${pathToTopParent} / ${parentArticle.name}`;
  }
  return parentArticle.name;
}

export const useOwnerState = (props: FsDirentCreateArticleProps): OwnerState => {
  const { isDarkMode } = useFsNav();
  const [isExpanded, setIsExpanded] = React.useState(false);

  const parentArticle = props.parentFolder?.type === 'article' ? props.parentFolder : undefined;
  const parentArticlePath = parentArticle ? getParentArticlePath(props.pathToTopParent, parentArticle) : undefined;

  function onToggleExpanded() {
    setIsExpanded(prev => !prev);
  }

  return ({ isDarkMode, parentFolder: props.parentFolder, parentArticle, parentArticlePath, isExpanded, onToggleExpanded });
}