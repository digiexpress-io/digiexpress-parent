import { generateUtilityClass, styled, useThemeProps, TextField, TextFieldProps } from '@mui/material'
import composeClasses from "@mui/utils/composeClasses";

import { GInputBaseAnyProps, GInputBaseProps } from '../g-input-base'
import { GInputTextAreaProps } from './GInputTextArea'
import { useVariantOverride } from '../api-variants'
import { GInputError } from '../g-input-error'
import { GInputLabel } from '../g-input-label'
import { GInputAdornment } from '../g-input-adornment'



const MUI_NAME = 'GInputTextArea';


export function useThemeInfra(initProps: GInputTextAreaProps) {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  })
  
  const {
    variant = 'textBox',
    rows = 10,
  } = props;

  const ownerState = {
    ...props,
    variant
  }
  const { id, onChange, value, label, labelPosition, errors } = props;
  const slots: GInputBaseProps<TextFieldProps>  = {
    id,
    slots: {
      error: GInputError,
      label: GInputLabel,
      adornment: GInputAdornment,
      input: GInput,
    },
    slotProps: {
      error: { id, errors },
      input: { name: id, onChange, value: value ?? '', rows, multiline: true, error: (props.errors?.length ?? 0) > 0  },
      label: { id, children: label ?? '', labelPosition },
      adornment: { id, children: props.description, title: label ?? '' }
    }
  }

  const classes = useUtilityClasses(props.id, variant);
  return { classes, ownerState, props, slots };
}


// ------------------- MATERIAL INFRA, ALLOWS STYLE OVERRIDES --------------
const GInput = styled(TextField, {
  name: MUI_NAME,
  slot: 'Input',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      useVariantOverride(props, styles),
      props.name,
    ];
  },
})<GInputBaseAnyProps & TextFieldProps>(({ theme }) => {
  return {

  };
});

// ------------------- MATERIAL INFRA, ALLOWS STYLE OVERRIDES --------------
export const GInputTextAreaRoot = styled("div", {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      useVariantOverride(props, styles)
    ];
  },
})<{ ownerState: { variant: string } }>(({ theme }) => {
  return {

  };
});


// ------------------- MATERIAL INFRA, CSS CLASS NAMES FOR SELECTORS -------
const useUtilityClasses = (itemId: string, variant: string) => {
  const slots = {
    root: [
      'root',
      variant,
      itemId
    ],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}