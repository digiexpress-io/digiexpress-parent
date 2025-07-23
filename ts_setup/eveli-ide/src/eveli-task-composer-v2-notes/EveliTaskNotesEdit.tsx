import React from 'react';
import { Box, Divider, generateUtilityClass, styled, TextField, Typography } from '@mui/material';
import CircleIcon from '@mui/icons-material/Circle';
import EditIcon from '@mui/icons-material/Edit';

import { TaskApi } from '@/api-task';
import { DateTime } from 'luxon';
import composeClasses from '@mui/utils/composeClasses';

const formatAnyDateShort = (value: Date | string | undefined): string => {
  if (!value) {
    return '--';
  }

  const dateTime = value instanceof Date ? DateTime.fromJSDate(value) : DateTime.fromISO(value);
  return dateTime.setLocale('fi').toLocaleString(DateTime.DATE_SHORT);
};

export interface EveliTaskNotesProps {
  task: TaskApi.Task;
}

export const EveliTaskNotesEdit: React.FC<EveliTaskNotesProps> = ({ task }) => {
  const classes = useUtilityClasses();
  const internalComments = task.comments?.filter(c => !c.external) || [];

  if (!internalComments || internalComments.length === 0) {
    return <>No notes</>
  }

  return (
    <StyledTaskNotes className={classes.notesContainer}>
      <Box className={classes.messagesContainer}>

        {internalComments
          .map(comment => (
            <Box key={comment.id}>
              <Box display='flex' alignItems='center'>
                <CircleIcon sx={{ fontSize: '7pt', mr: 1, color: 'primary.main' }} />
                <Typography component='div' className={classes.noteBody}>
                  {comment.commentText}
                </Typography>
              </Box>
              <Divider />

              <Box display='flex' alignItems='center' justifyContent='flex-end'>
                <Typography component='div' className={classes.noteAuthor}>
                  {`${comment.userName}` + " noted on " + `${formatAnyDateShort(comment.created)}`}
                </Typography>
              </Box>
            </Box>
          ))}
      </Box>
      <Box className={classes.inputBox}>
        <Divider sx={{ my: 1 }} />
        <Box display='flex' alignItems='center'>
          <EditIcon color='primary' />
          <Typography fontWeight='bold'>Write a new note</Typography>
        </Box>
        <StyledTextField multiline rows={4} />

      </Box>
    </StyledTaskNotes>
  )
}


const MUI_NAME = 'TaskNotes';
const StyledTaskNotes = styled('div', {
  name: MUI_NAME,
  slot: 'Notes',
  overridesResolver: (_props, styles) => {
    return [
      styles.notesContainer
    ];
  },
})(({ theme }) => ({
  height: '100%',
  display: 'flex',
  flexDirection: 'column',
  overflow: 'hidden',
  padding: theme.spacing(2),

  '& .TaskNotes-messagesContainer': {
    flexGrow: 1,
    overflowY: 'auto',
    paddingRight: theme.spacing(1),
  },

  '& .TaskNotes-inputBox': {
    position: 'sticky',
    bottom: 0,
    backgroundColor: theme.palette.background.paper,
  },

  '& .TaskNotes-noteBody': {
    fontWeight: 400,
    marginLeft: 1
  },
  '& .TaskNotes-noteAuthor': {
    textAlign: 'right',
    color: theme.palette.text.disabled
  },
  '& .MuiSvgIcon-root': {
    //fontSize: '20pt',
    marginRight: theme.spacing(1),
    marginLeft: theme.spacing(1),
  },

}));

const StyledTextField = styled(TextField)(({ theme }) => ({
  width: '100%',
  '& .MuiInputBase-input': {
    height: '2.5rem',
    padding: '0 12px'
  },
}));


export const useUtilityClasses = () => {
  const slots = {
    notesContainer: ['notesContainer'],
    noteBody: ['noteBody'],
    noteAuthor: ['noteAuthor'],
    noteBackground: ['noteBackground'],
    messagesContainer: ['messagesContainer'],
    inputBox: ['inputBox']

  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};