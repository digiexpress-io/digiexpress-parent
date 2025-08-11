import React from 'react';
import { Box, Divider, generateUtilityClass, styled, Typography } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import DescriptionIcon from '@mui/icons-material/Description';
import { useIntl } from 'react-intl';


import { TaskApi } from '@dxs-ts/eveli-api';
import { TaskCardStyleDefinition } from '../eveli-task-composer-v2-task-card';

const files = [
  { id: 1, name: 'report.pdf', uploadedAt: '11.09.2025' },
  { id: 2, name: 'design.png', uploadedAt: '10.09.2025' },
  { id: 3, name: 'invoice.docx', uploadedAt: '09.09.2025' },
];

export interface FilesReadOnlyProps {
  task: TaskApi.Task;
  style: TaskCardStyleDefinition
}

export const FilesReadOnly: React.FC<FilesReadOnlyProps> = ({ task, style }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  if (!files.length || files.length === 0) {
    return <>{intl.formatMessage({ id: 'task.file.none', defaultMessage: 'No files found' })}</>
  }
  return (
    <TaskFiles className={classes.root} style={style}>
      {files.map((file) => (<div key={file.id}>
        <Box className={classes.file}>
          <DescriptionIcon className={classes.fileIcon} />
          <Typography sx={{ ...style }}>{file.name}</Typography>
          <Box flexGrow={1} />
          <Typography sx={{ ...style }}>{file.uploadedAt}</Typography>
        </Box>
        <Divider />
      </div>
      ))}
    </TaskFiles>
  )
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
    '& .TaskFiles-file': {
      padding: theme.spacing(1),
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
