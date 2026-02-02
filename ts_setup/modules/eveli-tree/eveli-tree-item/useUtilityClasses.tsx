import React from 'react';
import { generateUtilityClass, styled, Badge, useTheme, ListItem, ListItemText, Typography, Tooltip } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';

import {
  Folder as FolderIcon,
  FolderOpen as FolderOpenIcon,
  Article as ArticleIcon,
  ArticleOutlined as ArticleOutlinedIcon,
  Description as FormIcon,
  Settings as SettingsIcon,
  SettingsOutlined as SettingsOutlinedIcon,
  Link as LinkIcon,
  Language as LanguageIcon,
  Build as FlowIcon,
  PrintOutlined as PrintIcon,
  ImageOutlined as ImageIcon,
  PictureAsPdf as PdfIcon,
  Construction as DevModeIcon,
  Assignment as AssignmentIcon,
  Block as DisabledIcon,
  VisibilityOff as AnonymousIcon,
} from '@mui/icons-material';

import { ConfigOption, TreeNode, TreeNodeType } from '../../eveli-tree-api';



export const MUI_NAME = 'EveliTreeItem';

export interface EveliTreeItemClasses {
  root: string;
  icon: string;
  iconFolder: string;
  iconArticle: string;
  iconService: string;
  iconDialob: string;
  iconFlow: string;
  iconLink: string;
  iconLanguage: string;
  iconPrintout: string;
  iconImage: string;
  iconTemplate: string;
  iconExpand: string;
  iconConfig: string;
}

export type EveliTreeItemClassKey = keyof EveliTreeItemClasses;

export const useUtilityClasses = (_isDarkTheme: boolean) => {
  const slots = {
    root: ['root'],
    icon: ['icon'],
    iconFolder: ['iconFolder'],
    iconArticle: ['iconArticle'],
    iconService: ['iconService'],
    iconDialob: ['iconDialob'],
    iconFlow: ['iconFlow'],
    iconLink: ['iconLink'],
    iconLanguage: ['iconLanguage'],
    iconPrintout: ['iconPrintout'],
    iconImage: ['iconImage'],
    iconTemplate: ['iconTemplate'],
    iconExpand: ['iconExpand'],
    iconConfig: ['iconConfig']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

export const EveliTreeItemRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.icon,
      styles.iconFolder,
      styles.iconArticle,
      styles.iconService,
      styles.iconDialob,
      styles.iconFlow,
      styles.iconLink,
      styles.iconLanguage,
      styles.iconPrintout,
      styles.iconImage,
      styles.iconTemplate,
      styles.iconExpand,
      styles.iconConfig
    ];
  },
})<{ isDarkTheme?: boolean }>(({ theme, isDarkTheme }) => {
  return {
    [`& .${MUI_NAME}-icon`]: {
      minWidth: 10,
      marginRight: theme.spacing(1),
      fontSize: '15px',
    },

    [`& .${MUI_NAME}-iconFolder`]: {
      minWidth: 10,
      marginRight: theme.spacing(1),
      color: getNodeColor('folder', isDarkTheme),
      '& .MuiSvgIcon-root': {
        fontSize: '15px',
      },
    },

    [`& .${MUI_NAME}-iconArticle`]: {
      minWidth: 10,
      marginRight: theme.spacing(1),
      color: getNodeColor('article', isDarkTheme),
      '& .MuiSvgIcon-root': {
        fontSize: '15px',
      },
    },

    [`& .${MUI_NAME}-iconService`]: {
      minWidth: 10,
      marginRight: theme.spacing(1),
      color: getNodeColor('service', isDarkTheme),
      '& .MuiSvgIcon-root': {
        fontSize: '15px',
      },
    },

    [`& .${MUI_NAME}-iconDialob`]: {
      minWidth: 10,
      marginRight: theme.spacing(1),
      color: getNodeColor('dialob', isDarkTheme),
      '& .MuiSvgIcon-root': {
        fontSize: '15px',
      },
    },

    [`& .${MUI_NAME}-iconFlow`]: {
      minWidth: 10,
      marginRight: theme.spacing(1),
      color: getNodeColor('flow', isDarkTheme),
      '& .MuiSvgIcon-root': {
        fontSize: '15px',
      },
    },

    [`& .${MUI_NAME}-iconLink`]: {
      minWidth: 10,
      marginRight: theme.spacing(1),
      color: getNodeColor('link', isDarkTheme),
      '& .MuiSvgIcon-root': {
        fontSize: '15px',
      },
    },

    [`& .${MUI_NAME}-iconLanguage`]: {
      minWidth: 10,
      marginRight: theme.spacing(1),
      color: getNodeColor('language', isDarkTheme),
      '& .MuiSvgIcon-root': {
        fontSize: '15px',
      },
    },

    [`& .${MUI_NAME}-iconPrintout`]: {
      minWidth: 10,
      marginRight: theme.spacing(1),
      color: getNodeColor('printout', isDarkTheme),
      '& .MuiSvgIcon-root': {
        fontSize: '15px',
      },
    },

    [`& .${MUI_NAME}-iconImage`]: {
      minWidth: 10,
      marginRight: theme.spacing(1),
      color: getNodeColor('image', isDarkTheme),
      '& .MuiSvgIcon-root': {
        fontSize: '15px',
      },
    },

    [`& .${MUI_NAME}-iconTemplate`]: {
      minWidth: 10,
      marginRight: theme.spacing(1),
      color: getNodeColor('template', isDarkTheme),
      '& .MuiSvgIcon-root': {
        fontSize: '15px',
      },
    },

    [`& .${MUI_NAME}-iconExpand`]: {
      fontSize: '15px',
      color: isDarkTheme ? '#cccccc' : '#666666',
    },

    [`& .${MUI_NAME}-iconConfig`]: {
      fontSize: '14px',
      color: isDarkTheme ? '#ffa500' : '#d90429',
      opacity: 0.8,
      '& .MuiSvgIcon-root': {
        fontSize: 'inherit',
        color: 'inherit',
      },
    },
  };
});

