import React from 'react';
import { Box, generateUtilityClass, IconButton, styled, Typography } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';

import composeClasses from '@mui/utils/composeClasses';



interface EveliTableDrawerProps {
  filterItems?: string[];
  children: React.ReactNode;
  title?: string | undefined;
  onClose: () => void;
  open: boolean;
}


export const EveliTableDrawer: React.FC<EveliTableDrawerProps> = ({ children, title, onClose, open }) => {
  const classes = useUtilityClasses();
  if (!open) {
    return (<></>);
  }

  return (
    <EveliTableDrawerRoot className={classes.root}>
      <Box className='title'>{title ? <Typography>{title}</Typography> : <Box />}
        <IconButton onClick={onClose}><CloseIcon fontSize='small' /></IconButton>
      </Box>
      {children}
    </EveliTableDrawerRoot>
  )
}


export const EveliTableDrawerRootClassName = 'EveliTableDrawer';

export const EveliTableDrawerRoot = styled('div', {
  name: EveliTableDrawerRootClassName,
  slot: 'VerticalMenuRoot',
  overridesResolver: (_props, styles) => {
    return [
      styles.root,
    ];
  },

})(({ theme }) => {
  return {
    width: '200px',
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

const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(EveliTableDrawerRootClassName, slot);
  return composeClasses(slots, getUtilityClass, {});
}
