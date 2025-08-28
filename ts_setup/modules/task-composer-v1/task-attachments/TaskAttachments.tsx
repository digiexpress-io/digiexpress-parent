import React, { useRef } from 'react';

import { FormattedDate, FormattedMessage, FormattedNumber, FormattedTime, useIntl, } from 'react-intl';
import MaterialTable, { Column, MTableAction } from '@material-table/core';
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Typography } from '@mui/material';
import DownloadIcon from '@mui/icons-material/Download';
import DeleteIcon from '@mui/icons-material/Delete';

import { TaskApi, useTaskBackend } from '@dxs-ts/task-api';
import { useMaterialTableLabels } from '../api-mui-table';


const classes = {
  addButton: {
    marginLeft: "1rem"
  },
  input: {
    display: "none"
  },
  table: {
    boxShadow: "none"
  }
};

interface TableState {
  columns: Array<Column<TaskApi.Attachment>>;
}

export interface TaskAttachmentsProps {
  taskId: string,
  readonly: boolean,
  attachments: TaskApi.Attachment[],
  setAttachments: React.Dispatch<React.SetStateAction<TaskApi.Attachment[]>>
}

export const TaskAttachments: React.FC<TaskAttachmentsProps> = ({ taskId, readonly, attachments, setAttachments }) => {

  const backend = useTaskBackend();

  const intl = useIntl();
  const tableLocalization = useMaterialTableLabels();
  const tableRef = useRef();
  const [confirmOpen, setConfirmOpen] = React.useState(false);
  const [attachmentFileName, setAttachmentFileName] = React.useState<TaskApi.Attachment|null>(null);

//  const { loadAttachments } = useFetch('worker/rest/api/tasks/$taskId/files.GET', {});
//  const { downloadAttachmentLink } = useFetch('worker/rest/api/tasks/$taskId/files/$filename.GET', {});
//  const { addAttachment } = useFetch('worker/rest/api/tasks/$taskId/files.POST', {});
//  const { deleteAttachment } = useFetch('worker/rest/api/tasks/$taskId/files/$filename.DELETE', {});


  const formatTime = (time: any) => {
    if (time) {
      return (
        <React.Fragment>
          <FormattedDate value={time} />&nbsp;<FormattedTime value={time} />
        </React.Fragment>
      )
    }
    return "-";
  }

  const formatNumber = (value?: number | null) => {
    if (value) {
      return (
        <FormattedNumber value={value} />
      )
    }
    return "-";
  }
  const handleUploadClick = (files: FileList | null) => {
    if (files) {
      backend.persistence
        .createManyAttachments(taskId, files)
        .then(() => {
            backend.persistence.findAllAttachments(taskId).then(setAttachments)
        });


    }
  }
  const handleDownloadClick = async (data: TaskApi.Attachment | TaskApi.Attachment[]) => {
    let attachment = Array.isArray(data) ? data[0] : data;
    const link = await backend.persistence.getOneAttachmentLink(taskId, attachment);
    window.open(link);
  };

  const handleAttachmentDeleteClick = (data: TaskApi.Attachment | TaskApi.Attachment[]) => {
    let attachment = Array.isArray(data) ? data[0] : data;
    setAttachmentFileName(attachment);
    setConfirmOpen(true);
  };

  const deleteAttachmentFile = async () => {
    if (attachmentFileName) {
      await backend.persistence.deleteOneAttachment(taskId, attachmentFileName)
      await backend.persistence.findAllAttachments(taskId).then(setAttachments)
      setConfirmOpen(false);
    };
  }
  const tableState: TableState = {
    columns: [
      {
        title: intl.formatMessage({ id: 'attachmentTableHeader.name' }),
        field: 'name',
        headerStyle: { fontWeight: 'bold' }
      },
      {
        title: intl.formatMessage({ id: 'attachmentTableHeader.created' }),
        field: 'created',
        headerStyle: { fontWeight: 'bold' },
        render: data => formatTime(data.created)
      },
      {
        title: intl.formatMessage({ id: 'attachmentTableHeader.size' }),
        field: 'size',
        align: 'right',
        headerStyle: { fontWeight: 'bold' },
        render: data => formatNumber(data.size)
      },

    ]
  };

  const UploadButton: React.FC<{ label: string, disabled: boolean }> = ({ label, disabled }) => {
    return (
      <Button
        component='label'
        htmlFor="contained-button-file"
        color="primary"
        variant="contained"
        style={{ textTransform: 'none', padding: "4px 8px", margin: '2px 8px', borderRadius: '4px' }}
        size="small"
        disabled={disabled}
      >
        {label}
      </Button>
    )
  }

  const triggerFileInput = () => {
    document.getElementById('contained-button-file')?.click();
  }

  return (
    <>
      <input
        style={classes.input}
        id="contained-button-file"
        multiple
        type="file"
        accept=".jpg, .jpeg, .png, .pdf"
        onChange={(event) => { handleUploadClick(event?.target.files) }}
      />
      <MaterialTable
        style={classes.table}
        tableRef={tableRef}
        title={null}
        localization={tableLocalization}
        columns={tableState.columns}
        options={{
          filtering: false,
          search: true,
          maxColumnSort: 1,
          padding: "dense",
          actionsColumnIndex: -1,
          paging: false
        }}
        actions={[
          {
            icon: 'upload',
            isFreeAction: true,
            tooltip: intl.formatMessage({ id: 'attachmentButton.addAttachment' }),
            disabled: readonly,
            hidden: readonly,
            onClick: triggerFileInput
          },
          {
            icon: DownloadIcon,
            isFreeAction: false,
            tooltip: intl.formatMessage({ id: 'attachmentButton.downloadAttachment' }),
            onClick: (event, data) => { handleDownloadClick(data) }
          },
          {
            icon: DeleteIcon,
            isFreeAction: false,
            tooltip: intl.formatMessage({ id: 'attachmentButton.deleteAttachment' }),
            onClick: (event, data) => { handleAttachmentDeleteClick(data) }
          }
        ]}
        components={{
          Action: props => {
            if (props.action.isFreeAction && props.action.icon === 'upload') {
              return (<UploadButton label={props.action.tooltip} disabled={props.action.disabled}></UploadButton>);
            }
            return (<MTableAction {...props}></MTableAction>)
          },
        }}
        data={attachments || []}
      />
      <Dialog
        open={confirmOpen}
        onClose={() => setConfirmOpen(false)}
        maxWidth="xs"
      >
        <DialogTitle>
          <FormattedMessage id='attachment.delete.confirmTitle' />
        </DialogTitle>
        <DialogContent>
          <Typography color='error'>
            <FormattedMessage id='attachment.delete.confirmText' values={{fileName: attachmentFileName?.name}}/>
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button variant="outlined" onClick={() => setConfirmOpen(false)} color='secondary'>
            <FormattedMessage id='button.cancel' />
          </Button>
          <Button onClick={deleteAttachmentFile} color='error'>
            <FormattedMessage id='button.confirmDelete' />
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
}
