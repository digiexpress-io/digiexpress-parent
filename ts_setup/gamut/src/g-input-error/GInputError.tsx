import React from 'react'
import { GInputErrorRoot, useThemeInfra} from './useThemeInfra'
import { DialobApi } from '../api-dialob';
import { FormHelperText } from '@mui/material';


export interface GInputErrorProps {
  id: string;
  errors: DialobApi.ActionError[] | undefined;
  component?: React.ElementType<GInputErrorProps>;
}


export const GInputError: React.FC<GInputErrorProps> = (initProps) => {
  const {classes, ownerState, props} = useThemeInfra(initProps);

  if((props.errors?.length ?? 0) === 0) {
    return (<></>)
  }

  return (<GInputErrorRoot className={classes.root} ownerState={ownerState} as={props.component}>
    {props.errors!.map(e => <FormHelperText error key={`${e.id}-${e.code}`}>{e.description}</FormHelperText>)}
    
  </GInputErrorRoot>);
}

