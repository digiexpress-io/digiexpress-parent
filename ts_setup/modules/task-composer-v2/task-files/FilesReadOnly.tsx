import React from 'react';
import { Box, Divider, Typography } from '@mui/material';
import { Description as DescriptionIcon } from '@mui/icons-material';
import { useIntl } from 'react-intl';

import { TaskApi, useTaskBackend } from '@dxs-ts/task-api';
import { TaskCardStyleDefinition } from '../task-card';
import { FilesReadOnlyRoot, useFilesReadOnlyClasses } from './useUtilityClasses';

const fiDate = new Intl.DateTimeFormat('fi-FI', {
  year: 'numeric', month: '2-digit', day: '2-digit',
  hour: '2-digit', minute: '2-digit'
});
const asDate = (v: any) => (v instanceof Date ? v : new Date(v));
const formatTs = (file: TaskApi.Attachment) =>
  fiDate.format(asDate((file as any).updated ?? (file as any).created ?? (file as any).createdAt));

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
            <Box className={classes.grow} />
            <Typography className={classes.timestamp}>
              {formatTs(file)}
            </Typography>
          </Box>
          <Divider />
        </div>
      ))}
    </FilesReadOnlyRoot>
  );
};
