import React from 'react';
import { alpha, Box, generateUtilityClass, IconButton, styled, Typography } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import DescriptionIcon from '@mui/icons-material/Description';
import DeleteIcon from '@mui/icons-material/Delete';
import DownloadIcon from '@mui/icons-material/Download';
import { useIntl } from 'react-intl';

import { TaskApi } from '@dxs-ts/task-api';



export interface FilesEditorProps {
  task: TaskApi.Task;
  attachments: TaskApi.Attachment[];
  onDownload: (data: TaskApi.Attachment | TaskApi.Attachment[]) => void;
  onDelete: (data: TaskApi.Attachment | TaskApi.Attachment[]) => void;
}

export const FilesEditor: React.FC<FilesEditorProps> = ({ task, onDownload, onDelete, attachments }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  return (<>

    <StyledFilesEditor className={classes.root}>
      {attachments.length ? (<>

        <Box className={classes.headerRow}>
          <Box width='70%'>{intl.formatMessage({ id: 'task.file.fileName', defaultMessage: 'File name' })}</Box>
          <Box width='25%'>{intl.formatMessage({ id: 'task.file.uploadDate', defaultMessage: 'Upload date' })}</Box>
        </Box>

        {attachments.map(file => (
          <Box key={file.name} className={classes.file}>
            <Box width='70%' display="flex" alignItems="center" gap={1}>
              <DescriptionIcon color="primary" />
              <Typography>{file.name}</Typography>
            </Box>
            <Box width='20%'>
              <Typography>{file.created.toString()}</Typography>
            </Box>
            <Box width='5%'>
              <IconButton className={classes.downloadIcon} onClick={() => onDownload(file)}>
                <DownloadIcon />
              </IconButton>
            </Box>
            <Box width='5%'>
              <IconButton className={classes.deleteIcon} onClick={() => onDelete(file)}>
                <DeleteIcon />
              </IconButton>
            </Box>
          </Box>
        ))}
      </>) : (
        <Typography className={classes.noFiles}>
          {intl.formatMessage({ id: 'task.file.attachments.none', defaultMessage: 'No files for this task' })}
        </Typography>
      )}

    </StyledFilesEditor>
  </>
  );
};



const MUI_NAME = 'FilesEditor';
const StyledFilesEditor = styled('div', {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },

})(({ theme }) => {

  return {
    width: '100%',

    '& .FilesEditor-headerRow': {
      display: 'flex',
      padding: theme.spacing(1),
      fontWeight: 'bold',
      borderBottom: `1px solid ${theme.palette.divider}`
    },
    '& .FilesEditor-file': {
      display: 'flex',
      alignItems: 'center',
      padding: theme.spacing(1),
      borderBottom: `1px solid ${alpha(theme.palette.divider, 0.5)}`
    },
    '& .FilesEditor-noFiles': {
      padding: theme.spacing(1),
      color: theme.palette.error.main
    },
    '& .FilesEditor-fileIcon': {
      color: theme.palette.primary.main
    },
    '& .FilesEditor-deleteIcon': {
      color: theme.palette.error.main
    },
    '& .FilesEditor-downloadIcon': {
      color: theme.palette.primary.main
    }
  };
})


export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    headerRow: ['headerRow'],
    file: ['file'],
    noFiles: ['noFiles'],
    fileIcon: ['fileIcon'],
    deleteIcon: ['deleteIcon'],
    downloadIcon: ['downloadIcon']

  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
