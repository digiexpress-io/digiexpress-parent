import composeClasses from '@mui/utils/composeClasses';
import { generateUtilityClass, styled, Badge, useTheme, ListItem, ListItemText, Typography } from '@mui/material';

import { TreeNode, TreeNodeType } from './mock-tree-data';
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
  iconDialob: string;
  iconFlow: string;
  iconLink: string;
  iconLanguage: string;
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
    iconDialob: ['iconDialob'],
    iconFlow: ['iconFlow'],
    iconLink: ['iconLink'],
    iconLanguage: ['iconLanguage'],
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
      styles.iconDialob,
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
      display: 'flex',
      justifyContent: 'left',
      alignItems: 'center',
      width: '100%'
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
      color: getNodeColor('folder'),
      '& .MuiSvgIcon-root': {
        fontSize: '15px',
      },
    },

    [`& .${MUI_NAME}-iconArticle`]: {
      minWidth: 10,
      marginRight: theme.spacing(1),
      color: getNodeColor('article'),
      '& .MuiSvgIcon-root': {
        fontSize: '15px',
      },
    },

    [`& .${MUI_NAME}-iconService`]: {
      minWidth: 10,
      marginRight: theme.spacing(1),
      color: getNodeColor('service'),
      '& .MuiSvgIcon-root': {
        fontSize: '15px',
      },
    },

    [`& .${MUI_NAME}-iconDialob`]: {
      minWidth: 10,
      marginRight: theme.spacing(1),
      color: getNodeColor('dialob'),
      '& .MuiSvgIcon-root': {
        fontSize: '15px',
      },
    },

    [`& .${MUI_NAME}-iconFlow`]: {
      minWidth: 10,
      marginRight: theme.spacing(1),
      color: getNodeColor('flow'),
      '& .MuiSvgIcon-root': {
        fontSize: '15px',
      },
    },

    [`& .${MUI_NAME}-iconLink`]: {
      minWidth: 10,
      marginRight: theme.spacing(1),
      color: getNodeColor('link'),
      '& .MuiSvgIcon-root': {
        fontSize: '15px',
      },
    },

    [`& .${MUI_NAME}-iconLanguage`]: {
      minWidth: 10,
      marginRight: theme.spacing(1),
      color: getNodeColor('language'),
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

export const StyledListItem = styled(ListItem, {
  name: MUI_NAME,
  slot: 'StyledListItem',
})<{ level: number }>(({ theme, level }) => ({

  paddingLeft: theme.spacing(level * 1.2),
  cursor: 'pointer',
  '&:hover': {
    backgroundColor: '#2d2d30',
  },
}));

interface StyledListItemTextProps {
  nodeType: TreeNodeType;
  nodeName: string;
  description?: string;
}

export const StyledListItemText: React.FC<StyledListItemTextProps> = ({ nodeType, nodeName, description }) => {
  return (
    <ListItemText
      primary={
        <Typography variant='subtitle2' sx={{ color: getNodeColor(nodeType), fontWeight: nodeType === 'folder' ? 500 : 400 }}>
          {nodeName}
          {description && (
            <Typography component='span' variant='caption' sx={{ ml: 1, color: '#6a9955', fontStyle: 'italic' }}>
              - "{description}"
            </Typography>
          )}
        </Typography>
      }
    />
  );
};

export function getIconClassName(node: TreeNode, classes: EveliTreeClasses) {
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
    default:
      return classes.iconFolder;
  }
};

export function getNodeColor(nodeType: TreeNodeType) {
  switch (nodeType) {
    case 'folder':
      return '#e8e5e5'; // Gray
    case 'article':
      return '#dcdcaa'; // Yellow for articles
    case 'service':
      return '#4ec9b0'; // Teal for services
    case 'dialob':
      return '#9cdcfe'; // Light blue for dialob forms
    case 'flow':
      return '#c586c0'; // Purple for flows
    case 'link':
      return '#98d982'; // Bright green for links
    case 'language':
      return '#ce9178'; // Orange for languages
    default:
      return '#cccccc';
  }
};

