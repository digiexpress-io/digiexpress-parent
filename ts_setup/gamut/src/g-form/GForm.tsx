import React from 'react'
import { GFormIterator } from './GFormIterator'
import { FormProvider, useFormTip, DialobApi } from '../api-dialob';

import { useUtilityClasses, GFormRoot, GFormProgress } from './useUtilityClasses';


export interface GFormProps {
  children: string | undefined; // dialob sessionId
  variant: string; // form technical name for overrides
  onAfterComplete: () => void;
}

export interface OwnerState {
  variant: string | undefined;
  questionnaire: DialobApi.ActionItem | undefined;
}

export const GForm: React.FC<GFormProps> = (props) => {
  if (!props.children) {
    return null;
  }
  return (<FormProvider variant={props.variant} id={props.children} onAfterComplete={props.onAfterComplete}><GFormTip {...props} /></FormProvider>);
}

// Internal component to access the provider
const GFormTip: React.FC<GFormProps> = (props) => {
  if (!props.children) {
    return null;
  }
  const tip = useFormTip();

  const ownerState: OwnerState = {
    variant: props.variant,
    questionnaire: tip
  }
  const classes = useUtilityClasses(ownerState);

  return (
    <GFormRoot ownerState={ownerState} className={classes.root}>
      {tip ? <GFormIterator /> : <GFormProgress className={classes.progress} />}
    </GFormRoot>);
}



