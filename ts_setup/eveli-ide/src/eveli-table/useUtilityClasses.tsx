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
    position: 'relative',
    ...theme.typography.subtitle2,
    width: '100%',
    overflowX: 'auto',
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
      ...theme.typography.subtitle2
    },

    '.headerCell': {
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
      ...theme.typography.subtitle2
    },
    '&:last-of-type': { // target the last row to round the bottom left corner
      borderRadius: `0px 0px 0px ${theme.spacing(1)}`,
    },
    '.rowCell': {
      paddingRight: cellPadding,
      alignItems: 'center',
      overflow: 'hidden',
      textOverflow: 'ellipsis',
      whiteSpace: 'nowrap',
      display: 'block'
    }

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
