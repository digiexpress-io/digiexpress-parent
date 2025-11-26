import React from "react";

import { Accordion, AccordionDetails, AccordionSummary, Badge, Box, Grid2, Typography } from "@mui/material";
import { ExpandMore as ExpandMoreIcon } from '@mui/icons-material';
import { DriveFileMoveOutlined as DriveFileMoveOutlinedIcon } from '@mui/icons-material';
import { FormattedMessage } from "react-intl";

import { TaskApi, TaskFeature, useTaskBackend } from '@dxs-ts/task-api';
import { StatusIndicator, UpsertOneFeedback } from "@dxs-ts/task-feedback";


import { TaskComments } from "../task-comments";
import { TaskAttachments } from "../task-attachments";
import { TaskTransfer, TaskTransferStatusIndicator } from "../task-transfer";

import { TaskCountIndicator } from "./TaskCountIndicator";



export const classes = {
  accordionSummary: {
    display: "flex",
    alignItems: 'center',
    "& .Mui-expanded": {
      marginBottom: - 1,
      marginTop: 0,
    }
  },
  accordionTitle: {
    fontWeight: 'bolder',
    width: "max-content",
    mr: 2
  },
  accordionDetails: {
    pt: 0,
  },
  spacer: { //TODO unify these styles with TaskClientMessages styles
    width: '17%',
    display: 'flex',
    justifyContent: 'space-between'
  },
};


export interface TaskBodyProps {
  task: TaskApi.Task;
  readOnly: boolean;
  onReload: () => Promise<void>;
}


export const TaskBody: React.FC<TaskBodyProps> = (props) => {
  const { task, readOnly, onReload } = props;
  const { id } = task;
  const [attachments, setAttachments] = React.useState<TaskApi.Attachment[]>([]);
  const backend = useTaskBackend();

  const isManual = task.keyWords?.includes('Manual');

  React.useEffect(() => {
    backend.persistence.findAllAttachments(id).then(setAttachments);
  }, [id]);

  function onFeedbackCancel() {
    backend.navigate.openOneTask(task.taskRef!);
  }


  return (
    <Grid2 container spacing={2} paddingLeft={2} paddingRight={2}>
      {isManual ? <></> : <TaskFeature id="CRM_MESSAGES">
        <Grid2 size={{ xs: 12 }}>
          <Accordion>
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              <Box sx={classes.spacer}>
                <Typography sx={classes.accordionTitle}><FormattedMessage id="externalComments" /></Typography>
                <TaskCountIndicator count={task.comments.filter(c => c.external).length} />
              </Box>
            </AccordionSummary>
            <AccordionDetails>
              <TaskComments task={task} isExternalThread={true} reload={onReload} />
            </AccordionDetails>
          </Accordion>
        </Grid2>
      </TaskFeature>}

      <TaskFeature id="TASK_FEEDBACK">
        <Grid2 size={{ xs: 12 }}>
          <Accordion>
            <AccordionSummary expandIcon={<ExpandMoreIcon />} sx={classes.accordionSummary}>
              <Box sx={classes.spacer}>
                <Typography sx={classes.accordionTitle}><FormattedMessage id="task.feedback.published" /></Typography>
                <StatusIndicator taskId={task.id} size='SMALL' />
              </Box>
            </AccordionSummary>
            <AccordionDetails sx={classes.accordionDetails}>
              <UpsertOneFeedback
                taskRef={task.taskRef!}
                reload={0}
                allowDelete={false} 
                onComplete={() => { }} 
                onCancel={onFeedbackCancel}
                onDelete={onFeedbackCancel}
              />
            </AccordionDetails>
          </Accordion>
        </Grid2>
      </TaskFeature>

      <Grid2 size={{ xs: 12 }}>
        <Accordion>
          <AccordionSummary expandIcon={<ExpandMoreIcon />} sx={classes.accordionSummary}>
            <Box sx={classes.spacer}>
              <Typography sx={classes.accordionTitle}><FormattedMessage id="attachmentView.title" /></Typography>
              <TaskCountIndicator count={attachments?.length} />
            </Box>
          </AccordionSummary>
          <AccordionDetails sx={classes.accordionDetails}>
            <TaskAttachments taskId={id} readonly={readOnly} attachments={attachments} setAttachments={setAttachments} />
          </AccordionDetails>
        </Accordion>
      </Grid2>

      <Grid2 size={{ xs: 12 }}>
        <Accordion>
          <AccordionSummary expandIcon={<ExpandMoreIcon />} sx={classes.accordionSummary}>
            <Box sx={classes.spacer}>
              <Typography sx={classes.accordionTitle}><FormattedMessage id="internalComments" /></Typography>
              <TaskCountIndicator count={task.comments.filter(c => !c.external).length} />
            </Box>
          </AccordionSummary>
          <AccordionDetails sx={classes.accordionDetails}>
            <TaskComments task={task} isExternalThread={false} reload={onReload} />
          </AccordionDetails>
        </Accordion>
      </Grid2>

      <TaskFeature id="TASK_TRANSFER">
        <Grid2 size={{ xs: 12 }}>
          <Accordion>
            <AccordionSummary expandIcon={<ExpandMoreIcon />} sx={classes.accordionSummary}>
              <Box sx={classes.spacer}>
                <Badge badgeContent={<TaskTransferStatusIndicator task={task} />}><DriveFileMoveOutlinedIcon /></Badge>
                <Typography sx={classes.accordionTitle}><FormattedMessage id="task.transfer.published" /></Typography>
              </Box>
            </AccordionSummary>
            <AccordionDetails sx={classes.accordionDetails}>
              <TaskTransfer task={task} onTransferComplete={onReload} />
            </AccordionDetails>
          </Accordion>
        </Grid2>
      </TaskFeature>
    </Grid2>
  );
}



