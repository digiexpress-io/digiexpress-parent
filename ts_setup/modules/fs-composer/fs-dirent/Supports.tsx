import React from 'react';
import { Badge, Box, ListItemText, Typography } from '@mui/material';

import { FsDirentConfigOption, FsDirent, useFsNav } from '@dxs-ts/fs-api';

import { useUtilityClasses } from './useUtilityClasses';
import { FsIcons, FsIcon, FsColors, getDirentColor } from '../fs-theme';

import { OwnerState } from './useOwnerState';
import { SearchResultHighlight } from '../fs-search';




export const ConfigOptionIcons: React.FC<{ ownerState: OwnerState }> = ({ ownerState }) => {
  const classes = useUtilityClasses(ownerState.isDarkMode);
  const Align = React.useCallback((props: { children: React.ReactNode }) => <Box display='flex' alignItems='center'>{props.children}</Box>, []);

  if (!ownerState.configOptions) {
    return (<></>)
  }
  const { options } = ownerState;
  return (
    <Box sx={{ marginLeft: 'auto', paddingRight: 1, display: 'flex', gap: 0.5 }}>
      {ownerState.options.length === 0 && (<Align><ConfigIcon className={classes.iconConfig} /></Align>)}
      {options.map((type) => (<Align key={type}><ConfigIcon type={type} className={classes.iconConfig} /></Align>))}
    </Box>
  )
}


function ConfigIcon(props: { type?: keyof FsDirentConfigOption, className: string }) {
  const { className, type } = props;

  if (type == undefined) {
    return <FsIcon small icon={FsIcons.Settings} className={className} tooltip='Configuration' key='configuration' />
  }

  switch (type) {
    case 'devMode': return <FsIcon small icon={FsIcons.DevMode} className={className} tooltip='Development Mode' key='development' />;
    case 'assignableMode': return <FsIcon small icon={FsIcons.Assignment} className={className} tooltip='Assignable Mode' key='assignable' />
    case 'disabledMode': return <FsIcon small icon={FsIcons.Disabled} className={className} tooltip='Disabled Mode' key='disabled' />
    case 'anonymousMode': return <FsIcon small icon={FsIcons.Anonymous} className={className} tooltip='Anonymous Mode' key='anonymous' />
  }
}

export const DirentDecorator = (props: { dirent: FsDirent, children: React.ReactNode }) => {
  const { isDarkMode } = useFsNav();
  const { dirent, children } = props;

  if (dirent.errors && dirent.errors.length > 0) {
    return (
      <Box display='flex' alignItems='center' sx={{ color: isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight }}>
        {children}
      </Box>
    );
  }
  if (dirent.reference) {
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
        {children}
      </Badge>
    );
  }

  return <>{children}</>;
}


export const DirentIcon = (props: { dirent: FsDirent }) => {
  const { dirent } = props;
  switch (dirent.type) {
    case 'folder':
      return dirent.expanded ? <FsIcons.FolderOpen /> : <FsIcons.FolderClosed />;
    case 'article':
      return dirent.expanded ? <FsIcons.ArticleOutlined /> : <FsIcons.Article />;
    case 'service':
      return dirent.expanded ? <FsIcons.SettingsOutlined /> : <FsIcons.Settings />;
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
}

interface FsDirentNameProps {
  dirent: FsDirent;
  isDarkTheme: boolean;
  error: boolean;
  searchTerm: string;
}

export const FsDirentName: React.FC<FsDirentNameProps> = (props) => {
  return (
    <ListItemText primary={<Typography variant='subtitle2'
      sx={{
        color: props.error ? (props.isDarkTheme ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight)
          :
          getDirentColor(props.dirent.type, props.isDarkTheme),
        fontWeight: props.isDarkTheme ? 400 : 500,
      }}
    >
      <SearchResultHighlight text={props.dirent.name} searchTerm={props.searchTerm} isDarkMode={props.isDarkTheme} />
      {props.dirent.description && (
        <Typography component='span' variant='caption' sx={{ ml: 1, color: FsColors.dark.textMuted, fontStyle: 'italic' }}>
          - "<SearchResultHighlight text={props.dirent.description} searchTerm={props.searchTerm} isDarkMode={props.isDarkTheme} />"
        </Typography>
      )}
    </Typography>
    }
    />
  );
}
