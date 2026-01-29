import composeClasses from '@mui/utils/composeClasses';
import { generateUtilityClass, styled } from '@mui/material';

import { TreeNode } from './mock-tree-data';
import {
  Folder as FolderIcon,
  FolderOpen as FolderOpenIcon,
  Article as ArticleIcon,
  Description as FormIcon,
  Settings as SettingsIcon,
  Link as LinkIcon,
  Language as LanguageIcon,
  Build as FlowIcon,
} from '@mui/icons-material';

export const MUI_NAME = 'EveliTree';

export interface EveliTreeClasses {
  root: string;
  title: string;
  titleText: string;
  icon: string;
}

export type EveliTreeClassKey = keyof EveliTreeClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    title: ['title'],
    titleText: ['titleText'],
    icon: ['icon']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const EveliTreeRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.title,
      styles.titleText,
      styles.icon
    ];
  },
})(({ theme }) => {
  return {
    backgroundColor: '#1e1e1e',
    color: '#cccccc',
    minHeight: '100vh',
    fontSize: '13px',
    overflow: 'auto',

    [`& .${MUI_NAME}-title`]: {
      borderBottom: '1px solid #3c3c3c',
      padding: theme.spacing(1),
      backgroundColor: '#2d2d30',
    },

    [`& .${MUI_NAME}-titleText`]: {
      color: theme.palette.background.default,
      ...theme.typography.body1
    },

    [`& .${MUI_NAME}-icon`]: {
      minWidth: 20,
      marginRight: theme.spacing(1),
      fontSize: '16px',
    }
  };
});

export const getIcon = (node: TreeNode) => {
  switch (node.type) {
    case 'folder':
      return node.isExpanded ? <FolderOpenIcon /> : <FolderIcon />;
    case 'article':
      return <ArticleIcon />;
    case 'service':
      return <SettingsIcon />;
    case 'form':
      return <FormIcon />;
    case 'flow':
      return <FlowIcon />;
    case 'link':
      return <LinkIcon />;
    case 'language':
      return <LanguageIcon />;
    default:
      return <ArticleIcon />;
  }
};

export const getTextColor = (node: TreeNode) => {
  if (node.isReference) {
    return '#569cd6'; // Blue for references
  }
  switch (node.type) {
    case 'folder':
      return '#eee5e5';
    case 'article':
      return '#dcdcaa'; // Yellow for articles
    case 'service':
      return '#4ec9b0'; // Teal for services
    case 'form':
      return '#9cdcfe'; // Light blue for forms
    case 'flow':
      return '#c586c0'; // Purple for flows
    case 'link':
      return '#569cd6'; // Blue for links
    case 'language':
      return '#ce9178'; // Orange for languages
    default:
      return '#cccccc';
  }
};