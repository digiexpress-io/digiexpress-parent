
import React from 'react';
import { IconButton, Popover, Typography } from '@mui/material';
import { HelpOutline as HelpOutlineIcon } from '@mui/icons-material';
import { GMarkdown } from '@dxs-ts/gamut-md';
import { GFormNoteValidationRoot, StyledPopover } from './useUtilityClasses';
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
      <GFormNoteValidationRoot ownerState={ownerState} as={ownerState.component} className={classes.root} severity={ownerState.style}
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

      <StyledPopover
        open={!!anchorEl}
        anchorEl={anchorEl}
        onClose={handleClose}
        anchorOrigin={{
          vertical: 'bottom',
          horizontal: 'left',
        }}
      >
        <GMarkdown
          overrides={{
            p: ({ children }) => (
              <Typography sx={{ marginBottom: '0px !important' }} >
                {children}
              </Typography>
            ),
          }}
        >
          {props.description}
        </GMarkdown >
      </StyledPopover>
    </>
  )
}