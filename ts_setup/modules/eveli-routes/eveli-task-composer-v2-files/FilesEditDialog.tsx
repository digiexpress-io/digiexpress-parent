import React from 'react';
import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, generateUtilityClass, styled, Typography, Zoom } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';

import { TaskApi } from '@dxs-ts/task-api';
import { FilesEditor } from './FilesEditor';
import { useFetch } from '@dxs-ts/envir-fetch';


export interface FilesEditProps {
  task: TaskApi.Task;
  open: boolean,
  onClose: () => void;
  attachments: TaskApi.Attachment[],
  setAttachments: React.Dispatch<React.SetStateAction<TaskApi.Attachment[]>>
}

export const FilesEditDialog: React.FC<FilesEditProps> = ({ task, open, onClose, attachments, setAttachments }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  const inputRef = React.useRef<HTMLInputElement | null>(null);

  const [confirmOpen, setConfirmOpen] = React.useState(false);
  const [attachmentFileName, setAttachmentFileName] = React.useState<string | null>(null);
  const { loadAttachments } = useFetch('worker/rest/api/tasks/$taskId/files.GET', {});
  const { downloadAttachmentLink } = useFetch('worker/rest/api/tasks/$taskId/files/$filename.GET', {});
  const { addAttachment } = useFetch('worker/rest/api/tasks/$taskId/files.POST', {});
  const { deleteAttachment } = useFetch('worker/rest/api/tasks/$taskId/files/$filename.DELETE', {});

  function handleFileDialog() {
    inputRef.current?.click();
  };


  const handleUploadClick = (files: FileList | null) => {
    if (files) {
      const arrFiles = Array.from(files)
      arrFiles.forEach((file, index) => {
        addAttachment(task.id, file)
          ?.then(response => {
            loadAttachments(task.id)
              .then(attachments => {
                setAttachments(attachments);
              });
          })
      })
    }
  }
  const handleDownloadClick = (data: TaskApi.Attachment | TaskApi.Attachment[]) => {
    let attachment = Array.isArray(data) ? data[0] : data;
    const link = downloadAttachmentLink(task.id, attachment.name);
    window.open(link);
  };

  const handleAttachmentDeleteClick = (data: TaskApi.Attachment | TaskApi.Attachment[]) => {
    let attachment = Array.isArray(data) ? data[0] : data;
    setAttachmentFileName(attachment.name);
    setConfirmOpen(true);
  };

  const deleteAttachmentFile = () => {
    if (attachmentFileName) {
      deleteAttachment(task.id, attachmentFileName)
        .then(resp => {
          loadAttachments(task.id)
            .then(attachments => {
              setAttachments(attachments);
            });
        })
    }
    setConfirmOpen(false);
  };


  return (<>

    {/* confirm delete file dialog */}
    <Dialog open={confirmOpen} onClose={() => setConfirmOpen(false)} maxWidth="xs">
      <DialogTitle>
        {intl.formatMessage({ id: 'attachment.delete.confirmTitle' })}
      </DialogTitle>
      <DialogContent>
        <Typography color='error'>
          {intl.formatMessage({ id: 'attachment.delete.confirmText' }, { fileName: attachmentFileName })}
        </Typography>
      </DialogContent>
      <DialogActions>
        <Button onClick={() => setConfirmOpen(false)} variant='outlined'>{intl.formatMessage({ id: 'button.cancel' })}</Button>
        <Button onClick={deleteAttachmentFile} color='error'> {intl.formatMessage({ id: 'button.confirmDelete' })}</Button>
      </DialogActions>
    </Dialog>



    <StyledDialog fullWidth maxWidth='lg' className={classes.root} open={open} onClose={onClose} slots={{ transition: Zoom }}>
      <DialogTitle sx={{ display: 'flex' }}>
        {intl.formatMessage({ id: 'task.attachments', defaultMessage: 'Task attached files' })}
        {intl.formatMessage({ id: 'eveli.textSeparatorColon' })}
        {task.taskRef ?? 'no task reference id'}
        <Box flexGrow={1} />
        <div>
          <input
            ref={inputRef}
            type="file"
            style={{ display: "none" }}
            onChange={(event) => { handleUploadClick(event?.target.files) }}
          />
          <Button variant='contained' onClick={handleFileDialog} startIcon={<AddIcon />}>
            {intl.formatMessage({ id: 'eveli.uploadFile', defaultMessage: 'Upload new file' })}
          </Button>
        </div>
      </DialogTitle>

      <DialogContent>
        <FilesEditor task={task} onDownload={handleDownloadClick} onDelete={handleAttachmentDeleteClick} attachments={attachments} />
      </DialogContent>

      <DialogActions>
        <Button variant='outlined' onClick={onClose}>{intl.formatMessage({ id: 'button.cancel' })}</Button>
        <Button>{intl.formatMessage({ id: 'button.save' })}</Button>
      </DialogActions>
    </StyledDialog>
  </>
  )
}






const MUI_NAME = 'FilesEditDialog';
const StyledDialog = styled(Dialog, {
  name: MUI_NAME,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },

})(({ theme }) => {

  return {
    '.MuiDialogContent-root': {
      display: 'flex',
      flexDirection: 'column',
      overflow: 'hidden'
    }
  };
})


const useUtilityClasses = () => {
  const slots = {
    root: ['root'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
}
