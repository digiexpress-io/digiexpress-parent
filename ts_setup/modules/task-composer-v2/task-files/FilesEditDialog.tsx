import React from 'react';
import { Box, Button, DialogActions, DialogContent, DialogTitle, Typography, Zoom } from '@mui/material';
import { Add as AddIcon } from '@mui/icons-material';
import { useIntl } from 'react-intl';

import { TaskApi, useTaskBackend } from '@dxs-ts/task-api';
import { FilesEditor } from './FilesEditor';
import { FilesEditDialogRoot, useFilesEditDialogClasses } from './useUtilityClasses';

/** ===========================
 *  DEV MOCK TOGGLE
 *  Flip to false when backend supports attachments
 *  =========================== */
const USE_MOCK_ATTACHMENTS = true;

/** Local helper type: allow extra mock fields without TS errors */
type AnyAttachment = Partial<TaskApi.Attachment> & {
  id?: string;
  attachmentId?: string;
  name?: string;
  created?: Date | string;
  createdAt?: Date | string;
  updated?: Date | string;
  size?: number;
  [k: string]: any;
};

/** Compute a key even if SDK type lacks 'id' */
const getAttachmentKey = (att: AnyAttachment): string => {
  return String(
    att.id ??
      att.attachmentId ??
      `${att.name ?? 'noname'}-${att.created ?? att.createdAt ?? ''}`,
  );
};

/** Minimal helper for mock download */
const mockDownloadBlob = (name: string, content: string, type = 'text/plain') => {
  const blob = new Blob([content], { type });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = name;
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
};

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

  // === Real attachments state/load (used when mock is OFF)
  const [attachments, setAttachments] = React.useState<TaskApi.Attachment[]>([]);
  React.useEffect(() => {
    if (!USE_MOCK_ATTACHMENTS) {
      backend.persistence.findAllAttachments(task.id).then(setAttachments);
    }
  }, [task.id, backend.persistence]);

  // === Delete confirmation (real flow)
  const [confirmOpen, setConfirmOpen] = React.useState(false);
  const [attachmentFileName, setAttachmentFileName] =
    React.useState<TaskApi.Attachment | null>(null);

  function handleFileDialog() {
    inputRef.current?.click();
  }

  const handleUploadClick = (files: FileList | null) => {
    if (!files || files.length === 0) return;

    if (USE_MOCK_ATTACHMENTS) {
      const file = files[0];
      mockUpload(file);
      return;
    }

    backend.persistence
      .createManyAttachments(task.id, files)
      .then(() => backend.persistence.findAllAttachments(task.id))
      .then(setAttachments);
  };

  const handleDownloadClick = async (data: TaskApi.Attachment | TaskApi.Attachment[]) => {
    const att = Array.isArray(data) ? data[0] : data;
    if (USE_MOCK_ATTACHMENTS) {
      mockDownload(att as AnyAttachment);
      return;
    }
    const link = await backend.persistence.getOneAttachmentLink(task.id, att as TaskApi.Attachment);
    window.open(link);
  };

  const handleAttachmentDeleteClick = (data: TaskApi.Attachment | TaskApi.Attachment[]) => {
    const att = Array.isArray(data) ? data[0] : data;

    if (USE_MOCK_ATTACHMENTS) {
      mockDelete(att as AnyAttachment);
      return;
    }

    setAttachmentFileName(att as TaskApi.Attachment);
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

  /** ===========================
   *  MOCK IMPLEMENTATION (local)
   *  Uses `created` to match TaskAttachments / FilesEditor
   *  =========================== */
  const [mockItems, setMockItems] = React.useState<AnyAttachment[]>([
    { id: '1', name: 'specs.pdf', size: 321_000, created: new Date() },
    { id: '2', name: 'photo.jpg', size: 820_144, created: new Date() },
  ]);

  const mockUpload = async (file: File) => {
    const att: AnyAttachment = {
      id: String(Date.now()),
      name: file.name,
      size: file.size,
      created: new Date(),
    };
    setMockItems(prev => [att, ...prev]);
  };

  const mockDelete = async (attachment: AnyAttachment) => {
    const key = getAttachmentKey(attachment);
    setMockItems(prev => prev.filter(a => getAttachmentKey(a) !== key));
  };

  const mockDownload = async (att: AnyAttachment) => {
    mockDownloadBlob(
      String(att.name),
      `Mock file for ${att.name}\nGenerated: ${new Date().toISOString()}`,
    );
  };

  // Which list to show
  const visibleAttachments = (USE_MOCK_ATTACHMENTS ? mockItems : attachments) as TaskApi.Attachment[];

  return (
    <>
      {/* confirm delete (real backend path) */}
      <FilesEditDialogRoot
        fullWidth
        maxWidth="lg"
        className={classes.root}
        open={open}
        onClose={onClose}
        slots={{ transition: Zoom }}
      >
        <DialogTitle className={classes.titleRow}>
          <span>
            {intl.formatMessage({ id: 'task.attachments', defaultMessage: 'Task attached files' })}
            {intl.formatMessage({ id: 'eveli.textSeparatorColon' })}
            {task.taskRef ?? 'no task reference id'}
          </span>

          <Box className={classes.grow} />

          <div className={classes.actionsRow}>
            <input
              ref={inputRef}
              type="file"
              className={classes.hiddenInput}
              onChange={(event) => {
                handleUploadClick(event?.target.files);
              }}
            />
            <Button
              className={classes.uploadBtn}
              variant="contained"
              onClick={handleFileDialog}
              startIcon={<AddIcon />}
            >
              {intl.formatMessage({ id: 'eveli.uploadFile', defaultMessage: 'Upload new file' })}
            </Button>
            {USE_MOCK_ATTACHMENTS && (
              <Typography className={classes.mockedBadge} variant="caption">
                (mocked)
              </Typography>
            )}
          </div>
        </DialogTitle>

        <DialogContent className={classes.content}>
          <FilesEditor
            task={task}
            attachments={visibleAttachments}
            onDownload={handleDownloadClick}
            onDelete={handleAttachmentDeleteClick}
          />
        </DialogContent>

        <DialogActions className={classes.dialogActions}>
          <Button variant="outlined" onClick={onClose}>
            {intl.formatMessage({ id: 'button.cancel' })}
          </Button>
          <Button>{intl.formatMessage({ id: 'button.save' })}</Button>
        </DialogActions>
      </FilesEditDialogRoot>
    </>
  );
};
