import React from 'react';
import { generateUtilityClass, styled, Typography, Drawer, useMediaQuery, useTheme, Box, IconButton, Slide } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import composeClasses from '@mui/utils/composeClasses';



export interface FormReviewDrawerProps {
  open: boolean;
  onClose: () => void;
}


export const FormReviewDrawer: React.FC<FormReviewDrawerProps> = ({ onClose, open }) => {
  const theme = useTheme();
  const isSmall = useMediaQuery(theme.breakpoints.down('md'));
  const classes = useUtilityClasses();

  return (
    <StyledFormReview className={classes.reviewDrawer}
      anchor={isSmall ? 'bottom' : 'right'}
      open={open}
      onClose={onClose}
      variant="persistent"
    >
      <Box display='flex' alignItems='center' justifyContent='space-between'>
        <Typography variant="h1">Form review</Typography>
        <IconButton onClick={onClose}><CloseIcon color='primary' /></IconButton>
      </Box>
    </StyledFormReview>
  );
};


const MUI_NAME = 'FormReview';
const StyledFormReview = styled(Drawer, {
  name: MUI_NAME,
  slot: 'Drawer',
  overridesResolver: (_props, styles) => {
    return [
      styles.reviewDrawer
    ];
  },
})(({ theme }) => {
  const drawerWidthOpen = '40%';

  return {
    '& .MuiDrawer-paper': {
      width: drawerWidthOpen,
      height: '100%',
      padding: theme.spacing(2),
      transform: 'translateX(100%)',
      transition: 'transform 0.3s ease-in-out',
      boxSizing: 'border-box',
      [theme.breakpoints.down('md')]: {
        width: '100%',
        transform: 'translateY(100%)'
      },
      '&.MuiDrawer-paperAnchorRight.MuiDrawer-paperOpen': {
        transform: 'translateX(0)',
      },
      '&.MuiDrawer-paperAnchorBottom.MuiDrawer-paperOpen': {
        transform: 'translateY(0)',
      },
    },
  }
});



export const useUtilityClasses = () => {
  const slots = {
    reviewDrawer: ['reviewDrawer'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
