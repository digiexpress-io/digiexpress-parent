import React from 'react';
import { generateUtilityClass, styled, SxProps } from '@mui/material'
import composeClasses from '@mui/utils/composeClasses'
import { useVariantOverride } from '../api-variants';
import { GInputGroupRowProps } from './GInputGroupRow';



export const MUI_NAME = 'GInputGroupRow';


export const useUtilityClasses = (itemId: string) => {
  const slots = {
    root: ['root', itemId],
    label: ['label'],
    body: ['body']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const GInputGroupRowRoot = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  shouldForwardProp: (prop) => prop !== 'onDelete' && prop !== 'ownerState',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      useVariantOverride(props, styles)
    ];
  },
})<{ ownerState: {  } }>(({ theme }) => {
  return {
    
  };
});






// ------------------- MATERIAL INFRA, ALLOWS STYLE OVERRIDES --------------
export const GInputGroupRowLabel = styled('div', {
  name: MUI_NAME,
  slot: 'Label',
  shouldForwardProp: (prop) => prop !== 'onDelete' ,
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      ...useVariantOverride(props, styles)
    ];
  },
})<GInputGroupRowProps>(({ theme, label }) => {
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
export const GInputGroupRowBody = styled('div', {
  name: MUI_NAME,
  slot: 'Body',
  shouldForwardProp: (prop) => prop !== 'onDelete',
  overridesResolver: (props, styles) => {
    return [
      styles.root,
      ...useVariantOverride(props, styles)
    ];
  },
})<GInputGroupRowProps>(({ theme, columns, children, id }) => {
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