import React from 'react';
import { GFormGroupProps } from './g-form-group-types';
import { Accordion, AccordionDetails, AccordionSummary, Typography } from '@mui/material';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import { GFormGroupLabel, useUtilityClasses } from './useThemeInfra';


export const GFormGroupCollapseble: React.FC<{ ownerState: GFormGroupProps, className: string, children: React.ReactNode }> = ({ ownerState }) => {
  const { collapsible, children, slots } = ownerState;

  if (collapsible !== true) {
    return (<>{children}</>)
  }
  const Label = slots?.label ?? GFormGroupLabel as any;
  const classes = useUtilityClasses(ownerState);

  return (
    <Accordion>
      <AccordionSummary expandIcon={<ExpandMoreIcon />}>
        {ownerState.label && (
          <Label ownerState={ownerState} className={classes.label}>
            <Typography>
              {ownerState.label}
            </Typography>
          </Label>
        )}
      </AccordionSummary>
      <AccordionDetails>
        {children}
      </AccordionDetails>
    </Accordion>
  )
}
