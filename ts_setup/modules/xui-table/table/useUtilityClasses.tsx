import { styled, generateUtilityClass } from '@mui/material'
import composeClasses from '@mui/utils/composeClasses'

export const EveliTableRootClassName = 'EveliTable';

const cellPadding = '10px';
const rowHeight = '40px';

export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],

    headerCell: ['headerCell'],
    bodyCell: ['bodyCell'],
    bodyCellFiller: ['bodyCellFiller'],

    bodyRow: ['bodyRow'],
    bodyFillerRow: ['bodyFillerRow'],
    headerRow: ['headerRow'],

    footer: ['footer'],

    drawer: ['drawer'],

    drawerButtonBar: ['drawerButtonBar'],
    drawerButton: ['drawerButton']
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
    padding: cellPadding,
    backgroundColor: theme.palette.secondary.main,
    borderRadius: `${theme.spacing(1)} 0px 0px 0px`,
    border: `1px solid ${theme.palette.divider}`,
    display: 'flex',
    flexDirection: 'row',
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
    padding: cellPadding,
    height: rowHeight,
    backgroundColor: theme.palette.background.default,
    border: `1px solid ${theme.palette.divider}`,
    borderTop: 'none',
    display: 'flex',
    alignItems: 'center',
    '.MuiTypography-root': {
      ...theme.typography.subtitle2
    },
    '&:last-of-type': { // target the last row to round the bottom left corner
      borderRadius: `0px 0px 0px ${theme.spacing(1)}`,
    },
    '.EveliTable-bodyCell': {
      paddingRight: cellPadding,
      overflow: 'hidden',
      textOverflow: 'ellipsis',
      whiteSpace: 'nowrap',
      display: 'block',
    }
  }
});


export const BodyFillerRowSlot = styled('div', {
  name: EveliTableRootClassName,
  slot: 'BodyFillerRow',
})<{}>(({ theme }) => {

  return {
    height: rowHeight,
    padding: cellPadding,
    border: `1px solid ${theme.palette.divider}`,
    borderTop: 'none',
    display: 'flex',
    minHeight: theme.typography.subtitle2.lineHeight
  }
});


export const FooterSlot = styled('div', {
  name: EveliTableRootClassName,
  slot: 'Footer',
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


export const DrawerSlot = styled('div', {
  name: EveliTableRootClassName,
  slot: 'Drawer',
})(({ theme }) => {
  return {
    width: '450px',
    position: 'absolute',
    overflow: 'scroll',
    top: 0,
    bottom: 0,
    boxShadow: '-2px 0px 8px rgba(0, 0, 0, 0.1)',
    right: '0px',
    backgroundColor: theme.palette.secondary.main,
    border: `1px solid ${theme.palette.divider}`,
    zIndex: 10,
    '.title': {
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      marginLeft: theme.spacing(1)
    },
    '& .title .MuiTypography-root': {
      ...theme.typography.subtitle2,
      fontWeight: 'bold'
    }
  }
});


export const DrawerButtonBarSlot = styled('div', {
  name: EveliTableRootClassName,
  slot: 'DrawerButtonBar',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
    ];
  },

})(({ theme }) => {

  return {
    grow: 1,
    paddingTop: theme.spacing(1),
    gap: theme.spacing(3),
    display: 'flex',
    borderRadius: '0px 10px 10px 0px',
    backgroundColor: theme.palette.secondary.main,
    border: `1px solid ${theme.palette.divider}`,
    borderLeft: 'unset',
    flexDirection: 'column',
    alignItems: 'center'
  };
});



export const DrawerButtonSlot = styled('div', {
  name: EveliTableRootClassName,
  slot: 'DrawerButton',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
    ];
  },

})(({ theme }) => {

  return {
    '.MuiButtonBase-root': {
      writingMode: 'vertical-rl',
      transform: 'rotate(360deg)',
      backgroundColor: 'transparent',
      ':hover': {
        backgroundColor: 'transparent',
      },
      '.MuiButton-icon': {
        marginRight: '0px',
        marginLeft: '0px',
        marginBottom: theme.spacing(1),
      }
    },
    '.MuiTypography-root': {
      color: theme.palette.text.primary,
      fontSize: '10pt'
    }
  }
});