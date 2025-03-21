import React, { useEffect, useState } from 'react';
import { Typography, Accordion, AccordionSummary, AccordionDetails, Badge } from '@mui/material';

import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import AttachmentIcon from '@mui/icons-material/Attachment';

import { FormattedMessage } from 'react-intl';
import { useFetch } from '@dxs-ts/eveli-fetch';

import { TaskApi } from '../api-task';
import { EveliTaskAttachments } from '../eveli-task-attachments';
import { useMuiClasses } from './useMuiClasses';



export const AttachmentTableWrapper: React.FC<{ editTask: TaskApi.Task, readonly: boolean }> = ({ editTask, readonly }) => {
  const taskId = editTask.id;
  const [attachments, setAttachments] = useState<TaskApi.Attachment[]>([]);
  const { loadAttachments } = useFetch('worker/rest/api/tasks/$taskId/files.GET', {});
  const { classes } = useMuiClasses();


  useEffect(() => {
    if (taskId) {
      loadAttachments(taskId)
        .then((result: TaskApi.Attachment[]) => {
          setAttachments(result);
        });
    }
    else {
      setAttachments([]);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [taskId]);

  return (
    <Accordion>
      <AccordionSummary
        expandIcon={<ExpandMoreIcon />}
        aria-controls="panel1bh-content"
        id="panel1bh-header"
        sx={classes.accordionSummary}
      >
        <Typography sx={classes.accordionTitle}>
          <FormattedMessage id="attachmentView.title" />
        </Typography>
        <Badge badgeContent={attachments?.length} color='secondary'>
          <AttachmentIcon />
        </Badge>
      </AccordionSummary>
      <AccordionDetails sx={classes.accordionDetails}>
        {!!editTask.id &&
          <EveliTaskAttachments taskId={editTask.id} readonly={readonly} attachments={attachments} setAttachments={setAttachments} />
        }
      </AccordionDetails>
    </Accordion>
  )
}
