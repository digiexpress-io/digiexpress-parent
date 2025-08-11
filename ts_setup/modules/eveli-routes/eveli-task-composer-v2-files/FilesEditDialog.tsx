import React from 'react';
import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, generateUtilityClass, styled, Zoom } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';

import { TaskApi } from '@dxs-ts/eveli-api';
import { FilesEditor } from './FilesEditor';


export interface FilesEditProps {
  task: TaskApi.Task;
  open: boolean,
  onClose: () => void
}

export const FilesEditDialog: React.FC<FilesEditProps> = ({ task, open, onClose }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();


  return (
    <StyledDialog fullWidth maxWidth='lg' className={classes.root} open={open} onClose={onClose} slots={{ transition: Zoom }}>

      <DialogTitle sx={{ display: 'flex' }}>
        {intl.formatMessage({ id: 'task.attachments', defaultMessage: 'Task attached files' })}
        {intl.formatMessage({ id: 'eveli.textSeparatorColon' })}
        {task.taskRef ?? 'no task reference id'}
        <Box flexGrow={1} />

        <div>
          <Button variant='contained' startIcon={<AddIcon />}>{intl.formatMessage({ id: 'eveli.uploadFile', defaultMessage: 'Upload new file' })}</Button>
        </div>

      </DialogTitle>

      <DialogContent>
        <FilesEditor task={task} />
      </DialogContent>

      <DialogActions>
        <Button variant='outlined' onClick={onClose}>{intl.formatMessage({ id: 'button.cancel' })}</Button>
        <Button>{intl.formatMessage({ id: 'button.save' })}</Button>
      </DialogActions>
    </StyledDialog>
  )
}





const MUI_NAME = 'FilesEditDialog';
const StyledDialog = styled(Dialog, {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },

})(({ theme }) => {

  return {
    '.MuiDialogContent-root': {
      display: 'flex',
      flexDirection: 'column',
      overflow: 'hidden'
    }
  };
})


const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
