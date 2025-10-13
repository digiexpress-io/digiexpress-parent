import React from 'react';
import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, generateUtilityClass, styled, Typography, Zoom } from '@mui/material';
import { Add as AddIcon } from '@mui/icons-material';
import composeClasses from '@mui/utils/composeClasses';
import { useIntl } from 'react-intl';

import { TaskApi, useTaskBackend } from '@dxs-ts/task-api';
import { FilesEditor } from './FilesEditor';


export interface FilesEditProps {
  task: TaskApi.Task;
  open: boolean,
  onClose: () => void;
}

export const FilesEditDialog: React.FC<FilesEditProps> = ({ task, open, onClose }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  const inputRef = React.useRef<HTMLInputElement | null>(null);

  const backend = useTaskBackend();
  
  const [attachments, setAttachments] = React.useState<TaskApi.Attachment[]>([]);
  React.useEffect(() => {
    backend.persistence.findAllAttachments(task.id).then(setAttachments);
  }, [task.id]);

  const [confirmOpen, setConfirmOpen] = React.useState(false);
  const [attachmentFileName, setAttachmentFileName] = React.useState<TaskApi.Attachment | null>(null);

  function handleFileDialog() {
    inputRef.current?.click();
  };

  const handleUploadClick = (files: FileList | null) => {
    if (files) {
      backend.persistence.createManyAttachments(task.id, files)
        .then(() => backend.persistence.findAllAttachments(task.id))
        .then(setAttachments);
    }
  }
  const handleDownloadClick = async (data: TaskApi.Attachment | TaskApi.Attachment[]) => {
    let attachment = Array.isArray(data) ? data[0] : data;
    const link = await backend.persistence.getOneAttachmentLink(task.id, attachment);
    window.open(link);
  };

  const handleAttachmentDeleteClick = (data: TaskApi.Attachment | TaskApi.Attachment[]) => {
    let attachment = Array.isArray(data) ? data[0] : data;
    setAttachmentFileName(attachment);
    setConfirmOpen(true);
  };

  const deleteAttachmentFile = () => {
    if (attachmentFileName) {
      backend.persistence.deleteOneAttachment(task.id, attachmentFileName)
      .then(() => backend.persistence.findAllAttachments(task.id))
      .then(setAttachments);
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
          {intl.formatMessage({ id: 'attachment.delete.confirmText' }, { fileName: attachmentFileName?.name })}
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
