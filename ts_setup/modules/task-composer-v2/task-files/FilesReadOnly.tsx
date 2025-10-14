import React from 'react';
import { Box, Divider, generateUtilityClass, styled, Typography } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { Description as DescriptionIcon } from '@mui/icons-material';
import { useIntl } from 'react-intl';


import { TaskApi, useTaskBackend } from '@dxs-ts/task-api';
import { TaskCardStyleDefinition } from '../task-card';


export interface FilesReadOnlyProps {
  task: TaskApi.Task;
  style: TaskCardStyleDefinition;
}

export const FilesReadOnly: React.FC<FilesReadOnlyProps> = ({ task, style }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const backend = useTaskBackend();
  
  const [attachments, setAttachments] = React.useState<TaskApi.Attachment[]>([]);
  React.useEffect(() => {
    backend.persistence.findAllAttachments(task.id).then(setAttachments);
  }, [task.id]);



  if (!attachments.length || attachments.length === 0) {
    return (
      <Typography sx={{ ...style.bodyTypography }} color='error'>
        {intl.formatMessage({ id: 'task.file.none', defaultMessage: 'No files found' })}
      </Typography>)
  }
  return (
    <TaskFiles className={classes.root} style={style}>
      {attachments.map((file) => (<div key={file.name}>
        <Box className={classes.file}>
          <DescriptionIcon className={classes.fileIcon} />
          <Typography sx={{ ...style.bodyTypography }}>{file.name}</Typography>
          <Box flexGrow={1} />
          <Typography sx={{ ...style.bodyTypography }}>{file.updated.toString()}</Typography>
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
