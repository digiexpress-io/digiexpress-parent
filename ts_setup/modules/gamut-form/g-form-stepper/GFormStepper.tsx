import React from 'react';
import { Typography, Box, useTheme, lighten } from '@mui/material';

import CircularProgress from '@mui/material/CircularProgress';

import { FormattedMessage } from 'react-intl';
import { useThemeInfra, GFormStepperRoot } from './useThemeInfra';



export interface GFormStepperClasses {
  root: string;
}
export type GFormStepperClassKey = keyof GFormStepperClasses;

export interface GFormStepperProps {
  id: string;
  pageNumber: number; // starts from 1..n
  totalPages: number;
  component?: React.ElementType<GFormStepperProps>;
}

export const GFormStepper: React.FC<GFormStepperProps> = (initProps) => {
  const { ownerState, classes, props } = useThemeInfra(initProps);
  const { progress, activeStep, pages } = ownerState;

  return (
    <GFormStepperRoot ownerState={ownerState} className={classes.root} as={props.component} >
      <CircularProgress className={classes.progress} size={80} variant='determinate' value={progress} />
      <Box className={classes.label}>
        <Typography variant='caption' component='div' color='text.secondary'>
          <FormattedMessage id='gamut.forms.page.stepper' values={{ start: activeStep, end: pages }}/>
        </Typography>
      </Box>
    </GFormStepperRoot>
  )
}