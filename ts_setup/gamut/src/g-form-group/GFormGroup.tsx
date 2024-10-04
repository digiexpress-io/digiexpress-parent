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
  component?: React.ElementType<GFormGroupProps>;
  slots?: {
    label: React.ElementType<GFormGroupProps>;
    body: React.ElementType<GFormGroupProps>;
  };
}

export const GFormGroup: React.FC<GFormGroupProps> = (initProps) => {
  const { ownerState, classes, props, slots } = useThemeInfra(initProps);

  return (
    <GFormGroupRoot ownerState={ownerState} as={ownerState.component} className={classes.root}>
    {props.label && (
      <slots.label {...props} className={classes.label}>
        <div>
        <Typography>
          {props.label}
        </Typography>
        </div>
        <Divider flexItem />
      </slots.label>
      )
    }

      {!props.label && (<div className={classes.label}><Divider /></div>)}

      <slots.body {...props} className={classes.body}>
        {props.children}
      </slots.body>
      
    </GFormGroupRoot>)
}

