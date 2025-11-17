import React from 'react';
import { Box, IconButton, Typography } from '@mui/material';
import { Description as DescriptionIcon } from '@mui/icons-material';
import { Delete as DeleteIcon } from '@mui/icons-material';
import { Download as DownloadIcon } from '@mui/icons-material';
import { useIntl } from 'react-intl';

import { TaskApi } from '@dxs-ts/task-api';
import { FilesEditorRoot, useFilesEditorClasses } from './useUtilityClasses';

const fiDate = new Intl.DateTimeFormat('fi-FI', {
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit',
});

const asDate = (v: any) => (v instanceof Date ? v : new Date(v));

export interface FilesEditorProps {
  task: TaskApi.Task;
  attachments: TaskApi.Attachment[];
  onDownload: (data: TaskApi.Attachment | TaskApi.Attachment[]) => void;
  onDelete: (data: TaskApi.Attachment | TaskApi.Attachment[]) => void;
}

export const FilesEditor: React.FC<FilesEditorProps> = ({
  task,
  onDownload,
  onDelete,
  attachments,
}) => {
  const intl = useIntl();
  const classes = useFilesEditorClasses();

  return (
    <FilesEditorRoot className={classes.root}>
      {attachments.length ? (
        <>
          <Box className={classes.headerRow}>
            <Box className={classes.colName}>
              {intl.formatMessage({ id: 'task.file.fileName' })}
            </Box>
            <Box className={classes.colDate}>
              {intl.formatMessage({ id: 'task.file.uploadDate' })}
            </Box>
            <Box className={classes.colAction} />
            <Box className={classes.colAction} />
          </Box>

          {attachments.map((file) => (
            <Box key={file.name} className={classes.fileRow}>
              <Box className={classes.colNameRow}>
                <DescriptionIcon className={classes.fileIcon} />
                <Typography className={classes.fileName} title={file.name}>
                  {file.name}
                </Typography>
              </Box>

              <Box className={classes.colDateRow}>
                <Typography className={classes.noWrapEllipsis}>
                  {fiDate.format(asDate((file as any).created))}
                </Typography>
              </Box>

              <Box className={classes.colAction}>
                <IconButton className={classes.downloadIcon} onClick={() => onDownload(file)}>
                  <DownloadIcon />
                </IconButton>
              </Box>

              <Box className={classes.colAction}>
                <IconButton className={classes.deleteIcon} onClick={() => onDelete(file)}>
                  <DeleteIcon />
                </IconButton>
              </Box>
            </Box>
          ))}
        </>
      ) : (
          <Typography className={classes.noFiles}>{intl.formatMessage({ id: 'task.files.none' })}</Typography>
      )}
    </FilesEditorRoot>
  );
};
