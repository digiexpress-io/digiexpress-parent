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
  iconFolder: string;
  iconArticle: string;
  iconService: string;
  iconForm: string;
  iconFlow: string;
  iconLink: string;
  iconLanguage: string;
  iconReference: string;
  iconExpand: string;
}

export type EveliTreeClassKey = keyof EveliTreeClasses;

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    title: ['title'],
    titleText: ['titleText'],
    icon: ['icon'],
    iconFolder: ['iconFolder'],
    iconArticle: ['iconArticle'],
    iconService: ['iconService'],
    iconForm: ['iconForm'],
    iconFlow: ['iconFlow'],
    iconLink: ['iconLink'],
    iconLanguage: ['iconLanguage'],
    iconReference: ['iconReference'],
    iconExpand: ['iconExpand']
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
      styles.icon,
      styles.iconFolder,
      styles.iconArticle,
      styles.iconService,
      styles.iconForm,
      styles.iconFlow,
      styles.iconLink,
      styles.iconLanguage,
      styles.iconReference
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
      minWidth: 10,
      marginRight: theme.spacing(1),
      fontSize: '15px',
    },

    [`& .${MUI_NAME}-iconFolder`]: {
      minWidth: 10,
      marginRight: theme.spacing(1),
      color: '#cccccc',
      '& .MuiSvgIcon-root': {
        fontSize: '15px',
      },
    },

    [`& .${MUI_NAME}-iconArticle`]: {
      minWidth: 10,
      marginRight: theme.spacing(1),
      color: '#dcdcaa',
      '& .MuiSvgIcon-root': {
        fontSize: '15px',
      },
    },

    [`& .${MUI_NAME}-iconService`]: {
      minWidth: 10,
      marginRight: theme.spacing(1),
      color: '#4ec9b0',
      '& .MuiSvgIcon-root': {
        fontSize: '15px',
      },
    },

    [`& .${MUI_NAME}-iconForm`]: {
      minWidth: 10,
      marginRight: theme.spacing(1),
      color: '#9cdcfe',
      '& .MuiSvgIcon-root': {
        fontSize: '15px',
      },
    },

    [`& .${MUI_NAME}-iconFlow`]: {
      minWidth: 10,
      marginRight: theme.spacing(1),
      color: '#c586c0',
      '& .MuiSvgIcon-root': {
        fontSize: '15px',
      },
    },

    [`& .${MUI_NAME}-iconLink`]: {
      minWidth: 10,
      marginRight: theme.spacing(1),
      color: '#569cd6',
      '& .MuiSvgIcon-root': {
        fontSize: '15px',
      },
    },

    [`& .${MUI_NAME}-iconLanguage`]: {
      minWidth: 10,
      marginRight: theme.spacing(1),
      color: '#ce9178',
      '& .MuiSvgIcon-root': {
        fontSize: '15px',
      },
    },

    [`& .${MUI_NAME}-iconReference`]: {
      minWidth: 10,
      marginRight: theme.spacing(1),
      color: '#569cd6',
      '& .MuiSvgIcon-root': {
        fontSize: '15px',
      },
    },

    [`& .${MUI_NAME}-iconExpand`]: {
      fontSize: '15px',
      color: '#cccccc',
    },
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

export const getIconClassName = (node: TreeNode, classes: EveliTreeClasses) => {
  if (node.isReference) {
    return classes.iconReference;
  }
  switch (node.type) {
    case 'folder':
      return classes.iconFolder;
    case 'article':
      return classes.iconArticle;
    case 'service':
      return classes.iconService;
    case 'form':
      return classes.iconForm;
    case 'flow':
      return classes.iconFlow;
    case 'link':
      return classes.iconLink;
    case 'language':
      return classes.iconLanguage;
    default:
      return classes.iconFolder;
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