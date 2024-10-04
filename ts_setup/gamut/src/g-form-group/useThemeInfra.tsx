import React from 'react'

import { generateUtilityClass, styled, SxProps, useThemeProps } from '@mui/material'
import composeClasses from '@mui/utils/composeClasses'
import { useVariantOverride } from '../api-variants'
import { GFormGroupProps } from './GFormGroup'
import { display, maxWidth, width } from '@mui/system'



const MUI_NAME = 'GFormGroup';

export function useThemeInfra(initProps: GFormGroupProps) {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const classes = useUtilityClasses(props);
  const ownerState = { ...props };
  const slots: { 
    label: React.ElementType, 
    body: React.ElementType
  } = {
    label: props.slots?.label ?? GFormGroupLabel,
    body: props.slots?.body ?? GFormGroupBody
  }
  return { classes, ownerState, props, slots };
}


// ------------------- MATERIAL INFRA, CSS CLASS NAMES FOR SELECTORS -------
const useUtilityClasses = (ownerState: GFormGroupProps) => {
  const slots = {
    root: ['root', ownerState.id],
    label: ['label'],
    body: ['body']
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
})<{ ownerState: GFormGroupProps }>(({ theme }) => {
  return {

  };
});


// ------------------- MATERIAL INFRA, ALLOWS STYLE OVERRIDES --------------
const GFormGroupLabel = styled('div', {
  name: MUI_NAME,
  slot: 'Label',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      ...useVariantOverride(props, styles)
    ];
  },
})<GFormGroupProps>(({ theme, label }) => {
  return {
    display: 'flex',
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
  
})<GFormGroupProps>(({ theme, columns, id, children }) => {
  let enabled = false;
  try {
    enabled = !!columns && parseInt(columns) > 1;
  } catch(e) {
    console.warn('unsupported columns definition', { id, columns });
  }
  if(!enabled) {
    return {};
  }
  const colCount = parseInt(columns!);
  const rowCount = Math.round(React.Children.count(children)/colCount);

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

    '& .GFormBase-root': {
      paddingRight: theme.spacing(1),
    },
    ...labels
  };
});