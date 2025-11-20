import React from 'react';
import { generateUtilityClass, styled, Typography, Drawer, useMediaQuery, useTheme, Box, IconButton, ButtonGroup } from '@mui/material';
import { Close as CloseIcon } from '@mui/icons-material';
import { PictureAsPdfRounded as PictureAsPdfRoundedIcon } from '@mui/icons-material';
import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';
import { TaskApi, useTaskBackend } from '@dxs-ts/task-api';

import { useTaskDashboard } from '../task-dashboard';



export interface FormReviewDrawerProps {
  open: boolean;
  assignment: TaskApi.TaskCustomerAssignment | undefined; 
  onClose: () => void;
}


export const TaskFormReviewDrawer: React.FC<FormReviewDrawerProps> = ({ assignment, onClose, open }) => {
  const theme = useTheme();
  const intl = useIntl();
  const isSmall = useMediaQuery(theme.breakpoints.down('md'));
  const classes = useUtilityClasses();
  const backend = useTaskBackend();
  const { task } = useTaskDashboard();

  return (
    <StyledFormReview className={classes.reviewDrawer}
      anchor={isSmall ? 'bottom' : 'right'}
      open={open}
      onClose={onClose}
      variant="persistent"
    >
      <Box display='flex' alignItems='center' justifyContent='space-between'>
        <Typography variant="h1">{intl.formatMessage({ id: 'taskcard.title.formReview' })}</Typography>

        <ButtonGroup>
          <IconButton onClick={onClose}><CloseIcon color='primary' /></IconButton>
          <IconButton onClick={async () => {
              const pdfBlob = await backend.persistence.getOneTaskPdf({ taskId: task.id, fields: [] });
              const pdfUrl = URL.createObjectURL(pdfBlob);
              const _newWindow = window.open(pdfUrl, '_blank');
            }}><PictureAsPdfRoundedIcon color='primary'/>
          </IconButton>
        </ButtonGroup>
      </Box>
      {open && <backend.slots.DialobReview task={{ id: task.id, questionnaireId: assignment?.questionnaireId ?? task.questionnaireId }} onClose={onClose} />}
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
    '& .GFormPage-root': {
      marginLeft: 'unset',
      marginRight: 'unset'
    },
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
