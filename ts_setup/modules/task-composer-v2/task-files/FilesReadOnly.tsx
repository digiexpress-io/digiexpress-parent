import React from 'react';
import { Box, Divider, Typography } from '@mui/material';
import { Description as DescriptionIcon } from '@mui/icons-material';
import { useIntl } from 'react-intl';

import { TaskApi, useTaskBackend } from '@dxs-ts/task-api';
import { TaskCardStyleDefinition } from '../task-card';
import { FilesReadOnlyRoot, useFilesReadOnlyClasses } from './useUtilityClasses';

export interface FilesReadOnlyProps {
  task: TaskApi.Task;
  style: TaskCardStyleDefinition;
}

export const FilesReadOnly: React.FC<FilesReadOnlyProps> = ({ task, style }) => {
  const intl = useIntl();
  const classes = useFilesReadOnlyClasses();
  const backend = useTaskBackend();

  const [attachments, setAttachments] = React.useState<TaskApi.Attachment[]>([]);
  React.useEffect(() => {
    backend.persistence.findAllAttachments(task.id).then(setAttachments);
  }, [task.id, backend.persistence]);

  if (!attachments.length) {
    return (
      <Typography className={classes.empty}>
        {intl.formatMessage({ id: 'task.file.none', defaultMessage: 'No files found' })}
      </Typography>
    );
  }

  return (
    <FilesReadOnlyRoot className={classes.root} style={style}>
      {attachments.map((file) => (
        <div key={file.name}>
          <Box className={classes.file}>
            <DescriptionIcon className={classes.fileIcon} />
            <Typography>{file.name}</Typography>
            <Box flexGrow={1} /* or className={classes.grow} */ />
            <Typography>{file.updated.toString()}</Typography>
          </Box>
          <Divider />
        </div>
      ))}
    </FilesReadOnlyRoot>
  );
};
