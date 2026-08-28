import React from 'react'

import { generateUtilityClass, styled, SxProps, useThemeProps } from '@mui/material'
import composeClasses from '@mui/utils/composeClasses'
import { useVariantOverride } from '@dxs-ts/gamut-api'
import { GFormGroupProps, GFormGroupSlot } from './g-form-group-types'
import { GFormGroupCollapsible } from './GFormGroupCollapsible'



const MUI_NAME = 'GFormGroup';

export function useThemeInfra(initProps: GFormGroupProps) {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const classes = useUtilityClasses(props);
  const ownerState = { ...props };
  const slots: {
    label: GFormGroupSlot;
    body: GFormGroupSlot;
    collapsible: GFormGroupSlot;
  } = {
    label: props.slots?.label ?? GFormGroupLabel as any,
    body: props.slots?.body ?? GFormGroupBody as any,
    collapsible: props.slots?.collapsible ?? GFormGroupCollapsible as any,
  }
  return { classes, ownerState, slots };
}


// ------------------- MATERIAL INFRA, CSS CLASS NAMES FOR SELECTORS -------
export const useUtilityClasses = (ownerState: GFormGroupProps) => {
  const slots = {
    root: ['root', ownerState.id],
    label: ['label'],
    labelContent: ['labelContent'],
    body: ['body'],
    collapsible: ['collapsible']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


// ------------------- MATERIAL INFRA, ALLOWS STYLE OVERRIDES --------------
export const GFormGroupRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      ...useVariantOverride(props, styles)
    ];
  },
})<{ ownerState: GFormGroupProps, className: string, children: React.ReactNode }>(({ theme, ownerState }) => {

  // Each child group must have a greater margin than its parent group to visually show nested levels
  const nestingLevel = ownerState.level ?? 0;

  return {

    // Page 
    ...(ownerState.border ? {
      border: `1px solid ${theme.palette.divider}`,
      padding: theme.spacing(2),
      margin: nestingLevel > 1 ? theme.spacing(nestingLevel) : theme.spacing(1),
      boxShadow: '0px 4px 10px rgba(0, 0, 0, 0.1)'
    } : {}),
    ...(ownerState.readOnly ? {
      cursor: 'not-allowed'
    } : {}),
  };
});


// ------------------- MATERIAL INFRA, ALLOWS STYLE OVERRIDES --------------
export const GFormGroupLabel = styled('div', {
  name: MUI_NAME,
  slot: 'Label',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      ...useVariantOverride(props, styles)
    ];
  },
})<{ ownerState: GFormGroupProps }>(({ theme, ownerState }) => {
  const { label } = ownerState;

  return {
    display: 'flex',
    '& .GFormGroup-labelContent': {
      display: 'flex',
      alignItems: 'center',
    },
    '& .MuiDivider-root': {
      display: label ? undefined : 'none',
      flexGrow: 1,
      alignSelf: 'center',
      marginLeft: theme.spacing(2)
    },
    '& .MuiTypography-root': {
      ...theme.typography.h3
    }
  };
});


// ------------------- MATERIAL INFRA, ALLOWS STYLE OVERRIDES --------------
const GFormGroupBody = styled('div', {
  name: MUI_NAME,
  slot: 'Body',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      ...useVariantOverride(props, styles)
    ];
  },

})<{ ownerState: GFormGroupProps }>(({ theme, ownerState }) => {
  const { columns, id, children } = ownerState;
  let enabled = false;
  try {
    enabled = !!columns && parseInt(columns) > 1;
  } catch (e) {
    console.warn('unsupported columns definition', { id, columns });
  }
  if (!enabled) {
    return {};
  }
  const colCount = parseInt(columns!);
  const rowCount = Math.round(React.Children.count(children) / colCount);

  const labels: SxProps = colCount > 2 ? {
    '& .GInputBase-label': {
      width: '100px'
    },
    '& .GInputBase-label .MuiTypography-root': {
      whiteSpace: 'nowrap',
      textOverflow: 'ellipsis',
      overflow: 'hidden',
    }
  } : {};

  return {
    display: 'grid',
    gridAutoFlow: 'row',
    gridTemplateRows: `repeat(${rowCount}, auto)`,
    gridTemplateColumns: `repeat(${colCount}, 1fr)`,
    alignItems: 'end',

    // ------------------------- compensation for MUI bug - force grid items in dialob's multi-col group layout to re-render with full width 
    '& .MuiGrid-item': {
      width: '100% !important',
      maxWidth: 'none !important',
      flex: 'auto',
    },
    '& .GInputLabel-root': {
      justifyContent: 'flex-start'
    },
    // --------------------------

    '& .GFormBase-root': {
      paddingRight: theme.spacing(1),
    },
    ...labels
  };
});