import React from "react";

import { Accordion, AccordionDetails, AccordionSummary, Badge, Box, Grid2, ThemeProvider, Typography } from "@mui/material";
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';

import DriveFileMoveOutlinedIcon from '@mui/icons-material/DriveFileMoveOutlined';

import { FormattedMessage } from "react-intl";

import { useFetch } from "@dxs-ts/eveli-fetch";
import { TaskApi } from "@/api-task";
import { EveliTaskComments } from "@/eveli-task-comments";
import { EveliTaskAttachments } from "@/eveli-task-attachments";
import { EveliTaskFeature } from "@/eveli-task-feature";
import { EveliTaskTransfer, EveliTaskTransferStatusIndicator } from "@/eveli-task-transfer";
import { StatusIndicator, UpsertOneFeedback } from "@/eveli-task-feedback";
import { EveliTaskCountIndicator } from "./EveliTaskCountIndicator";



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
  spacer: { //TODO unify these styles with EveliTaskClientMessages styles
    width: '17%',
    display: 'flex',
    justifyContent: 'space-between'
  },
};


export interface EveliTaskBodyProps {
  task: TaskApi.Task;
  readOnly: boolean;
  onReload: () => Promise<void>;
}


export const EveliTaskBody: React.FC<EveliTaskBodyProps> = (props) => {
  const { task, readOnly, onReload } = props;
  const { id } = task;
  const [attachments, setAttachments] = React.useState<TaskApi.Attachment[]>([]);
  const { loadAttachments } = useFetch('worker/rest/api/tasks/$taskId/files.GET', {});

  React.useEffect(() => {
    loadAttachments(id).then(setAttachments);
  }, [id]);


  return (
    <Grid2 container spacing={2} paddingLeft={2} paddingRight={2}>
      <EveliTaskFeature id="CRM_MESSAGES">
        <Grid2 size={{ xs: 12 }}>
          <Accordion>
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              <Box sx={classes.spacer}>
                <Typography sx={classes.accordionTitle}><FormattedMessage id="externalComments" /></Typography>
                <EveliTaskCountIndicator count={task.comments.filter(c => c.external).length} />
              </Box>
            </AccordionSummary>
            <AccordionDetails>
              <EveliTaskComments task={task} isExternalThread={true} reload={onReload} />
            </AccordionDetails>
          </Accordion>
        </Grid2>
      </EveliTaskFeature>

      <EveliTaskFeature id="TASK_FEEDBACK">
        <Grid2 size={{ xs: 12 }}>
          <Accordion>
            <AccordionSummary expandIcon={<ExpandMoreIcon />} sx={classes.accordionSummary}>
              <Box sx={classes.spacer}>
                <Typography sx={classes.accordionTitle}><FormattedMessage id="task.feedback.published" /></Typography>
                <StatusIndicator taskId={task.id} size='SMALL' />
              </Box>
            </AccordionSummary>
            <AccordionDetails sx={classes.accordionDetails}>
              <UpsertOneFeedback taskId={task.id} onComplete={() => { }} reload={0} />
            </AccordionDetails>
          </Accordion>
        </Grid2>
      </EveliTaskFeature>

      <Grid2 size={{ xs: 12 }}>
        <Accordion>
          <AccordionSummary expandIcon={<ExpandMoreIcon />} sx={classes.accordionSummary}>
            <Box sx={classes.spacer}>
              <Typography sx={classes.accordionTitle}><FormattedMessage id="attachmentView.title" /></Typography>
              <EveliTaskCountIndicator count={attachments?.length} />
            </Box>
          </AccordionSummary>
          <AccordionDetails sx={classes.accordionDetails}>
            <EveliTaskAttachments taskId={id} readonly={readOnly} attachments={attachments} setAttachments={setAttachments} />
          </AccordionDetails>
        </Accordion>
      </Grid2>

      <Grid2 size={{ xs: 12 }}>
        <Accordion>
          <AccordionSummary expandIcon={<ExpandMoreIcon />} sx={classes.accordionSummary}>
            <Box sx={classes.spacer}>
              <Typography sx={classes.accordionTitle}><FormattedMessage id="internalComments" /></Typography>
              <EveliTaskCountIndicator count={task.comments.filter(c => !c.external).length} />
            </Box>
          </AccordionSummary>
          <AccordionDetails sx={classes.accordionDetails}>
            <EveliTaskComments task={task} isExternalThread={false} reload={onReload} />
          </AccordionDetails>
        </Accordion>
      </Grid2>

      <EveliTaskFeature id="TASK_TRANSFER">
        <Grid2 size={{ xs: 12 }}>
          <Accordion>
            <AccordionSummary expandIcon={<ExpandMoreIcon />} sx={classes.accordionSummary}>
              <Box sx={classes.spacer}>
                <Badge badgeContent={<EveliTaskTransferStatusIndicator task={task} />}><DriveFileMoveOutlinedIcon /></Badge>
                <Typography sx={classes.accordionTitle}><FormattedMessage id="task.transfer.published" /></Typography>
              </Box>
            </AccordionSummary>
            <AccordionDetails sx={classes.accordionDetails}>
              <EveliTaskTransfer task={task} onTransferComplete={onReload} />
            </AccordionDetails>
          </Accordion>
        </Grid2>
      </EveliTaskFeature>
    </Grid2>
  );
}



