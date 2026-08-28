import React from 'react';
import { Divider, Typography } from '@mui/material';
import { useThemeInfra, GFormGroupRoot } from './useThemeInfra';
import { GFormGroupProps } from './g-form-group-types';
import { GInputAdornment } from '../g-input-adornment';


export const GFormGroup: React.FC<GFormGroupProps> = (initProps) => {
  const { ownerState, classes, slots } = useThemeInfra(initProps);

  const isLabelDisplayedHere = !ownerState.collapsible && ownerState.label;

  return (
    <GFormGroupRoot ownerState={ownerState} as={ownerState.component} className={classes.root}>
      <slots.collapsible ownerState={ownerState} className={classes.collapsible}>
        {isLabelDisplayedHere && (
          <slots.label ownerState={ownerState} className={classes.label}>
            <div className={classes.labelContent}>
              <Typography>
                {ownerState.label}
              </Typography>
              <GInputAdornment id={ownerState.id} title={ownerState.label} disabled={ownerState.disabled} children={ownerState.description} />
            </div>
            <Divider flexItem />
          </slots.label>
        )}

        {!ownerState.label && (<div className={classes.label}><Divider /></div>)}
        <slots.body ownerState={ownerState} className={classes.body}>
          {ownerState.children}
        </slots.body>
      </slots.collapsible>
    </GFormGroupRoot>

  )
}

