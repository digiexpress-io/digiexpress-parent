import React from 'react';
import { alpha, Box, darken, generateUtilityClass, IconButton, styled, Typography } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import DescriptionIcon from '@mui/icons-material/Description';
import DeleteIcon from '@mui/icons-material/Delete';

import { TaskApi } from '@/api-task';
import { TaskCardStyleDefinition } from './cardThemeConfig';



export interface EveliTaskFilesProps {
  task: TaskApi.Task;
  style: TaskCardStyleDefinition
}

export const EveliTaskFiles: React.FC<EveliTaskFilesProps> = ({ task, style }) => {
  const classes = useUtilityClasses();

  return (
    <TaskFiles className={classes.root} style={style}>
      <Box className={classes.file}>
        <DescriptionIcon className={classes.fileIcon} />
        <Typography sx={{ ...style }}>file-name.jpg</Typography>
        <Box flexGrow={1} />
        <IconButton color='error'><DeleteIcon className={classes.deleteIcon} /></IconButton>
      </Box>
    </TaskFiles>)
}



const MUI_NAME = 'TaskFiles';
const TaskFiles = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },

})<{ style: TaskCardStyleDefinition }>(({ theme, style }) => {

  return {
    display: "flex",
    alignItems: "center",

    '& .TaskFiles-file': {
      padding: theme.spacing(1),
      border: `1px solid ${theme.palette.divider}`,
      backgroundColor: alpha(theme.palette.divider, 0.3),
      ':hover': {
        border: `1px solid ${darken(theme.palette.divider, 0.2)}`,
      },
      borderRadius: theme.spacing(2),
      display: 'flex',
      alignItems: 'center',
      width: '100%',
      '.MuiTypography-root': {
        ...style.bodyTypography
      }
    },
    '& .TaskFiles-fileIcon': {
      marginRight: theme.spacing(1),
      color: theme.palette.primary.main
    },
    '& .TaskFiles-deleteIcon': {
      color: theme.palette.error.main
    }


  };
})


export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    file: ['file'],
    fileIcon: ['fileIcon'],
    deleteIcon: ['deleteIcon']

  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
