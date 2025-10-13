import React from 'react'
import { useThemeProps, Typography, Divider, IconButton } from '@mui/material'
import { Add as AddIcon } from '@mui/icons-material';
import { MUI_NAME, useUtilityClasses, GInputGroupRoot, GInputGroupLabel, GInputGroupBody } from './useUtilityClasses';


export interface GInputGroupProps {
  id: string;
  label: string | undefined;
  description: string | undefined;
  disabled: boolean;
  children: React.ReactNode;
  onAddRow: (id: string) => void;

  /**
- Styles resembling MUI Paper, which include a border, elevation, and padding/margins   
- Set in Composer properties: border = true  
 */
  border?: boolean | undefined;

  component?: React.ElementType<GInputGroupProps>;
  slots?: {
    label: React.ElementType<{ ownerState: GInputGroupProps, className: string }>;
    body: React.ElementType<{ ownerState: GInputGroupProps, className: string }>;
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

  const Label: React.ElementType<{ ownerState: GInputGroupProps, className: string }> = props.slots?.label ?? GInputGroupLabel;
  const Body: React.ElementType<{ ownerState: GInputGroupProps, className: string }> = props.slots?.body ?? GInputGroupBody;

  function handleAddRow() {
    props.onAddRow(props.id);
  }

  return (
    <GInputGroupRoot className={classes.root} ownerState={ownerState} as={props.component}>
      <Label ownerState={props} className={classes.label}>
        <div>
          <Typography>{props.label}</Typography>
        </div>
        <Divider flexItem />
        <IconButton disabled={props.disabled} color='primary' onClick={handleAddRow}>
          <AddIcon />
        </IconButton>
      </Label>
      <Body ownerState={props} className={classes.body}>
        {children}
      </Body>
    </GInputGroupRoot>);
}
