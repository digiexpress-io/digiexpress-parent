import React from 'react';
import { Box, Button, DialogActions, DialogContent, DialogTitle, Typography, Zoom } from '@mui/material';
import { Add as AddIcon } from '@mui/icons-material';
import { useIntl } from 'react-intl';

import { TaskApi, useTaskBackend } from '@dxs-ts/task-api';
import { FilesEditor } from './FilesEditor';
import { FilesEditDialogRoot, useFilesEditDialogClasses } from './useUtilityClasses';

export interface FilesEditProps {
  task: TaskApi.Task;
  open: boolean;
  onClose: () => void;
}

export const FilesEditDialog: React.FC<FilesEditProps> = ({ task, open, onClose }) => {
  const classes = useFilesEditDialogClasses();
  const intl = useIntl();
  const inputRef = React.useRef<HTMLInputElement | null>(null);

  const backend = useTaskBackend();
  const [attachments, setAttachments] = React.useState<TaskApi.Attachment[]>([]);
  const [uploadError, setUploadError] = React.useState<string | null>(null);

  React.useEffect(() => {
    backend.persistence.findAllAttachments(task.id).then(setAttachments);
  }, [task.id, backend.persistence]);

  const [confirmOpen, setConfirmOpen] = React.useState(false);
  const [attachmentFileName, setAttachmentFileName] =
    React.useState<TaskApi.Attachment | null>(null);

  function handleFileDialog() {
    inputRef.current?.click();
  }

  const handleUploadClick = (files: FileList | null) => {
    if (!files || files.length === 0) return;
  
    // allow selecting the same file again to re-trigger onChange
    if (inputRef.current) {
      inputRef.current.value = '';
    }
  
    const normalize = (name: string) => name.trim().toLowerCase();
  
    const existingNames = new Set(attachments.map((a) => normalize(a.name)));
    const selected = Array.from(files);
  
    const duplicates = selected
      .map((f) => f.name)
      .filter((name) => existingNames.has(normalize(name)));
  
    if (duplicates.length > 0) {
      const unique = Array.from(new Set(duplicates));
      setUploadError(
        unique.length === 1
          ? `A file named "${unique[0]}" already exists. Please rename it before uploading.`
          : `These files already exist: ${unique.map((n) => `"${n}"`).join(', ')}. Please rename them before uploading.`
      );
      return;
    }
  
    setUploadError(null);
  
    backend.persistence
      .createManyAttachments(task.id, files)
      .then(() => backend.persistence.findAllAttachments(task.id))
      .then(setAttachments);
  };

  const handleDownloadClick = async (data: TaskApi.Attachment | TaskApi.Attachment[]) => {
    const att = Array.isArray(data) ? data[0] : data;
    const link = await backend.persistence.getOneAttachmentLink(task.id, att);
    window.open(link);
  };

  const handleAttachmentDeleteClick = (data: TaskApi.Attachment | TaskApi.Attachment[]) => {
    const att = Array.isArray(data) ? data[0] : data;
    setAttachmentFileName(att);
    setConfirmOpen(true);
  };

  const deleteAttachmentFile = () => {
    if (attachmentFileName) {
      backend.persistence
        .deleteOneAttachment(task.id, attachmentFileName)
        .then(() => backend.persistence.findAllAttachments(task.id))
        .then(setAttachments);
    }
    setConfirmOpen(false);
  };

  return (
    <FilesEditDialogRoot
      fullWidth
      maxWidth="lg"
      className={classes.root}
      open={open}
      onClose={onClose}
      slots={{ transition: Zoom }}
    >
      <DialogTitle className={classes.titleRow}>
        {intl.formatMessage({ id: 'task.attachments' })}{": "}{task.taskRef ?? intl.formatMessage({ id: 'task.noTaskReferenceId' })}

        <Box className={classes.grow} />

        <div className={classes.actionsRow}>
          <input
            ref={inputRef}
            type="file"
            className={classes.hiddenInput}
            onChange={(event) => handleUploadClick(event?.target.files)}
          />
          <Button
            className={classes.uploadBtn}
            variant="contained"
            onClick={handleFileDialog}
            startIcon={<AddIcon />}
          >
            {intl.formatMessage({ id: 'task.button.uploadFile' })}
          </Button>
          {uploadError ? (
            <Typography color="error" variant="body2">
              {uploadError}
            </Typography>
          ) : null}
        </div>
      </DialogTitle>

      <DialogContent className={classes.content}>
        <FilesEditor
          task={task}
          attachments={attachments}
          onDownload={handleDownloadClick}
          onDelete={handleAttachmentDeleteClick}
        />
      </DialogContent>

      <DialogActions className={classes.dialogActions}>
        <Button variant="outlined" onClick={onClose}>
          {intl.formatMessage({ id: 'button.cancel' })}
        </Button>
      </DialogActions>
    </FilesEditDialogRoot>
  );
};
