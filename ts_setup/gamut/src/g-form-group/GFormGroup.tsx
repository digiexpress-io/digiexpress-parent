import React from 'react';
import { Divider, Typography } from '@mui/material';
import { useThemeInfra, GFormGroupRoot } from './useThemeInfra';


export interface GFormGroupClasses {
  root: string;
}
export type GFormGroupClassKey = keyof GFormGroupClasses;

export interface GFormGroupProps {
  id: string;
  label: string | undefined;
  description: string | undefined;
  children: React.ReactNode;
  columns?: string | undefined; // numerical string

  /**
  - Starts from 1 (Page)
  - Indicates how deep the item is nested
   */
  level?: number | undefined,

  /**
  - Styles for parent and child group items, resembling MUI Paper, which include a border, elevation, and padding/margins   
  - For every level of nesting of a group within other groups, it will have additional margins calculated from its level property
   */
  border?: boolean | undefined;

  component?: React.ElementType<GFormGroupProps>;
  slots?: {
    label: React.ElementType<{ ownerState: GFormGroupProps, className: string, children: React.ReactNode }>;
    body: React.ElementType<{ ownerState: GFormGroupProps, className: string, children: React.ReactNode }>;
  };
}

export const GFormGroup: React.FC<GFormGroupProps> = (initProps) => {
  const { ownerState, classes, slots } = useThemeInfra(initProps);

  return (
    <GFormGroupRoot ownerState={ownerState} as={ownerState.component} className={classes.root}>

      {ownerState.label && (
        <slots.label ownerState={ownerState} className={classes.label}>
          <div>
            <Typography>
              {ownerState.label}
            </Typography>
          </div>
        <Divider flexItem />
      </slots.label>
      )
    }

      {!ownerState.label && (<div className={classes.label}><Divider /></div>)}
      <slots.body ownerState={ownerState} className={classes.body}>
        {ownerState.children}
      </slots.body>
      
    </GFormGroupRoot>

  )
}

