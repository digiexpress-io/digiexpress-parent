
import React from 'react';
import { IconButton, Popover, Typography } from '@mui/material';
import HelpOutlineIcon from '@mui/icons-material/HelpOutline';
import { GMarkdown } from '@dxs-ts/gamut-md';
import { GFormNoteValidationRoot } from './useUtilityClasses';
import { useThemeInfra } from './useUtilityClasses';

export interface GFormNoteValidationClasses {
  root: string;
}
export type GFormNoteValidationClassKey = keyof GFormNoteValidationClasses;

export interface GFormNoteValidationProps {
  id: string;
  label: string | undefined;
  style: 'error' | 'success' | 'warning' | 'info' | undefined;
  description: string | undefined;
  component?: React.ElementType<GFormNoteValidationProps>;
}


export const GFormNoteValidation: React.FC<GFormNoteValidationProps> = (initProps) => {
  const { ownerState, classes, props } = useThemeInfra(initProps);
  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);

  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  return (
    <>
      <GFormNoteValidationRoot ownerState={ownerState} as={ownerState.component} className={classes.root} severity={props.style}
        icon={
          props.description ? (
            <IconButton onClick={handleClick}>
              <HelpOutlineIcon />
            </IconButton>
          ) : (
            <></>
          )
        }>
        <GMarkdown>{props.label}</GMarkdown>
      </GFormNoteValidationRoot>

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