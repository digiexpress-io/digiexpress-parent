import React from 'react';
import { generateUtilityClass, styled, useThemeProps, TextField, TextFieldProps } from '@mui/material'
import composeClasses from "@mui/utils/composeClasses";

import { GInputBaseAnyProps, GInputBaseProps } from '../g-input-base'
import { GInputTextAreaProps } from './GInputTextArea'
import { useVariantOverride } from '@dxs-ts/gamut-api'
import { GInputError } from '../g-input-error'
import { GInputLabel } from '../g-input-label'
import { GInputAdornment } from '../g-input-adornment'
import { useGFormErrorVisibility } from '../g-form-error-visibility';



const MUI_NAME = 'GInputTextArea';

export function useThemeInfra(initProps: GInputTextAreaProps) {

  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  })

  const { isErrorsVisible } = useGFormErrorVisibility({ controlId: props.id });
  const visibleErrors = isErrorsVisible ? props.errors : undefined;

  const {
    variant = 'textBox',
    rows = 10,
  } = props;

  const ownerState = {
    ...props,
    variant,
    visibleErrors
  }

  const { id, onChange, value, label, labelPosition, errors } = props;
  const slots: GInputBaseProps<TextFieldProps> = {
    id,
    slots: {
      error: GInputError,
      label: GInputLabel,
      adornment: GInputAdornment,
      input: props.readOnly ? ReadOnlyTextarea : GInput,
    },
    slotProps: {
      error: { id, errors },
      input: { name: id, onChange, value: value ?? '', rows, multiline: true, errors: props.errors, disabled: props.disabled },
      label: { id, children: label ?? '', labelPosition, required: initProps.required, errors: visibleErrors },
      adornment: { id, children: props.description, title: label ?? '', disabled: props.disabled }
    }
  }
  const classes = useUtilityClasses(props.id, variant);
  return { classes, ownerState, props, slots };
}


const ReadOnlyTextarea: React.FC<GInputBaseAnyProps & TextFieldProps & { errors?: any }> = (props) => {
  return (
    <TextField
      value={props.value}
      rows={props.rows}
      multiline
      slotProps={{ input: { readOnly: true } }}
    />
  );
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
})<GInputBaseAnyProps & TextFieldProps & { errors?: any }>(({ theme, errors }) => {

  const hasErrors = !!errors && (Array.isArray(errors) ? errors.length > 0 : true);

  return hasErrors ? {
    '& .MuiOutlinedInput-root .MuiOutlinedInput-notchedOutline': {
      borderColor: theme.palette.error.main,
    }
  } : {

  };
});

interface OwnerState {
  variant: string;
  disabled: boolean;
  readOnly?: boolean;
  visibleErrors?: any;
}

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
})<{ ownerState: OwnerState }>(({ theme, ownerState }) => {
  const isErrorsVisible = ownerState.visibleErrors && ownerState.visibleErrors.length > 0;

  if (ownerState.disabled) {
    return {
      '& .MuiInputBase-input.Mui-disabled': {
        WebkitTextFillColor: theme.palette.info.main,
      },
      '& .MuiOutlinedInput-root.Mui-disabled': {
        backgroundColor: theme.palette.background.paper,
      }
    }
  }

  if (ownerState.readOnly) {
    return {
      '& .MuiOutlinedInput-notchedOutline': {
        border: 'none',
      },
      '& .MuiOutlinedInput-root': {
        backgroundColor: 'transparent',
      },
      '& .MuiInputBase-input': {
        cursor: 'not-allowed',
        color: theme.palette.text.primary,
        ...theme.typography.body1,
      },
    }
  }

  return {
    ...(isErrorsVisible && {
      "& .GInputLabel-root .MuiTypography-root": {
        color: theme.palette.error.main,
      },
    }),
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