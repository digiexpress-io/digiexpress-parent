
import { Box, Typography } from '@mui/material';
import { useThemeInfra, GInputLabelRoot } from './useThemeInfra'
import { GInputCurlyBracket } from './GInputCurlyBracket'
import { DialobApi } from '@dxs-ts/gamut-api';


export interface GInputLabelProps {
  id: string;
  labelPosition: DialobApi.ControlLabelPosition,
  children: string; 
  braced?: boolean | undefined;
  required: boolean;
  component?: React.ElementType<GInputLabelProps>;
}

export const GInputLabel: React.FC<GInputLabelProps> = (initProps) => {
  const { classes, props, ownerState } = useThemeInfra(initProps);
  const { labelPosition } = ownerState;
  

  return (<GInputLabelRoot className={classes.root} ownerState={ownerState} as={props.component}>
    <>
      <Typography>{props.children}</Typography>
      {props.required && <Box display='flex' alignItems='center'>
        <Box ml={0.5}>*</Box>
      </Box>}
      {labelPosition === 'label-left' && <GInputCurlyBracket enabled={ownerState.braced}/> }
    </>
  </GInputLabelRoot>);
}