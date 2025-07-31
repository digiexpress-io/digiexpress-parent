import React from 'react';
import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, generateUtilityClass, styled, Typography, Zoom } from '@mui/material';
import HistoryIcon from '@mui/icons-material/History';
import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';

import { TaskApi } from '@/api-task';
import { NotesEditor } from './NotesEditor';



export interface NotesEditDialogProps {
  task: TaskApi.Task;
  open: boolean,
  onClose: () => void
}

export const NotesEditDialog: React.FC<NotesEditDialogProps> = ({ task, open, onClose }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();

  return (
    <StyledEditNotesDialog fullWidth maxWidth='xl' className={classes.editDialog} open={open} onClose={onClose} slots={{ transition: Zoom }}>

      <DialogTitle>
        {intl.formatMessage({ id: 'task.note.edit', defaultMessage: 'Edit notes for task' })}
        {intl.formatMessage({ id: 'eveli.textSeparatorColon' })}
        {task.taskRef ?? 'no task reference id'}
      </DialogTitle>

      <DialogContent>
        <Box className={classes.historyLabel}>
          <HistoryIcon />
          <Typography>{intl.formatMessage({ id: 'task.note.history', defaultMessage: 'Note history' })}</Typography>
        </Box>
        <NotesEditor task={task} />
      </DialogContent>

      <DialogActions>
        <Button variant='outlined' onClick={onClose}>{intl.formatMessage({ id: 'button.cancel' })}</Button>
        <Button onClick={onClose}>{intl.formatMessage({ id: 'button.save' })}</Button>
      </DialogActions>
    </StyledEditNotesDialog>
  )
}




const MUI_NAME = 'EditNotesDialog';
const StyledEditNotesDialog = styled(Dialog, {
  name: MUI_NAME,
  slot: 'EditDialog',
  overridesResolver: (_props, styles) => {
    return [
      styles.editDialog
    ];
  },

})(({ theme }) => {

  return {
    height: '100vh',
    '.MuiDialogContent-root': {
      display: 'flex',
      flexDirection: 'column',
      overflow: 'hidden',
    },
    '.EditNotesDialog-historyLabel': {
      marginLeft: theme.spacing(1),
      display: 'flex',
      alignItems: 'center',
      '& .MuiTypography-root': {
        ...theme.typography.body2,
        fontWeight: 'bold'
      },
      '& .MuiSvgIcon-root': {
        fontSize: '20pt',
        marginRight: theme.spacing(1),
        marginLeft: theme.spacing(1),
        color: theme.palette.primary.main
      },
    },

  };
})


const useUtilityClasses = () => {
  const slots = {
    editDialog: ['editDialog'],
    historyLabel: ['historyLabel']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
