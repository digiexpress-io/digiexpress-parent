import React from 'react'
import { useThemeProps, Typography, Divider, IconButton } from '@mui/material'
import AddIcon from '@mui/icons-material/Add';
import { MUI_NAME, useUtilityClasses, GInputGroupRoot, GInputGroupLabel, GInputGroupBody } from './useUtilityClasses';


export interface GInputGroupProps {
  id: string;
  label: string | undefined;
  description: string | undefined;
  children: React.ReactNode;
  onAddRow: (id: string) => void;

  component?: React.ElementType<GInputGroupProps>;
  slots?: {
    label: React.ElementType<GInputGroupProps>;
    body: React.ElementType<GInputGroupProps>;
  };
}


export const GInputGroup: React.FC<GInputGroupProps> = (initProps) => {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const { id, children } = props;
  const ownerState = { ...props };
  const classes = useUtilityClasses(id);

  const Label = props.slots?.label ?? GInputGroupLabel;
  const Body = props.slots?.body ?? GInputGroupBody;

  function handleAddRow() {
    props.onAddRow(props.id);
  }

  return (
    <GInputGroupRoot className={classes.root} ownerState={ownerState} as={props.component}>


      <Label {...props} className={classes.label}>
        <div>
          <Typography>{props.label}</Typography>
        </div>
        <Divider flexItem />
        <IconButton color='primary' onClick={handleAddRow}>
          <AddIcon />
        </IconButton>
      </Label>
      
      <Body {...props} className={classes.body}>
        {children}
      </Body>

    </GInputGroupRoot>);
}
