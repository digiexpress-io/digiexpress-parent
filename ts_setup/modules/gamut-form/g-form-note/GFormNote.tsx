import React from 'react';
import { IconButton, Popover, Typography } from '@mui/material';
import HelpOutlineIcon from '@mui/icons-material/HelpOutline';

import { useThemeInfra, GFormNoteRoot } from './useThemeInfra';
import { GMarkdown } from '@dxs-ts/gamut-md'

export interface GFormNoteClasses {
  root: string;
}
export type GFormNoteClassKey = keyof GFormNoteClasses;

export interface GFormNoteProps {
  id: string;
  label: string | undefined;
  style: 'error' | 'success' | 'warning' | 'info' | undefined;
  description: string | undefined;
  component?: React.ElementType<GFormNoteProps>;
}

export const GFormNote: React.FC<GFormNoteProps> = (initProps) => {
  const { ownerState, classes, props } = useThemeInfra(initProps);
  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);

  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };


  return (<>
    <GFormNoteRoot ownerState={ownerState} as={ownerState.component} className={classes.root} severity={ownerState.style}
      icon={
        props.description ? (
          <IconButton onClick={handleClick}>
            <HelpOutlineIcon />
          </IconButton>
        ) : (
          <></>
        )}
    >
      <GMarkdown>{props.label}</GMarkdown>
    </GFormNoteRoot>


    <Popover
      open={!!anchorEl}
      anchorEl={anchorEl}
      onClose={handleClose}
      sx={{
        maxWidth: '60vw',
        textWrap: 'wrap',
        wordBreak: 'break-word',
        '.MuiPaper-root': {
          padding: 2
        }
      }}
      anchorOrigin={{
        vertical: 'bottom',
        horizontal: 'left',
      }}
    >
      <Typography>
        {props.description}
      </Typography>
    </Popover>
  </>
  )
}
