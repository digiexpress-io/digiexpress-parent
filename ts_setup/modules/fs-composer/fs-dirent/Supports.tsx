import React from 'react';
import { Badge, Box, ListItemText, Typography } from '@mui/material';

import { Fs, useFsDirent } from '@dxs-ts/fs-api';

import { useUtilityClasses } from './useUtilityClasses';
import { FsIcons, FsIcon, FsColors } from '../fs-theme';

import { OwnerState } from './useOwnerState';
import { SearchResultHighlight } from '../fs-search';
import { createWidget } from '../fs-factory';




export const ConfigOptionIcons: React.FC<{ ownerState: OwnerState }> = ({ ownerState }) => {
  const classes = useUtilityClasses(ownerState.dirent);
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
    case 'DEV_MODE': return <FsIcon small icon={FsIcons.DevMode} className={className} tooltip='Development Mode' key='development' />;
    case 'ASSIGNABLE_MODE': return <FsIcon small icon={FsIcons.Assignment} className={className} tooltip='Assignable Mode' key='assignable' />
    case 'DISABLED_MODE': return <FsIcon small icon={FsIcons.Disabled} className={className} tooltip='Disabled Mode' key='disabled' />
    case 'ANONYMOUS_MODE': return <FsIcon small icon={FsIcons.Anonymous} className={className} tooltip='Anonymous Mode' key='anonymous' />
  }
}

export const DirentDecorator = (props: { dirent: Fs.DirentBase, children: React.ReactNode }) => {
  const { getDirent } = useFsDirent();
  const { dirent, children } = props;
  const direntEntry = getDirent(dirent.id);

  if ((direntEntry?.props?.errors.length ?? 0) > 0) {
    return (
      <Box display='flex' alignItems='center' sx={{ color: FsColors.semantic.danger }}>
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
            backgroundColor: FsColors.semantic.danger,
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


interface FsDirentNameProps {
  dirent: Fs.DirentBase;
  error: boolean;
  searchTerm: string;
}

export const FsDirentName: React.FC<FsDirentNameProps> = (props) => {
  const { getParentDirent, getDirentName } = useFsDirent();
  const classes = useUtilityClasses(props.dirent);
  let displayName: string;

  if (props.dirent.type === 'ARTICLE') {
    displayName = getParentDirent(props.dirent.id)?.name ?? props.dirent.name;
  } else if (props.dirent.type === 'PRINTOUT_PAGE') {
    displayName = getDirentName(props.dirent.id) ?? props.dirent.name;
  } else {
    displayName = props.dirent.name;
  }
  const widget = createWidget(props.dirent);


  const fullDisplayName = widget.meta.extension ? displayName + widget.meta.extension : displayName;


  return (
    <ListItemText className={classes.direntName} primary={<Typography variant='subtitle2'
      sx={{
        color: props.error ? FsColors.semantic.danger : widget.colors.dirent,
        fontWeight: 500,
      }}
    >
      <SearchResultHighlight text={fullDisplayName} searchTerm={props.searchTerm} />
    </Typography>
    }
    />
  );
}


