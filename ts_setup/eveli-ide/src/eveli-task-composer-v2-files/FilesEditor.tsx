import React from 'react';
import { alpha, Box, generateUtilityClass, IconButton, styled, Typography } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import DescriptionIcon from '@mui/icons-material/Description';
import DeleteIcon from '@mui/icons-material/Delete';
import { useIntl } from 'react-intl';

import { TaskApi } from '@/api-task';

const files = [
  { id: 1, name: 'report.pdf', uploadedAt: '11.09.2025' },
  { id: 2, name: 'design.png', uploadedAt: '10.09.2025' },
  { id: 3, name: 'invoice.docx', uploadedAt: '09.09.2025' },
];

export interface FilesEditorProps {
  task: TaskApi.Task;
}

export const FilesEditor: React.FC<FilesEditorProps> = ({ task }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();

  return (
    <StyledFilesEditor className={classes.root}>
      <Box className={classes.headerRow}>
        <Box width='70%'>{intl.formatMessage({ id: 'task.file.fileName', defaultMessage: 'File name' })}</Box>
        <Box width='25%'>{intl.formatMessage({ id: 'task.file.uploadDate', defaultMessage: 'Upload date' })}</Box>
      </Box>

      {files.map(file => (
        <Box key={file.id} className={classes.file}>
          <Box width='70%' display="flex" alignItems="center" gap={1}>
            <DescriptionIcon color="primary" />
            <Typography>{file.name}</Typography>
          </Box>
          <Box width='25%'>
            <Typography>{file.uploadedAt}</Typography>
          </Box>
          <Box width='5%'>
            <IconButton className={classes.deleteIcon}>
              <DeleteIcon />
            </IconButton>
          </Box>
        </Box>
      ))}
    </StyledFilesEditor>
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
    '& .FilesEditor-fileIcon': {
      color: theme.palette.primary.main
    },
    '& .FilesEditor-deleteIcon': {
      color: theme.palette.error.main
    }
  };
})


export const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
    headerRow: ['headerRow'],
    file: ['file'],
    fileIcon: ['fileIcon'],
    deleteIcon: ['deleteIcon']

  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
