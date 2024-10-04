import React from 'react';
import { useThemeProps, Button, Typography } from '@mui/material';
import { OverridableStringUnion } from '@mui/types';
import CheckBoxIcon from '@mui/icons-material/CheckBox';
import CheckBoxOutlineBlankIcon from '@mui/icons-material/CheckBoxOutlineBlank';

import { GInputBase, GInputBaseAnyProps, GInputBaseProps, LabelPosition } from '../g-input-base';
import { DialobApi } from '../api-dialob';
import { GInputError } from '../g-input-error';
import { GInputLabel } from '../g-input-label';
import { GInputAdornment } from '../g-input-adornment';


import { useUtilityClasses, MUI_NAME, GInputBooleanRoot } from './useUtilityClasses';
import { FormattedMessage } from 'react-intl';


// extension hook for adding custom input types
export interface GInputBooleanPropsVariantOverrides { };

export interface GInputBooleanProps {
  id: string;
  value: boolean | undefined;
  onChange: (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
  label: string | undefined;
  labelPosition: LabelPosition,
  description: string | undefined;

  errors?: DialobApi.ActionError[] | undefined;
  invalid?: boolean | undefined;

  variant: OverridableStringUnion<
    'checkbox',
    GInputBooleanPropsVariantOverrides
  > | undefined;

  slots?: Record<OverridableStringUnion<
    'checkbox',
    GInputBooleanPropsVariantOverrides>,
    React.ElementType>; 

  component?: React.ElementType<GInputBooleanProps>;
}



export const GInputBoolean: React.FC<GInputBooleanProps> = (initProps) => {

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });

  const { id, label, variant = 'checkbox', labelPosition, errors } = props;
  const ownerState = { ...props, variant };
  const classes = useUtilityClasses(id, variant);

  const slots: GInputBaseProps<GInputBooleanProps> =  {
    id,
    slots: {
      error: GInputError,
      label: GInputLabel,
      input: YesAndNoCheckbox,
      adornment: GInputAdornment
    },
    slotProps: {
      error: { id, errors },
      input: { ...ownerState, name: id },
      label: { id, children: label ?? '', labelPosition },
      adornment: { id, children: props.description, title: label }
    }
  }

  return (<GInputBooleanRoot className={classes.root} ownerState={ownerState} as={props.component}>
    <GInputBase id={props.id} slots={slots.slots} slotProps={slots.slotProps} />
  </GInputBooleanRoot>);
}


const YesAndNoCheckbox: React.FC<GInputBaseAnyProps & GInputBooleanProps> = (props) => {
  const { onChange, id, variant, value } = props;
  const ref = React.useRef<HTMLInputElement>(null);
  const classes = useUtilityClasses(id, variant);
  const [inputValue, setInputValue] = React.useState<string>(value ? value + '' : '');
  const [sync, setSync] = React.useState<boolean>(false);

  React.useEffect(() => {
    function poulateTheChange(event: any) {
      onChange(event);
    }
    ref.current?.addEventListener("input", poulateTheChange);
    return () => ref.current?.removeEventListener("input", poulateTheChange);
  }, [onChange]);

  React.useEffect(() => {
    if(sync) {
      const event = new Event('input', { bubbles: true });
      ref.current?.dispatchEvent(event);
    }
  },[sync, inputValue]);

  function toggleYes() {
    setInputValue(inputValue === 'true' ? '' : 'true');
    setSync(true);
  }
  function toggleNo() {
    setInputValue(inputValue === 'false' ? '' : 'false');
    setSync(true);

  }

  function doNothing() {

  }

  function startIcon(checked: boolean) {
    return checked ? <CheckBoxIcon className={classes.optionIcon} /> : <CheckBoxOutlineBlankIcon className={classes.optionIcon} />;
  }

  const isYes: boolean = inputValue === 'true';
  const isNo: boolean = inputValue === 'false';

  return (
    <div className={classes.input}>
      <Button fullWidth className={classes.option} variant='outlined' onClick={toggleYes} startIcon={startIcon(isYes)}>
        <Typography className={classes.optionTitle}><FormattedMessage id='gamut.forms.answer.boolean.yes'/></Typography>
      </Button>
      
      <Button fullWidth className={classes.option} variant='outlined' onClick={toggleNo} startIcon={startIcon(isNo)}>
        <Typography className={classes.optionTitle}><FormattedMessage id='gamut.forms.answer.boolean.no'/></Typography>
      </Button>
      
      <input hidden value={inputValue} ref={ref} onChange={doNothing} />
    </div>
  );
}

