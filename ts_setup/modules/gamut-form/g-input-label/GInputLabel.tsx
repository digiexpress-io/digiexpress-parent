
import { Box, Typography } from '@mui/material';
import { useThemeInfra, GInputLabelRoot } from './useThemeInfra'
import { GInputCurlyBracket } from './GInputCurlyBracket'
import { DialobApi } from '@dxs-ts/gamut-api';
import { GMarkdown } from '@dxs-ts/gamut-md';



export interface GInputLabelClasses {
  root: string;
}
export type GInputLabelClassKey = keyof GInputLabelClasses;


export interface GInputLabelProps {
  id: string;
  labelPosition: DialobApi.ControlLabelPosition,
  // defaults to true... parses label text as markdown
  labelMarkdown?: boolean | undefined; 
  children: string;
  braced?: boolean | undefined;
  required: boolean;
  // field is required but should be shown as black text red asterix
  requiredSoft?: true | undefined;


  errors: DialobApi.ActionError[] | undefined;
  component?: React.ElementType<GInputLabelProps>;
}

export const GInputLabel: React.FC<GInputLabelProps> = (initProps) => {
  const { classes, props, ownerState } = useThemeInfra(initProps);
  const { labelPosition } = ownerState;
  const isErrors = props.errors && props.errors?.length > 0;

  const isErrorRed = props.requiredSoft !== true;
  const requiredColor = (props.required && isErrors) && isErrorRed ? 'error.main' : 'text.primary';
  const labelValue = _isEmpty(props.children) && labelPosition == 'label-top' ? <>&nbsp;</> : _markdownWrapper(ownerState);

  return (<GInputLabelRoot className={classes.root} ownerState={ownerState} as={props.component}>
    <>
      <Typography sx={{ color: requiredColor }} component="span">{labelValue}</Typography>
      {props.required && (
        <Box display='flex' alignItems='center'>
          <Box ml={0.5}><Typography fontSize='15pt' fontWeight='bold' color='error.main'>*</Typography></Box>
        </Box>
      )
      }
      {labelPosition === 'label-left' && <GInputCurlyBracket enabled={ownerState.braced} />}
    </>
  </GInputLabelRoot>);
}


function _markdownWrapper(props: GInputLabelProps): JSX.Element {
  // markdown disabled
  if (!props.labelMarkdown || props.children.trim().length === 0) {
    return <>{props.children}</>
  }
  return (<GMarkdown>{props.children}</GMarkdown>);
}

function _isEmpty(value: unknown): boolean {
  return !value || (typeof value === 'string' && !value.trim());
}