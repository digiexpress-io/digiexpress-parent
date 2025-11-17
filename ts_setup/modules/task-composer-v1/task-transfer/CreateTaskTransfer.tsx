import React from 'react';
import { Box, Button, TextField, Typography, Stack, List, ListItem } from '@mui/material';

import { FormattedMessage, useIntl } from 'react-intl';
import { TaskApi, useTaskBackend } from '@dxs-ts/task-api';


export interface CreateTaskTransferProps {
  task: TaskApi.Task;
  onTransferComplete: () => void;
}

export const CreateTaskTransfer: React.FC<CreateTaskTransferProps> = (props) => {
  const intl = useIntl();
  const [isSaving, setSaving] = React.useState(false);
  const backend = useTaskBackend();

  function handleOnTransfer() {
    setSaving(true)
    backend.persistence
      .createOnTaskTransfer(props.task, {})
      .then(() => props.onTransferComplete())
  }

  return (
    <>
      <div style={{ display: 'flex', flexDirection: 'column', padding: 10 }}>
        <Stack spacing={3}>
          <Typography variant='h3' fontWeight='bold' mr={3}>{intl.formatMessage({ id: 'task.transfer.create.title' })}</Typography>
          <div>
            <Typography mt={2} fontWeight='bold'>{intl.formatMessage({ id: 'task.transfer.create.journalNumber' })}</Typography>
            <TextField 
              variant="filled"
              slotProps={{
                input: {
                  readOnly: true,
                },
              }}
              sx={{ mb: 3 }}
              value={props.task.transferredId ?? ''}
            />
          </div>

        { props.task.transferredId && (<>
            <Typography variant='h3' fontWeight='bold' mr={3}>{intl.formatMessage({ id: 'task.transfer.create.files' })}</Typography>
            <div>
              {props.task.transferredProps && Object.entries(props.task.transferredProps).filter(([key, value])=>{key==='files'}).map(([key, value])=>{
                if (Array.isArray(value)) {
                  return (
                    <List>
                      {value.map(file => {
                        return (<ListItem>
                          {file}
                        </ListItem>)
                      })}
                    </List>
                  )
                }
                return null;
              })}
            </div>
        </>)}
        </Stack>

      </div>
      <Box display='flex' gap={1}>
        <Button variant='contained' onClick={handleOnTransfer} disabled={isSaving}>
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