import React from 'react';
import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, generateUtilityClass, Grid2, styled, TextField, Typography, Zoom } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';

import { TaskApi, useTaskBackend } from '@dxs-ts/task-api';


export interface TaskTransferEditProps {
  task: TaskApi.Task;
  open: boolean,
  onClose: () => void;
}

export const TaskTransferEditDialog: React.FC<TaskTransferEditProps> = ({ task, open, onClose }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  const [title, setTitle] = React.useState<string>(task.transferredId ?? '');
  const [error, setError] = React.useState<Error | undefined>();
  const [isSaving, setSaving] = React.useState(false);
  const backend = useTaskBackend();

  function handleOnTransfer() {
    setSaving(true)
    backend.persistence
      .createOnTaskTransfer(task, { transferTitle: title })
      .then(() => setSaving(false))
      .then(() => onClose())
      .catch((error: any) => {
        setSaving(false);
        setError(error);
      })
  }

  return (
    <>
      <StyledDialog fullWidth maxWidth='lg' className={classes.root} open={open} onClose={onClose} slots={{ transition: Zoom }}>
        <DialogTitle sx={{ display: 'flex' }}>
          {intl.formatMessage({ id: 'task.transfer.create.title', defaultMessage: 'Task transfer' })}
          {intl.formatMessage({ id: 'eveli.textSeparatorColon' })}
          {task.taskRef ?? 'no task reference id'}
          <Box flexGrow={1} />
        </DialogTitle>

        <DialogContent>
          {error && <Typography color='error'>
            {intl.formatMessage({ id: 'task.transfer.create.error', defaultMessage: 'An error has occured: ' })}
            {error.message}
          </Typography>
          }
          <Grid2 container display='flex' alignItems='center'>
            <Grid2 size={{ md: 3, lg: 3, xl: 3 }}>
              <Typography fontWeight='bold'>{intl.formatMessage({ id: 'task.transfer.create.docTitle', defaultMessage: 'Document title' })}</Typography>
            </Grid2>
            <Grid2 size={{ md: 7, lg: 7, xl: 7 }}>
              <StyledTextField
                fullWidth
                placeholder={intl.formatMessage({ id: 'task.transfer.create.docTitle.placeholder', defaultMessage: 'Document title' })}
                onChange={(e: any) => setTitle(e.target.value)}
                value={title ?? ''}
              />
            </Grid2>
          </Grid2>

          {task.transferredId && (
            <>
              <Typography variant='h3' fontWeight='bold' mr={3}>{intl.formatMessage({ id: 'task.transfer.props.title' })}</Typography>
              <div>{JSON.stringify(task.transferredProps ?? {}, null, 2)} </div>
            </>
          )}

        </DialogContent>

        <DialogActions>
          <Button variant='outlined' onClick={onClose}>{intl.formatMessage({ id: 'button.cancel' })}</Button>
          <Button variant='contained' onClick={handleOnTransfer} disabled={!title || isSaving}>
            {
              task.transferredId ? intl.formatMessage({ id: 'button.republish' }) : intl.formatMessage({ id: 'button.publish' })
            }
          </Button>
        </DialogActions>
      </StyledDialog>
    </>
  )
}

const StyledTextField = styled(TextField)(({ theme }) => ({
  '& .MuiInputBase-input': {
    height: '2.5rem',
    padding: '0 12px'
  },
  '& .MuiInputBase-multiline': {
    paddingLeft: '0px',
    paddingRight: '0px'
  },
}));



const MUI_NAME = 'TaskTransferEditDialog';
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
