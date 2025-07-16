import React from 'react';
import { Box, Divider, generateUtilityClass, List, ListItem, ListItemText, styled, Typography } from '@mui/material';
import CircleIcon from '@mui/icons-material/Circle';

import { TaskApi } from '@/api-task';
import { DateTime } from 'luxon';
import { TaskCardStyleDefinition } from './cardThemeConfig';
import composeClasses from '@mui/utils/composeClasses';

const formatAnyDateShort = (value: Date | string | undefined): string => {
  if (!value) {
    return '--';
  }

  const dateTime = value instanceof Date ? DateTime.fromJSDate(value) : DateTime.fromISO(value);
  return dateTime.setLocale('fi').toLocaleString(DateTime.DATE_SHORT);
};

export const TaskNotes: React.FC<{ task: TaskApi.Task, style: TaskCardStyleDefinition }> = ({ task, style }) => {
  const classes = useUtilityClasses();
  const internalComments = task.comments?.filter(c => !c.external) || [];

  if (!internalComments || internalComments.length === 0) {
    return <>No notes</>
  }

  const truncateText = (text: string, maxLength: number): string => {
    if (text.length <= maxLength) return text;
    return text.slice(0, maxLength) + '…';
  };


  return (
    <StyledTaskNotes className={classes.notesContainer}>
      {internalComments
        .slice(0, 3)
        .map(comment => (
          <Box key={comment.id}>
            <>
              <Box display='flex' alignItems='center'>
                <CircleIcon sx={{ fontSize: '7pt', mr: 1, color: 'primary.main' }} />
                <Typography component='div' sx={{ ...style.bodyTypography }} className={classes.noteBody}>
                  {`${truncateText(comment.commentText, 200)}`}
                </Typography>
              </Box>
              <Divider />

              <Box display='flex' alignItems='center' justifyContent='flex-end'>
                <Typography component='div' sx={{ ...style.bodyTypographySmall }} className={classes.noteAuthor}>
                  {`${comment.userName}` + " noted on " + `${formatAnyDateShort(comment.created)}`}
                </Typography>
              </Box>
            </>
          </Box>
        ))}
      {internalComments.length > 3 && (<Typography sx={{ ...style.bodyTypography }}>...{internalComments.length - 3} more...</Typography>
      )}
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

  '& .TaskNotes-noteBody': {
    fontWeight: 400,
    marginLeft: 1
  },
  '& .TaskNotes-noteAuthor': {
    textAlign: 'right',
    color: theme.palette.text.disabled
  }

}));

export const useUtilityClasses = () => {
  const slots = {
    notesContainer: ['notesContainer'],
    noteBody: ['noteBody'],
    noteAuthor: ['noteAuthor'],
    noteBackground: ['noteBackground']

  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};