export function getIcon(node: TreeNode) {
  const getBaseIcon = () => {
    switch (node.type) {
      case 'folder':
        return node.isExpanded ? <FolderOpenIcon /> : <FolderIcon />;
      case 'article':
        return node.isExpanded ? <ArticleOutlinedIcon /> : <ArticleIcon />;
      case 'service':
        return node.isExpanded ? <SettingsOutlinedIcon /> : <SettingsIcon />;
      case 'dialob':
        return <FormIcon />;
      case 'flow':
        return <FlowIcon />;
      case 'link':
        return <LinkIcon />;
      case 'language':
        return <LanguageIcon />;
      case 'printout':
        return <PrintIcon />;
      case 'image':
        return <ImageIcon />;
      case 'template':
        return <PdfIcon />;
      default:
        return <ArticleIcon />;
    }
  };

  const baseIcon = getBaseIcon();
  const theme = useTheme();

  if (node.isReference) {
    return (
      <Badge variant="dot"
        anchorOrigin={{
          vertical: 'bottom',
          horizontal: 'right',
        }}
        sx={{
          '& .MuiBadge-dot': {
            backgroundColor: theme.palette.error.main,
            width: 6,
            height: 6,
          },
        }}
      >
        {baseIcon}
      </Badge>
    );
  }

  return baseIcon;
};

export const StyledListItem = styled(ListItem)<{ level: number; isDarkTheme?: boolean }>(({ theme, level, isDarkTheme }) => ({
  paddingLeft: theme.spacing(level * 1.2),
  cursor: 'pointer',
  '&:hover': {
    backgroundColor: isDarkTheme ? '#2d2d30' : '#f0f0f0',
  },
}));

interface StyledListItemTextProps {
  nodeType: TreeNodeType;
  nodeName: string;
  description?: string;
  isDarkTheme?: boolean;
}

export const StyledListItemText: React.FC<StyledListItemTextProps> = ({ nodeType, nodeName, description, isDarkTheme }) => {
  return (
    <ListItemText
      primary={
        <Typography variant='subtitle2' sx={{ color: getNodeColor(nodeType, isDarkTheme), fontWeight: isDarkTheme ? 400 : 500 }}>
          {nodeName}
          {description && (
            <Typography component='span' variant='caption' sx={{ ml: 1, color: isDarkTheme ? '#6a9955' : '#5d5b5b', fontStyle: 'italic' }}>
              - "{description}"
            </Typography>
          )}
        </Typography>
      }
    />
  );
}

