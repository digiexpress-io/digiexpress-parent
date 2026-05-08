import React from 'react';
import { Badge, Box, ListItemText, Typography } from '@mui/material';

import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { useFsNav } from '@dxs-ts/fs-nav';
import { useFsTheme } from '../fs-theme';

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
      {ownerState.options.length === 0 && (<Align><FsIcon small icon={FsIcons.Settings} className={classes.iconConfig} tooltip='Configuration' key='configuration' /></Align>)}
      {options.map((type) => (<Align key={type}><ConfigIcon type={type} className={classes.iconConfig} /></Align>))}
    </Box>
  )
}


function ConfigIcon(props: { type: Fs.ConfigOption, className: string }) {
  const { className, type } = props;

  switch (type) {
    case 'devMode': return <FsIcon small icon={FsIcons.DevMode} className={className} tooltip='Development Mode' key='development' />;
    case 'assignableMode': return <FsIcon small icon={FsIcons.Assignment} className={className} tooltip='Assignable Mode' key='assignable' />
    case 'disabledMode': return <FsIcon small icon={FsIcons.Disabled} className={className} tooltip='Disabled Mode' key='disabled' />
    case 'anonymousMode': return <FsIcon small icon={FsIcons.Anonymous} className={className} tooltip='Anonymous Mode' key='anonymous' />
  }
}

export const DirentDecorator = (props: { dirent: Fs.DirentBase, children: React.ReactNode }) => {
  const { isDarkMode } = useFsTheme();
  const { getDirent } = useFsDirent();
  const { dirent, children } = props;
  const direntEntry = getDirent(dirent.id);

  if ((direntEntry?.props?.errors.length ?? 0) > 0) {
    return (
      <Box display='flex' alignItems='center' sx={{ color: isDarkMode ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight }}>
        {children}
      </Box>
    );
  }
  if (direntEntry?.props?.reference) {
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


export const DirentIcon = (props: { dirent: Fs.DirentBase }) => {
  const { dirent } = props;
  const { isExpanded } = useFsNav();
  const expanded = isExpanded(dirent.id);
  switch (dirent.type) {
    case 'FOLDER':
      return expanded ? <FsIcons.FolderOpen /> : <FsIcons.FolderClosed />;
    case 'ARTICLE':
      return <FsIcons.Article />;
    case 'ARTICLE_WORKFLOW':
      return expanded ? <FsIcons.SettingsOutlined /> : <FsIcons.Settings />;
    case 'DIALOB_FORM':
      return <FsIcons.Form />;
    case 'FLOW':
      return <FsIcons.Flow />;
    case 'ARTICLE_LINK':
      return <FsIcons.Link />;
    case 'LOCALE':
      return <FsIcons.Language />;
    case 'PRINTOUT':
      return <FsIcons.Print />;
    case 'PRINTOUT_RESOURCE':
      return <FsIcons.Image />;
    case 'PRINTOUT_PAGE':
      return <FsIcons.Pdf />;
    case 'UNKNOWN':
      return <FsIcons.Phone />;
    case 'ARTICLE_PAGE':
      return <FsIcons.Page />;
    default:
      return <FsIcons.Article />;
  }
}

interface FsDirentNameProps {
  dirent: Fs.DirentBase;
  isDarkTheme: boolean;
  error: boolean;
  searchTerm: string;
}

export const FsDirentName: React.FC<FsDirentNameProps> = (props) => {
  const { getDirent, getParentDirent, getExtension } = useFsDirent();
  const classes = useUtilityClasses(props.isDarkTheme);
  const description = getDirent(props.dirent.id)?.props?.description;
  const displayName = props.dirent.type === 'ARTICLE' ? (
    getParentDirent(props.dirent.id)?.name ?? props.dirent.name) : props.dirent.name;
  const extension = getExtension(props.dirent.type);

  return (
    <ListItemText className={classes.direntName} primary={<Typography variant='subtitle2'
      sx={{
        color: props.error ? (props.isDarkTheme ? FsColors.semantic.dangerDark : FsColors.semantic.dangerLight)
          :
          getDirentColor(props.dirent.type, props.isDarkTheme),
        fontWeight: props.isDarkTheme ? 400 : 500,
      }}
    >
      <SearchResultHighlight text={displayName} searchTerm={props.searchTerm} isDarkMode={props.isDarkTheme} />
      {extension && <Typography component='span' variant='inherit'><SearchResultHighlight text={extension} searchTerm={props.searchTerm} isDarkMode={props.isDarkTheme} /></Typography>}
      {description && (
        <Typography component='span' variant='caption' sx={{ ml: 1, color: FsColors.dark.textMuted, fontStyle: 'italic' }}>
          - "<SearchResultHighlight text={description} searchTerm={props.searchTerm} isDarkMode={props.isDarkTheme} />"
        </Typography>
      )}
    </Typography>
    }
    />
  );
}


