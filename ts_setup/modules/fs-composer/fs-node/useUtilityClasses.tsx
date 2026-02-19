import React from 'react';
import { generateUtilityClass, styled, Badge, ListItem, ListItemText, Typography, Tooltip, alpha, Box } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors, getNodeColor, FsIcons } from '../fs-theme';
import { SearchResultHighlight } from '../fs-search/SearchResultHighlight';

import { ConfigOption, FsNode, FsNodeType, useFs } from '@dxs-ts/fs-api';



export const MUI_NAME = 'FsNode';

export interface FsNodeClasses {
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

export type FsNodeClassKey = keyof FsNodeClasses;

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

export const FsNodeRoot = styled('div', {
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
      color: isDarkTheme ? FsColors.dark.text : FsColors.light.textSecondary,
    },

    [`& .${MUI_NAME}-iconConfig`]: {
      fontSize: '14px',
      color: isDarkTheme ? FsColors.dark.text : FsColors.light.text,
    },
  };
});

export function getIcon(node: FsNode) {
  const { isDarkMode } = useFs();

  const getBaseIcon = () => {
    switch (node.type) {
      case 'folder':
        return node.expanded ? <FsIcons.FolderOpen /> : <FsIcons.FolderClosed />;
      case 'article':
        return node.expanded ? <FsIcons.ArticleOutlined /> : <FsIcons.Article />;
      case 'service':
        return node.expanded ? <FsIcons.SettingsOutlined /> : <FsIcons.Settings />;
      case 'dialob':
        return <FsIcons.Form />;
      case 'flow':
        return <FsIcons.Flow />;
      case 'link':
        return <FsIcons.Link />;
      case 'language':
        return <FsIcons.Language />;
      case 'printout':
        return <FsIcons.Print />;
      case 'image':
        return <FsIcons.Image />;
      case 'template':
        return <FsIcons.Pdf />;
      default:
        return <FsIcons.Article />;
    }
  };

  const baseIcon = getBaseIcon();

  if (node.error) {
    return (
      <Box display='flex' alignItems='center' sx={{ color: isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight }}>
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
            backgroundColor: isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight,
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
    backgroundColor: isDarkTheme ? FsColors.dark.surface : FsColors.light.surface,
  },
  ...error && {
    backgroundColor: alpha(isDarkTheme ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight, 0.1),
    //borderBottom: `1px solid ${isDarkTheme ? alpha(FsColors.semantic.dangerDark, 0.3) : alpha(FsColors.semantic.dangerLight, 0.2)}`,
    '&:hover': {
      backgroundColor: isDarkTheme ? alpha(FsColors.semantic.dangerDark, 0.3) : alpha(FsColors.semantic.dangerLight, 0.2),
      //borderBottom: `1px solid ${FsColors.semantic.dangerDark}`
    }
  }
}));

interface StyledListItemTextProps {
  nodeType: FsNodeType;
  nodeName: string;
  description?: string;
  isDarkTheme: boolean;
  error: boolean;
  searchTerm: string;
}

export const StyledListItemText: React.FC<StyledListItemTextProps> = ({
  nodeType, nodeName, description, isDarkTheme, error, searchTerm = ''
}) => {
  return (
    <ListItemText primary={<Typography variant='subtitle2' sx={{
      color: error
        ? (isDarkTheme ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight)
        : getNodeColor(nodeType, isDarkTheme),
      fontWeight: isDarkTheme ? 400 : 500,
    }}
    >
      <SearchResultHighlight text={nodeName} searchTerm={searchTerm} isDarkMode={isDarkTheme} />
      {description && (
        <Typography component='span' variant='caption' sx={{ ml: 1, color: FsColors.dark.textMuted, fontStyle: 'italic' }}>
          - "<SearchResultHighlight text={description} searchTerm={searchTerm} isDarkMode={isDarkTheme} />"
        </Typography>
      )}
    </Typography>
    }
    />
  );
}


export function getIconClassName(node: FsNode, classes: FsNodeClasses) {
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
          <FsIcons.DevMode fontSize='small' className={iconClassName} />
        </Tooltip>
      );
    }
    if (config.assignableMode) {
      icons.push(
        <Tooltip key="assign" title="Assignable Mode" arrow>
          <FsIcons.Assignment fontSize='small' className={iconClassName} />
        </Tooltip>
      );
    }
    if (config.disabledMode) {
      icons.push(
        <Tooltip key="disabled" title="Disabled Mode" arrow>
          <FsIcons.Disabled fontSize='small' className={iconClassName} />
        </Tooltip>
      );
    }
    if (config.anonymousMode) {
      icons.push(
        <Tooltip key="anonymous" title="Anonymous Mode" arrow>
          <FsIcons.Anonymous fontSize='small' className={iconClassName} />
        </Tooltip>
      );
    }
  }

  return icons.length > 0 ? icons : [
    <Tooltip key="default" title="Configuration" arrow>
      <FsIcons.Settings fontSize='small' className={iconClassName} />
    </Tooltip>
  ];
}

