import React from 'react';
import { Box, Button, TextField, Typography, Stack } from '@mui/material';

import { FormattedMessage, useIntl } from 'react-intl';
import { TaskApi, useTaskBackend } from '@dxs-ts/task-api';


export interface CreateTaskTransferProps {
  task: TaskApi.Task;
  onTransferComplete: () => void;
}

export const CreateTaskTransfer: React.FC<CreateTaskTransferProps> = (props) => {
  const intl = useIntl();
  const [title, setTitle] = React.useState<string>(props.task.transferredId ?? '');
  const [isSaving, setSaving] = React.useState(false);
  const backend = useTaskBackend();

  function handleOnTransfer() {
    setSaving(true)
    backend.persistence
      .createOnTaskTransfer(props.task, { transferTitle: title })
      .then(() => props.onTransferComplete())
  }

  return (
    <>
      <div style={{ display: 'flex', flexDirection: 'column', padding: 10 }}>
        <Stack spacing={3}>
          <Typography variant='h3' fontWeight='bold' mr={3}>{intl.formatMessage({ id: 'task.transfer.create.title' })}</Typography>
          <div>
            <Typography mt={2} fontWeight='bold'>{intl.formatMessage({ id: 'task.transfer.create.docTitle' })}</Typography>
            <TextField onChange={(e) => setTitle(e.target.value)}
              sx={{ mb: 3 }}
              placeholder={intl.formatMessage({ id: 'task.transfer.create.docTitle.placeholder' })}
              value={title ?? ''}
            />
          </div>

        { props.task.transferredId && (<>
            <Typography variant='h3' fontWeight='bold' mr={3}>{intl.formatMessage({ id: 'task.transfer.props.title' })}</Typography>
            <div>
              {JSON.stringify(props.task.transferredProps ?? {}, null, 2)}
            </div>
        </>)}
        </Stack>

      </div>
      <Box display='flex' gap={1}>
        <Button variant='contained' onClick={handleOnTransfer} disabled={!title || isSaving}>
          { 
            props.task.transferredId ? 
            <FormattedMessage id='button.republish' /> :
            <FormattedMessage id='button.publish' />
          }
        </Button>
      </Box>
    </>
  );
}