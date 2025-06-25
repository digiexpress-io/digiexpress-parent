import React from 'react';
import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, TextField, Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { BatchApi } from '@/api-batch';
import { useFetch } from '@dxs-ts/eveli-fetch';

export interface StartBatchDialogProps {
  batch: BatchApi.Batch;
  open: boolean;
  onClose: () => void;
}

export const StartBatchDialog: React.FC<StartBatchDialogProps> = ({ batch, open, onClose }) => {
  const intl = useIntl();
  const [instanceName, setInstanceName] = React.useState('');
  const [commitMessage, setCommitMessage] = React.useState('');
  const { createInstance } = useFetch('worker/rest/api/batches/$batchName/instances.POST', {})

  function handleComment(event: React.ChangeEvent<HTMLInputElement>) {
    setCommitMessage(event.target.value)
  }
  
  function handleInstanceName(event: React.ChangeEvent<HTMLInputElement>) {
    setInstanceName(event.target.value)
  }

  function handleStartBatch() {
    createInstance(
      batch.batchName, 
      { commitMessage, instanceName, params: {} })
    .then(onClose);
  }


  return (
    <Dialog open={open} onClose={onClose}>
      <DialogTitle>
        {intl.formatMessage({ id: 'eveli.batches.batchView.startBatch', defaultMessage: 'Start new batch run' })}
      </DialogTitle>
      <DialogContent>
        <Box mb={3}>
          <Typography component='span'>{intl.formatMessage({ id: 'eveli.batches.batchView.startBatch.selected', defaultMessage: 'You are about to initiate a new run of' })}
            {intl.formatMessage({ id: 'eveli.textSeparatorColon' })}
          </Typography>
          <Typography component='span' fontWeight='bold'>{batch.batchName}</Typography>
        </Box>
        <TextField onChange={handleInstanceName} 
          label={intl.formatMessage({ id: 'eveli.batches.batchView.startBatch.instanceName', defaultMessage: 'Name of the run' })}
          placeholder='Instance name' multiline minRows={2} fullWidth/>

        <TextField onChange={handleComment} 
          label={intl.formatMessage({ id: 'eveli.batches.batchView.startBatch.comment', defaultMessage: 'Comment' })}
          placeholder='Comment' multiline minRows={4} fullWidth />
      </DialogContent>

      <DialogActions>
        <Button variant='outlined' onClick={onClose}>{intl.formatMessage({ id: 'button.cancel' })}</Button>
        <Button variant='contained' disabled={!commitMessage} onClick={handleStartBatch}>{intl.formatMessage({ id: 'button.startBatch' })}</Button>
      </DialogActions>

    </Dialog>
  )
}