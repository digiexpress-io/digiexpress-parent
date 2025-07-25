import React from 'react';
import { Alert, alpha, Box, Divider, generateUtilityClass, styled, TextField, Typography } from '@mui/material';
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


  return (
    <StyledTaskNotes className={classes.notesContainer}>
      <Box className={classes.messagesContainer}>

        {!internalComments || internalComments.length === 0 && <Typography variant='body2' textAlign='center'>No notes yet</Typography>}

        {internalComments.map(comment => (
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
        <Box className={classes.inputBoxTitle}>
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

  '& .TaskNotes-messagesContainer': {
    flexGrow: 1,
    overflowY: 'auto',
    padding: theme.spacing(4),
    margin: theme.spacing(2),
    border: `1px solid ${theme.palette.divider}`,
    borderRadius: theme.spacing(0.5),
    boxShadow: `0 4px 12px rgba(0, 0, 0, 0.05)`,
  },

  '& .TaskNotes-inputBoxTitle': {
    display: 'flex',
    alignItems: 'center',
    backgroundColor: alpha(theme.palette.primary.main, 0.1),
    borderRadius: `4px 4px 0 0`,
    border: `1px solid ${theme.palette.divider}`,
    borderBottom: 'none',
    padding: theme.spacing(1),
  },

  '& .TaskNotes-inputBox': {
    position: 'sticky',
    bottom: 0,
    paddingLeft: theme.spacing(2),
    paddingRight: theme.spacing(2),
  },

  '& .TaskNotes-noteBody': {
    fontWeight: 400,
  },
  '& .TaskNotes-noteAuthor': {
    textAlign: 'right',
    color: theme.palette.text.disabled
  },
  '& .MuiSvgIcon-root': {
    marginRight: theme.spacing(1),
  },

}));

const StyledTextField = styled(TextField)(({ theme }) => ({
  width: '100%',
  marginTop: 0,
  '& .MuiOutlinedInput-root': {
    borderTopLeftRadius: 0,
    borderTopRightRadius: 0,
  },
  '& .MuiInputBase-input': {
    height: '2.5rem',
    padding: '0 12px',
  }
}));


export const useUtilityClasses = () => {
  const slots = {
    notesContainer: ['notesContainer'],
    noteBody: ['noteBody'],
    noteAuthor: ['noteAuthor'],
    noteBackground: ['noteBackground'],
    messagesContainer: ['messagesContainer'],
    inputBox: ['inputBox'],
    inputBoxTitle: ['inputBoxTitle']

  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};