import React from 'react';
import { Button, Stack } from '@mui/material';
import ArrowRightIcon from '@mui/icons-material/ArrowRight';
import { FormattedMessage } from 'react-intl';
import { TaskApi } from '@/burger';


export type TaskLinkProps = {
  link: TaskApi.TaskLink
  taskId?: string
}

export interface TaskLinkOpenCallback {
  (link: TaskApi.TaskLink):void
}
export interface TaskLinkPdfCallback {
  (link: TaskApi.TaskLink, taskId: string):void
}

type Props = {
  openCallback: TaskLinkOpenCallback
  pdfCallback: TaskLinkPdfCallback
}
export const TaskLinkComponent:React.FC<Props & TaskLinkProps> = (props) => {
  const link = props.link;
  const taskId = props.taskId;
  const pdfCallback = props.pdfCallback;
  return (
    <Stack direction='row' spacing={2}>
      <Button
        onClick={()=>props.openCallback(link)}
        size='small'
        color='secondary'
        variant='contained'
        sx={{borderRadius: 1}}
        endIcon={<ArrowRightIcon/>}
      >
        <FormattedMessage id='taskLink.button.open' />
      </Button>
      { taskId && (
        <Button
          onClick={()=>pdfCallback(link, taskId)}
          size='small'
          color='secondary'
          variant='contained'
          sx={{borderRadius: 1}}
          endIcon={<ArrowRightIcon/>}
        >
          <FormattedMessage id='taskLink.pdf.open' />
        </Button>
      )}
    </Stack>
  )
}

