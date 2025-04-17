import { styled, generateUtilityClass } from '@mui/material'
import composeClasses from '@mui/utils/composeClasses'
import { HeaderGroup } from '@tanstack/react-table';

export const EveliTableRootClassName = 'EveliTable';


const cellPadding = '10px';

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    headerCell: ['headerCell'],
    rowCell: ['rowCell'],
    col: ['col']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(EveliTableRootClassName, slot);
  return composeClasses(slots, getUtilityClass, {});
}

export const EveliTableRoot = styled('div', {
  name: EveliTableRootClassName,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },

})<{}>(({ theme }) => {

  return {
    fontSize: '10pt',
    width: '100%'
  };
});

export const EveliTableHeaderRoot = styled('div', {
  name: EveliTableRootClassName,
  slot: 'HeaderRow',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.headerRow,
      styles.headerCell
    ];
  },

})<{}>(({ theme }) => {


  return {
    backgroundColor: theme.palette.secondary.main,
    borderRadius: `${theme.spacing(1)} 0px 0px 0px`,
    border: `1px solid ${theme.palette.divider}`,
    display: 'flex',
    flexDirection: 'row',

    padding: cellPadding,
    '.MuiTypography-root': {
      fontSize: '10pt'
    },

    '.headerCell': {
      borderRight: `2px solid ${theme.palette.divider}`,
      paddingLeft: cellPadding,
      paddingRight: cellPadding,
      display: 'flex',
      //flex: 1,

      alignItems: 'center',
      '.MuiTypography-root': {
        fontWeight: 'bolder',
      },
      '&:last-of-type': {
        borderRight: 'none', // remove right border from the last cell
        paddingRight: 'unset'
      },
      '.MuiSvgIcon-root': {
        color: theme.palette.primary.main,
        fontSize: '14pt',
        ':hover': {
          cursor: 'pointer'
        }
      }
    },
  };
});


export const EveliTableRowRoot = styled('div', {
  name: EveliTableRootClassName,
  slot: 'Row',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.row,
      styles.rowCell
    ];
  },

})<{}>(({ theme }) => {

  return {
    backgroundColor: theme.palette.background.default,
    border: `1px solid ${theme.palette.divider}`,
    borderTop: 'none',
    display: 'flex',
    padding: cellPadding,
    '.MuiTypography-root': {
      fontSize: '10pt'
    },
    '&:last-of-type': { // target the last row to round the bottom left corner
      borderRadius: `0px 0px 0px ${theme.spacing(1)}`,
    },
    '.rowCell': {
      //paddingLeft: cellPadding,
      paddingRight: cellPadding,
      display: 'flex',
      alignItems: 'center',
    },

  };
});



export const EveliTableColRoot = styled('div', {
  name: EveliTableRootClassName,
  slot: 'Col',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
      styles.col
    ];
  },

})<{ width: number }>(({ theme, width }) => {

  return {
    width,
  };
});
