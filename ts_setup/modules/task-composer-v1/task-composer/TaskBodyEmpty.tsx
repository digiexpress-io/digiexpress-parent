import React from 'react';
import { Grid2, Typography, Paper } from '@mui/material';
import { InfoOutlined as InfoOutlinedIcon } from '@mui/icons-material';
import { FormattedMessage } from 'react-intl';

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


export const TaskBodyEmpty: React.FC<{  }> = ({  }) => {
  return (<>
    <Grid2 size={{ xs: 12 }}>
      <NewTaskAccordionMsg id='task.comments.external.createTask' />
    </Grid2>
    <Grid2 size={{ xs: 12 }}>
      <NewTaskAccordionMsg id='task.attachments.createTask' />
    </Grid2>
    <Grid2 size={{ xs: 12 }}>
      <NewTaskAccordionMsg id='task.comments.internal.createTask' />
    </Grid2>
  </>);
};

  
