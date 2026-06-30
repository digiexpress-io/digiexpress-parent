import React from 'react';
import { Badge, Box, ListItemText, Typography } from '@mui/material';
import { useIntl } from 'react-intl';

import { Fs, useFsDirent } from '@dxs-ts/fs-api';

import { useUtilityClasses } from './useUtilityClasses';
import { FsIcons, FsIcon, FsColors } from '../fs-theme';

import { OwnerState } from './useOwnerState';
import { SearchResultHighlight } from '../fs-search';
import { createWidget } from '../fs-factory';


export const ConfigOptionIcons: React.FC<{ ownerState: OwnerState }> = ({ ownerState }) => {
  const classes = useUtilityClasses(ownerState.dirent);
  const intl = useIntl();
  const Align = React.useCallback((props: { children: React.ReactNode }) => <Box display='flex' alignItems='center'>{props.children}</Box>, []);

  const isLocaleDisabled = ownerState.dirent.type === 'LOCALE' &&
    (ownerState.dirent.props as Fs.LanguageProps)?.enabled === false;

  if (!ownerState.configOptions && !isLocaleDisabled) {
    return (<></>)
  }

  const { options } = ownerState;
  return (
    <Box sx={{ marginLeft: 'auto', paddingRight: 1, display: 'flex', gap: 0.5 }}>
      {isLocaleDisabled && (
        <Align><FsIcon small icon={FsIcons.Disabled} className={classes.iconConfig} tooltip={intl.formatMessage({ id: 'fs.dirent.language.disabled' })} /></Align>
      )}
      {!isLocaleDisabled && options.length === 0 && (<Align><FsIcon small icon={FsIcons.Settings} className={classes.iconConfig} tooltip='Configuration' key='configuration' /></Align>)}
      {options.map((type) => (<Align key={type}><ConfigIcon type={type} className={classes.iconConfig} /></Align>))}
    </Box>
  )
}


const ConfigIcon: React.FC<{ type: Fs.ConfigOption, className: string }> = ({ type, className }) => {
  const intl = useIntl();
  const tooltip = intl.formatMessage({ id: `fs.dirent.configOption.${type}` });

  switch (type) {
    case 'DEV_MODE': return <FsIcon small icon={FsIcons.DevMode} className={className} tooltip={tooltip} />;
    case 'ASSIGNABLE_MODE': return <FsIcon small icon={FsIcons.Assignment} className={className} tooltip={tooltip} />;
    case 'DISABLED_MODE': return <FsIcon small icon={FsIcons.Disabled} className={className} tooltip={tooltip} />;
    case 'ANONYMOUS_MODE': return <FsIcon small icon={FsIcons.Anonymous} className={className} tooltip={tooltip} />;
    case 'AUTH_ONLY_MODE': return <FsIcon small icon={FsIcons.Locked} className={className} tooltip={tooltip} />;
    case 'IN_HOUSE_MODE': return <FsIcon small icon={FsIcons.InHouse} className={className} tooltip={tooltip} />;
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


