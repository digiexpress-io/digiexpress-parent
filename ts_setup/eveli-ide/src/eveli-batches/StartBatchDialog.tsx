import React from 'react';
import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, TextField, Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { BatchApi } from '@/api-batch';

export interface StartBatchDialogProps {
  batch: BatchApi.Batch;
  open: boolean;
  onClose: () => void;
}

export const StartBatchDialog: React.FC<StartBatchDialogProps> = ({ batch, open, onClose }) => {
  const intl = useIntl();
  const [comment, setComment] = React.useState('');

  function handleComment(event: React.ChangeEvent<HTMLInputElement>) {
    setComment(event.target.value)
  }

  console.log(comment)

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
        <TextField onChange={handleComment} label={intl.formatMessage({ id: 'eveli.batches.batchView.startBatch.comment', defaultMessage: 'Comment' })}
          placeholder='Comment' multiline minRows={4} fullWidth></TextField>
      </DialogContent>

      <DialogActions>
        <Button variant='outlined' onClick={onClose}>{intl.formatMessage({ id: 'button.cancel' })}</Button>
        <Button variant='contained' disabled={!comment} onClick={onClose}>{intl.formatMessage({ id: 'button.startBatch' })}</Button>
      </DialogActions>

    </Dialog>
  )
}