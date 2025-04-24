import React from "react";

import { Accordion, AccordionDetails, AccordionSummary, Badge, Grid2, Typography } from "@mui/material";
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import ChatBubbleOutlineIcon from '@mui/icons-material/ChatBubbleOutline';
import SupportAgentIcon from '@mui/icons-material/SupportAgent';
import AttachmentIcon from '@mui/icons-material/Attachment';

import { FormattedMessage } from "react-intl";

import { useFetch } from "@dxs-ts/eveli-fetch";
import { TaskApi } from "@/api-task";
import { EveliTaskComments } from "@/eveli-task-comments";
import { StatusIndicator, UpsertOneFeedback } from "@/eveli-task-feedback";
import { EveliTaskAttachments } from "@/eveli-task-attachments";



export const classes = {
  accordionSummary: {
    display: "flex",
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
};


export interface EveliTaskBodyProps {
  task: TaskApi.Task;
  readOnly: boolean;
  onReload: () => Promise<void>;
}


export const EveliTaskBody: React.FC<EveliTaskBodyProps> = (props) => {
  const { task, readOnly, onReload } = props;
  const { id, features } = task;
  const isFeedbackEnabled = features?.includes('feedback') ;
  const [attachments, setAttachments] = React.useState<TaskApi.Attachment[]>([]);
  const { loadAttachments } = useFetch('worker/rest/api/tasks/$taskId/files.GET', {});
  
  React.useEffect(() => {
    loadAttachments(id).then(setAttachments);
  }, [id]);  

  return (
    <Grid2 container spacing={2}>
      <Grid2 size={{ xs: 12 }}>
        <Accordion>
          <AccordionSummary expandIcon={<ExpandMoreIcon />} sx={classes.accordionSummary} >
            <Typography sx={classes.accordionTitle}><FormattedMessage id="externalComments" /></Typography>
            <Badge badgeContent={task.comments.filter(c => c.external).length} color="warning"><ChatBubbleOutlineIcon /></Badge>
          </AccordionSummary>
          <AccordionDetails sx={classes.accordionDetails}>
            <EveliTaskComments task={task} isExternalThread={true} reload={onReload}/>
          </AccordionDetails>
        </Accordion>
      </Grid2>

      {isFeedbackEnabled && (
        <Grid2 size={{ xs: 12 }}>
          <Accordion>
            <AccordionSummary expandIcon={<ExpandMoreIcon />} sx={classes.accordionSummary} >
              <Typography sx={classes.accordionTitle}><FormattedMessage id="task.feedback.published" /></Typography>
              <Badge badgeContent={<StatusIndicator size="SMALL" taskId={task.id} />}><SupportAgentIcon /></Badge>
            </AccordionSummary>
            <AccordionDetails sx={classes.accordionDetails}>
              <UpsertOneFeedback taskId={task.id} onComplete={() => {}} reload={0} />
            </AccordionDetails>
          </Accordion>
        </Grid2>
      )}

      <Grid2 size={{ xs: 12 }}>
        <Accordion>
          <AccordionSummary expandIcon={<ExpandMoreIcon />} sx={classes.accordionSummary}>
            <Typography sx={classes.accordionTitle}><FormattedMessage id="attachmentView.title" /></Typography>
            <Badge badgeContent={attachments?.length} color='secondary'><AttachmentIcon /></Badge>
          </AccordionSummary>
          <AccordionDetails sx={classes.accordionDetails}>
            <EveliTaskAttachments taskId={id} readonly={readOnly} attachments={attachments} setAttachments={setAttachments} />
          </AccordionDetails>
        </Accordion>
      </Grid2>
      
      <Grid2 size={{ xs: 12 }}>
        <Accordion>
          <AccordionSummary expandIcon={<ExpandMoreIcon />} sx={classes.accordionSummary}>
            <Typography sx={classes.accordionTitle}><FormattedMessage id="internalComments" /></Typography>
            <Badge badgeContent={task.comments.filter(c => !c.external).length} color="primary"><ChatBubbleOutlineIcon /></Badge>
          </AccordionSummary>
          <AccordionDetails sx={classes.accordionDetails}>
            <EveliTaskComments task={task} isExternalThread={false} isThreaded reload={onReload}/>
          </AccordionDetails>
        </Accordion>
      </Grid2>
    </Grid2>
  );
}



