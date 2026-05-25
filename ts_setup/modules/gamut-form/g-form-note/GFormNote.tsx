import React from 'react';
import { IconButton, Popover, Typography } from '@mui/material';
import { HelpOutline as HelpOutlineIcon } from '@mui/icons-material';

import { GMarkdown } from '@dxs-ts/gamut-md';
import { DialobApi } from '@dxs-ts/gamut-api';

import { useThemeInfra, GFormNoteRoot } from './useThemeInfra';

import { GInputBase, GInputBaseProps } from '../g-input-base';
import { GInputLabel } from '../g-input-label';


export interface GFormNoteClasses {
  root: string;
}
export type GFormNoteClassKey = keyof GFormNoteClasses;

export interface GFormNoteProps {
  id: string;
  label: string | undefined;
  style: 'error' | 'success' | 'warning' | 'info' | undefined;
  description: string | undefined;
  labelPosition: DialobApi.ControlLabelPosition;
  component?: React.ElementType<GFormNoteProps>;
}

export const GFormNote: React.FC<GFormNoteProps> = (initProps) => {
  const { ownerState, classes, props } = useThemeInfra(initProps);
  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);

  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  }

  const handleClose = () => {
    setAnchorEl(null);
  }

  const { id, label, description, labelPosition } = props;

  // delegate component where the real note is. Based on label, it's wrapped into input or rendered as-is.
  const Input = React.useCallback(function DelegateAllToActualNote() {
    return (<GFormNoteRoot ownerState={ownerState} as={ownerState.component} className={classes.root} severity={ownerState.style}
      icon={
        description ? (
          <IconButton onClick={handleClick}>
            <HelpOutlineIcon />
          </IconButton>
        ) : (
          <></>
        )}
    >
      <GMarkdown>{label}</GMarkdown>
    </GFormNoteRoot>)
  }, [label, description, ownerState, classes])

  // this is required for specific row-group configuration (when the label is at the top) to reserve the space for the label text.
  const slots: GInputBaseProps<GFormNoteProps> = {
    id,
    slots: {
      error: () => <></>,
      label: GInputLabel,
      input: Input,
      adornment: undefined
    },
    slotProps: {
      error: { id, errors: [] },
      input: { ...ownerState, name: id },
      label: { id, children: '', labelPosition, required: false, errors: [] },
      adornment: { id, children: props.description, title: undefined, disabled: false }
    }
  }

  return (<>
    {labelPosition === 'label-left' ?
      <Input /> :
      <GInputBase id={props.id} slots={slots.slots} slotProps={slots.slotProps} />}

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
