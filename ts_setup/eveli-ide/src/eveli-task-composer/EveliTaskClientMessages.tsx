import React from 'react';
import { Accordion, AccordionDetails, AccordionSummary, Badge, Typography } from '@mui/material';
import ChatBubbleOutlineIcon from '@mui/icons-material/ChatBubbleOutline';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';

import { FormattedMessage } from 'react-intl';

import { TaskApi } from '@/api-task';
import { EveliTaskComments } from '@/eveli-task-comments';

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

export const EveliTaskClientMessages: React.FC<{ task: TaskApi.Task, onReload: () => Promise<void> }> = ({ task, onReload }) => {

  return (
    <Accordion>
      <AccordionSummary expandIcon={<ExpandMoreIcon />} sx={classes.accordionSummary} >
        <Typography sx={classes.accordionTitle}><FormattedMessage id="externalComments" /></Typography>
        <Badge badgeContent={task.comments.filter(c => c.external).length} color="warning"><ChatBubbleOutlineIcon /></Badge>
      </AccordionSummary>
      <AccordionDetails sx={classes.accordionDetails}>
        <EveliTaskComments task={task} isExternalThread={true} reload={onReload} />
      </AccordionDetails>
    </Accordion>
  )
}