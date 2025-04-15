import React from 'react';
import {
    Grid2, Accordion, AccordionSummary, AccordionDetails, Badge, Typography, Paper, Box
} from '@mui/material';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import ChatBubbleOutlineIcon from '@mui/icons-material/ChatBubbleOutline';
import InfoOutlinedIcon from '@mui/icons-material/InfoOutlined';
import SupportAgentIcon from '@mui/icons-material/SupportAgent';
import { useSnackbar } from 'notistack';
import { FormattedMessage } from 'react-intl';

import { EveliTaskComments } from '../eveli-task-comments';
import { UpsertOneFeedback, StatusIndicator } from '../eveli-task-feedback';

import { TaskApi } from '../api-task';
import { classes } from './useMuiClasses';

type Classes = typeof classes;

type CommonProps = {
  task: TaskApi.Task;
  comments: TaskApi.Comment[];
  reloadComments: () => void;
  externalThreads?: boolean;
  classes: Classes;
};

const NewTaskAccordionMsg: React.FC<{ id: string }> = ({ id }) => {
  return (
    <Paper sx={{
      p: 2,
      display: 'flex',
      alignItems: 'center',
      backgroundColor: theme => theme.palette.action.hover
    }}>
      <InfoOutlinedIcon sx={{ mr: 1, color: theme => theme.palette.text.secondary }} />
      <Typography variant="subtitle2">
        <FormattedMessage id={id} />
      </Typography>
    </Paper>
  );
};

export const ExternalCommentsAccordion: React.FC<CommonProps> = ({
  task,
  comments,
  reloadComments,
  externalThreads,
  classes
}) => {
  const { enqueueSnackbar } = useSnackbar();

  if (!task.id || !externalThreads) {
    return (
      <Grid2 size={{ xs: 12 }}>
        <NewTaskAccordionMsg id='task.comments.external.createTask' />
      </Grid2>
    );
  }

  const handleCommentPosted = () => {
    enqueueSnackbar(<FormattedMessage id="task.comments.external.added" />, { variant: 'success' });
    reloadComments();
  };

  return (
    <Grid2 size={{ xs: 12 }}>
      <Accordion>
        <AccordionSummary
          expandIcon={<ExpandMoreIcon />}
          sx={classes.accordionSummary}
        >
          <Typography sx={classes.accordionTitle}>
            <FormattedMessage id="externalComments" />
          </Typography>
          <Badge badgeContent={comments.filter(c => c.external).length} color="warning">
            <ChatBubbleOutlineIcon />
          </Badge>
        </AccordionSummary>
        <AccordionDetails sx={classes.accordionDetails}>
          <EveliTaskComments
            task={task}
            isExternalThread={true}
            comments={comments}
            loadData={reloadComments}
            isThreaded={false}
            onCommentPosted={handleCommentPosted}
          />
        </AccordionDetails>
      </Accordion>
    </Grid2>
  );
};

export const PublishedReplyAccordion: React.FC<CommonProps> = ({
  task,
  comments,
  reloadComments,
  externalThreads,
  classes
}) => {
  const { enqueueSnackbar } = useSnackbar();

  if (!task.id || !externalThreads) {
    return (
      <Grid2 size={{ xs: 12 }}>
        <NewTaskAccordionMsg id='task.comments.external.createTask' />
      </Grid2>
    );
  }

  const handleFeedbackSaved = () => {
    enqueueSnackbar(<FormattedMessage id="task.feedback.publishedSaved" />, { variant: 'success' });
    reloadComments();
  };

  return (
    <Grid2 size={{ xs: 12 }}>
      <Accordion>
        <AccordionSummary
          expandIcon={<ExpandMoreIcon />}
          sx={classes.accordionSummary}
        >
          <Typography sx={classes.accordionTitle}>
            <FormattedMessage id="task.feedback.published" />
          </Typography>
          <Badge badgeContent={<StatusIndicator size="SMALL" taskId={task.id + ""} />}>
            <SupportAgentIcon />
          </Badge>
        </AccordionSummary>
        <AccordionDetails sx={classes.accordionDetails}>
          <UpsertOneFeedback
            taskId={task.id + ""}
            onComplete={handleFeedbackSaved}
            reload={comments.length ?? 0}
          />
        </AccordionDetails>
      </Accordion>
    </Grid2>
  );
};

export const InternalCommentsAccordion: React.FC<CommonProps> = ({
    task,
    comments,
    reloadComments,
    externalThreads,
    classes
  }) => {
    const { enqueueSnackbar } = useSnackbar();
  
    if (!task.id) {
      return (
        <Grid2 size={{ xs: 12 }}>
          <NewTaskAccordionMsg id='task.comments.internal.createTask' />
        </Grid2>
      );
    }
  
    const handleCommentPosted = () => {
      enqueueSnackbar(<FormattedMessage id="task.comments.internal.added" />, {
        variant: 'success'
      });
      reloadComments();
    };
  
    return (
      <Grid2 size={{ xs: 12 }}>
        <Accordion>
          <AccordionSummary
            expandIcon={<ExpandMoreIcon />}
            sx={classes.accordionSummary}
          >
            <Typography sx={classes.accordionTitle}>
              <FormattedMessage id="internalComments" />
            </Typography>
            <Badge badgeContent={comments.filter(c => !c.external).length} color="primary">
              <ChatBubbleOutlineIcon />
            </Badge>
          </AccordionSummary>
          <AccordionDetails sx={classes.accordionDetails}>
            <EveliTaskComments
              task={task}
              isExternalThread={false}
              comments={comments}
              loadData={reloadComments}
              isThreaded={true}
              onCommentPosted={handleCommentPosted}
            />
          </AccordionDetails>
        </Accordion>
      </Grid2>
    );
  };

 
  
  
