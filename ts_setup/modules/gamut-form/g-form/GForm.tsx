import React from 'react'
import { GFormIterator } from './GFormIterator'
import { FormProvider, useFormTip, DialobApi } from '@dxs-ts/gamut-api';

import { useUtilityClasses, GFormRoot, GFormProgress, MUI_NAME } from './useUtilityClasses';
import { CircularProgress, useThemeProps } from '@mui/material';
import { GOverridableComponent } from '@dxs-ts/gamut-api';


export interface GFormProps {
  executionId: string;
  debugFormId?: string;
  variant: string; // form technical name for overrides
  onAfterComplete: () => void;
  onCancel: () => void;
  component?: GOverridableComponent<GFormProps>;
  formUnavailable: React.ElementType
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

  return (<FormProvider 
    onCancel={themeProps.onCancel}
    debugFormId={themeProps.debugFormId} 
    variant={themeProps.variant} 
    executionId={themeProps.executionId} 
    onAfterComplete={themeProps.onAfterComplete} 
    formUnavailable={themeProps.formUnavailable}>

      <GFormTip {...themeProps} />
    </FormProvider>);
}

// Internal component to access the provider
export const GFormTip: React.FC<GFormProps> = (props) => {
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