export function getIconClassName(node: TreeNode, classes: EveliTreeItemClasses) {
  switch (node.type) {
    case 'folder':
      return classes.iconFolder;
    case 'article':
      return classes.iconArticle;
    case 'service':
      return classes.iconService;
    case 'dialob':
      return classes.iconDialob;
    case 'flow':
      return classes.iconFlow;
    case 'link':
      return classes.iconLink;
    case 'language':
      return classes.iconLanguage;
    case 'printout':
      return classes.iconPrintout;
    case 'image':
      return classes.iconImage;
    case 'template':
      return classes.iconTemplate;
    default:
      return classes.iconFolder;
  }
}

export function getConfigIcons(configOptions: ConfigOption[], iconClassName: string) {
  const icons: React.ReactElement[] = [];

  for (const config of configOptions) {
    if (config.devMode) {
      icons.push(
        <Tooltip key="dev" title="Development Mode" arrow>
          <DevModeIcon fontSize='small' className={iconClassName} />
        </Tooltip>
      );
    }
    if (config.assignableMode) {
      icons.push(
        <Tooltip key="assign" title="Assignable Mode" arrow>
          <AssignmentIcon fontSize='small' className={iconClassName} />
        </Tooltip>
      );
    }
    if (config.disabledMode) {
      icons.push(
        <Tooltip key="disabled" title="Disabled Mode" arrow>
          <DisabledIcon fontSize='small' className={iconClassName} />
        </Tooltip>
      );
    }
    if (config.anonymousMode) {
      icons.push(
        <Tooltip key="anonymous" title="Anonymous Mode" arrow>
          <AnonymousIcon fontSize='small' className={iconClassName} />
        </Tooltip>
      );
    }
  }

  return icons.length > 0 ? icons : [
    <Tooltip key="default" title="Configuration" arrow>
      <SettingsIcon fontSize='small' className={iconClassName} />
    </Tooltip>
  ];
}


export function getNodeColor(nodeType: TreeNodeType, isDarkTheme: boolean = false) {
  if (isDarkTheme) {
    switch (nodeType) {
      case 'folder':
        return '#e8e5e5'; // Gray - dark theme
      case 'article':
        return '#dcdcaa'; // Yellow for articles - dark theme
      case 'service':
        return '#4ec9b0'; // Teal for services - dark theme
      case 'dialob':
        return '#9cdcfe'; // Light blue for dialob forms - dark theme
      case 'flow':
        return '#c586c0'; // Purple for flows - dark theme
      case 'link':
        return '#98d982'; // Bright green for links - dark theme
      case 'language':
        return '#ce9178'; // Orange for languages - dark theme
      case 'printout':
        return '#f4b942'; // Golden yellow for printouts - dark theme
      case 'image':
        return '#ff6b6b'; // Coral red for images - dark theme
      case 'template':
        return '#ce9178'; // Orange for templates (same as language) - dark theme
      default:
        return '#cccccc'; // dark theme
    }
  } else {
    // Light theme colors (7:1 contrast)
    switch (nodeType) {
      case 'folder':
        return '#333333'; // Even darker gray for folders
      case 'article':
        return '#8b008b'; // Dark magenta for articles (7:1 contrast)
      case 'service':
        return '#1f5f3f'; // Darker sea green for services (7:1 contrast)
      case 'dialob':
        return '#0056b3'; // Darker blue for dialob forms (7:1 contrast)
      case 'flow':
        return '#8e2557'; // Dark fuchsia for flows (7:1 contrast)
      case 'link':
        return '#228b22'; // Forest green for links
      case 'language':
        return '#a0122a'; // Darker crimson red for languages (7:1 contrast)
      case 'printout':
        return '#2e1065'; // Dark indigo for printouts (7:1 contrast)
      case 'image':
        return '#a0122a'; // Darker crimson red for images (same as language, 7:1 contrast)
      case 'template':
        return '#a0122a'; // Darker crimson red for templates (same as language, 7:1 contrast)
      default:
        return '#666666';
    }
  }
}