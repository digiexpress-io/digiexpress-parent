import { styled, generateUtilityClass } from '@mui/material'
import composeClasses from '@mui/utils/composeClasses'

export const EveliTableRootClassName = 'EveliTable';


const cellPadding = '10px';

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],

    headerCell: ['headerCell'],
    bodyCell: ['bodyCell'],

    bodyRow: ['bodyRow'],
    headerRow: ['headerRow'],

    pagination: ['pagination'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(EveliTableRootClassName, slot);
  return composeClasses(slots, getUtilityClass, {});
}


export const Root = styled('div', {
  name: EveliTableRootClassName,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },

})<{}>(({ theme }) => {

  return {
    position: 'relative',
    ...theme.typography.subtitle2,
  };
});




export const HeaderRowSlot = styled('div', {
  name: EveliTableRootClassName,
  slot: 'HeaderRow',
})<{}>(({ theme }) => {
  return {
    backgroundColor: theme.palette.secondary.main,
    borderRadius: `${theme.spacing(1)} 0px 0px 0px`,
    border: `1px solid ${theme.palette.divider}`,
    display: 'flex',
    flexDirection: 'row',

    padding: cellPadding,
    '.MuiTypography-root': {
      ...theme.typography.subtitle2
    },

    '.EveliTable-headerCell': {
      overflow: 'hidden',
      textOverflow: 'ellipsis',
      whiteSpace: 'nowrap',
      position: 'relative',
      borderRight: `2px solid ${theme.palette.divider}`,
      paddingLeft: cellPadding,
      paddingRight: cellPadding,
      display: 'flex',
      alignItems: 'center',
      '.MuiTypography-root': {
        fontWeight: 'bolder',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        whiteSpace: 'nowrap',
        flexShrink: 1,
        flexGrow: 1
      },
      '.MuiSvgIcon-root': {
        color: theme.palette.primary.main,
        fontSize: '14pt',
        ':hover': {
          cursor: 'pointer'
        }
      },
      '.columnResizer': {
        position: 'absolute',
        ':hover': {
          backgroundColor: theme.palette.divider,
        },
        right: 0,
        top: 0,
        bottom: 0,
        width: '5px',
        cursor: 'col-resize',
        userSelect: 'none',
        touchAction: 'none',
      }
    },
  };
});


export const BodyRowSlot = styled('div', {
  name: EveliTableRootClassName,
  slot: 'BodyRow',
})<{}>(({ theme }) => {

  return {
    backgroundColor: theme.palette.background.default,
    border: `1px solid ${theme.palette.divider}`,
    borderTop: 'none',
    display: 'flex',
    padding: cellPadding,
    '.MuiTypography-root': {
      ...theme.typography.subtitle2
    },
    '&:last-of-type': { // target the last row to round the bottom left corner
      borderRadius: `0px 0px 0px ${theme.spacing(1)}`,
    },
    '.EveliTable-bodyCell': {
      paddingRight: cellPadding,
      alignItems: 'center',
      overflow: 'hidden',
      textOverflow: 'ellipsis',
      whiteSpace: 'nowrap',
      display: 'block'
    }

  };
});



export const PaginationSlot = styled('div', {
  name: EveliTableRootClassName,
  slot: 'Pagination',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },

})<{}>(({ theme }) => {

  return {
    display: 'flex',
    justifyContent: 'flex-end',
    alignItems: 'center',
    fontSize: '10pt',
    width: '100%',
    borderRadius: `0px 0px 0px ${theme.spacing(1)}`,
    border: `1px solid ${theme.palette.divider}`,
    borderTop: 'unset',
    padding: theme.spacing(0.5),
    '.MuiTypography-root': {
      ...theme.typography.subtitle2,
      marginLeft: theme.spacing(1),
      marginRight: theme.spacing(1)
    },
    '.MuiIconButton-root': {
      '.MuiSvgIcon-root': {
        color: theme.palette.primary.main,
      },
      '&.Mui-disabled .MuiSvgIcon-root': {
        color: theme.palette.action.disabled
      },
    },
    '.MuiFormControl-root.MuiTextField-root': {
      marginTop: '0px'
    },
    '.MuiTextField-root .MuiInputBase-input': {
      paddingLeft: theme.spacing(2),
      paddingTop: theme.spacing(0.5),
      paddingBottom: theme.spacing(0.5)
    }
  };
});