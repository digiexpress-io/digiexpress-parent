import React from 'react'
import { GFormIterator } from './GFormIterator'
import { FormProvider, useFormTip, DialobApi } from '../api-dialob';

import { useUtilityClasses, GFormRoot, GFormProgress, MUI_NAME } from './useUtilityClasses';
import { CircularProgress, useThemeProps } from '@mui/material';
import { GOverridableComponent } from '../g-override';


export interface GFormProps {
  executionId: string;
  children: string | undefined; // dialob sessionId
  variant: string; // form technical name for overrides
  onAfterComplete: () => void;
  component?: GOverridableComponent<GFormProps>;
}

export interface OwnerState {
  variant: string | undefined;
  questionnaire: DialobApi.ActionItem | undefined;
}

export const GForm: React.FC<GFormProps> = (initProps) => {
  const themeProps = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  
  if(themeProps.component) {
    const Root = themeProps.component;
    return <Root {...initProps} ownerState={themeProps} className=''/>
  }

  if (!themeProps.children) {
    return null;
  }
  return (<FormProvider variant={themeProps.variant} executionId={themeProps.executionId} id={themeProps.children} onAfterComplete={themeProps.onAfterComplete}><GFormTip {...themeProps} /></FormProvider>);
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
      {tip ? <GFormIterator /> : <GFormProgress className={classes.progress}><CircularProgress /></GFormProgress>}
    </GFormRoot>);
}



