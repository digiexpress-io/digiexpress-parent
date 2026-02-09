import React from 'react';
import { generateUtilityClass, styled, Badge, useTheme, ListItem, ListItemText, Typography, Tooltip, alpha, Box } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { TreeColors, getNodeColor } from '../tree-theme';

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

import { ConfigOption, TreeNode, TreeNodeType, useEveliTree } from '../../eveli-tree-api';



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
  shouldForwardProp: (prop) => prop !== 'isDarkTheme',
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
})<{ isDarkTheme: boolean }>(({ theme, isDarkTheme }) => {
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
      color: isDarkTheme ? TreeColors.dark.text : TreeColors.light.textSecondary,
    },

    [`& .${MUI_NAME}-iconConfig`]: {
      fontSize: '14px',
      color: isDarkTheme ? TreeColors.semantic.warning : TreeColors.semantic.warningLight,
    },
  };
});

export function getIcon(node: TreeNode) {
  const { isDarkMode } = useEveliTree();

  const getBaseIcon = () => {
    switch (node.type) {
      case 'folder':
        return node.expanded ? <FolderOpenIcon /> : <FolderIcon />;
      case 'article':
        return node.expanded ? <ArticleOutlinedIcon /> : <ArticleIcon />;
      case 'service':
        return node.expanded ? <SettingsOutlinedIcon /> : <SettingsIcon />;
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

  if (node.error) {
    return (
      <Box display='flex' alignItems='center' sx={{ color: isDarkMode ? TreeColors.semantic.dangerDark : TreeColors.semantic.dangerLight }}>
        {baseIcon}
      </Box>
    );
  }
  if (node.reference) {
    return (
      <Badge variant="dot"
        anchorOrigin={{
          vertical: 'bottom',
          horizontal: 'right',
        }}
        sx={{
          '& .MuiBadge-dot': {
            backgroundColor: isDarkMode ? TreeColors.semantic.dangerDark : TreeColors.semantic.dangerLight,
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

export const StyledListItem = styled(ListItem, {
  shouldForwardProp: (prop) =>
    prop !== 'isDarkTheme' &&
    prop !== 'level' &&
    prop !== 'error'
})<{
  level: number, isDarkTheme: boolean, error: boolean
}>(({ theme, level, isDarkTheme, error }) => ({

  paddingLeft: theme.spacing(level * 1.2),
  cursor: 'pointer',
  '&:hover': {
    backgroundColor: isDarkTheme ? TreeColors.dark.surface : TreeColors.light.surface,
  },
  ...error && {
    backgroundColor: alpha(isDarkTheme ? TreeColors.semantic.dangerDark : TreeColors.semantic.dangerLight, 0.1),
    '&:hover': {
      backgroundColor: isDarkTheme ? alpha(TreeColors.semantic.dangerDark, 0.3) : alpha(TreeColors.semantic.dangerLight, 0.2),
    },
  }
}));

interface StyledListItemTextProps {
  nodeType: TreeNodeType;
  nodeName: string;
  description?: string;
  isDarkTheme: boolean;
  error: boolean;
}

export const StyledListItemText: React.FC<StyledListItemTextProps> = ({
  nodeType, nodeName, description, isDarkTheme, error
}) => {
  return (
    <ListItemText primary={<Typography variant='subtitle2' sx={{
        color: error
          ? (isDarkTheme ? TreeColors.semantic.dangerDark : TreeColors.semantic.dangerLight)
          : getNodeColor(nodeType, isDarkTheme),
        fontWeight: isDarkTheme ? 400 : 500
      }}
    >
      {nodeName}
      {description && (
        <Typography component='span' variant='caption'
          sx={{
            ml: 1,
            color: TreeColors.dark.textMuted,
            fontStyle: 'italic'
          }}
        >
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

