import React from 'react'

import { useThemeProps, Typography, Divider, IconButton } from '@mui/material'
import { Delete as DeleteIcon } from '@mui/icons-material';

import { MUI_NAME, useUtilityClasses, GInputGroupRowRoot, GInputGroupRowLabel, GInputGroupRowBody } from './useUtilityClasses';


export interface GInputGroupRowProps {
  id: string;
  label: string | undefined;
  description: string | undefined;
  disabled: boolean;
  children: React.ReactNode;
  onDelete: (id: string) => void
  columns?: string | undefined; // numerical string
  readOnly?: boolean;

  order: number;
  total: number;

  component?: React.ElementType<GInputGroupRowProps>;

  slots?: {
    label: React.ElementType<GInputGroupRowProps>;
    body: React.ElementType<GInputGroupRowProps>;
  };
}


export const GInputGroupRow: React.FC<GInputGroupRowProps> = (initProps) => {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const { id, label, children } = props;
  const ownerState = { ...props };
  const classes = useUtilityClasses(id);

  const Label = props.slots?.label ?? GInputGroupRowLabel;
  const Body = props.slots?.body ?? GInputGroupRowBody;

  function handleDelete() {
    props.onDelete(props.id);
  }



  return (
    <GInputGroupRowRoot className={classes.root} ownerState={ownerState} as={props.component}>

      {props.label && (
        <Label {...props} className={classes.label}>
          <div>
            <Typography>{props.order + 1}</Typography>
          </div>
          <Divider flexItem />
          <IconButton disabled={props.disabled} color='error' onClick={handleDelete}>
            <DeleteIcon />
          </IconButton>
        </Label>)
      }
      <Body {...props} disabled={props.disabled} className={classes.body}>
        {children}
      </Body>
    </GInputGroupRowRoot>);
}
