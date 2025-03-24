import React, { useRef } from 'react';

import { FormattedDate, FormattedNumber, FormattedTime, useIntl, } from 'react-intl';
import MaterialTable, { Column, MTableAction  } from '@material-table/core';
import { Box, Button} from '@mui/material';
import DownloadIcon from '@mui/icons-material/Download';
import { TaskApi } from '../api-task';
import { useMaterialTableLabels } from '../api-mui-table';
import { useFetch } from '@dxs-ts/eveli-fetch';


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

interface TableState  {
  columns: Array<Column<TaskApi.Attachment>>;
}

export interface EveliTaskAttachmentsProps {
  taskId: string,
  readonly: boolean,
  attachments: TaskApi.Attachment[], 
  setAttachments: React.Dispatch<React.SetStateAction<TaskApi.Attachment[]>>
}

export const EveliTaskAttachments: React.FC<EveliTaskAttachmentsProps> = ({ taskId, readonly, attachments, setAttachments }) =>{

  const intl = useIntl();
  const tableLocalization = useMaterialTableLabels();
  const tableRef = useRef();

  const { loadAttachments } = useFetch('worker/rest/api/tasks/$taskId/files.GET', {});
  const { downloadAttachmentLink } = useFetch('worker/rest/api/tasks/$taskId/files/$filename.GET', {});
  const { addAttachment } = useFetch('worker/rest/api/tasks/$taskId/files.POST', {});


  const formatTime = (time:any) => {
    if (time) {
      return (
        <React.Fragment>
          <FormattedDate value={time} />&nbsp;<FormattedTime value={time}/>
        </React.Fragment>
      )
    }
    return "-";
  }

  const formatNumber = (value?:number|null) => {
    if (value) {
      return (
        <FormattedNumber value={value} />
      )
    }
    return "-";
  }
  const handleUploadClick = (files: FileList|null) => {
    if (files) {
      const arrFiles = Array.from(files)
      arrFiles.forEach((file, index) => {
        addAttachment(taskId, file)
        ?.then(response => {
          loadAttachments(taskId)
          .then(attachments => {
            setAttachments(attachments);
          });
        })
      })
    }
  }
  const handleDownloadClick = (data: TaskApi.Attachment | TaskApi.Attachment[]) => {
    let attachment = Array.isArray(data) ? data[0] : data;
    const link = downloadAttachmentLink(taskId, attachment.name);
    window.open(link);
  };

  const tableState: TableState = {
    columns: [
      {
        title: intl.formatMessage({id: 'attachmentTableHeader.name'}),
        field: 'name',
        headerStyle: { fontWeight: 'bold' }
      },
      {
        title: intl.formatMessage({id: 'attachmentTableHeader.created'}),
        field: 'created',
        headerStyle: { fontWeight: 'bold' },
        render: data => formatTime(data.created)
      },
      {
        title: intl.formatMessage({id: 'attachmentTableHeader.updated'}),
        field: 'updated',
        headerStyle: { fontWeight: 'bold' },
        render: data => formatTime(data.updated)
      },
      {
        title: intl.formatMessage({id: 'attachmentTableHeader.size'}),
        field: 'size',
        align: 'right',
        headerStyle: { fontWeight: 'bold' },
        render: data => formatNumber(data.size)
      },
      
    ]
  };

  const UploadButton:React.FC<{label:string, disabled:boolean}> = ({label, disabled})=> {
    return (
        <Button
          component='label'
          htmlFor="contained-button-file"
          color="primary"
          variant="contained"
          style={{textTransform: 'none', padding: "4px 8px", margin: '2px 8px', borderRadius: '4px'}}
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
    <Box>
        <input
          style={classes.input}
          id="contained-button-file"
          multiple
          type="file"
          accept=".jpg, .jpeg, .png, .pdf"
          onChange={(event)=>{handleUploadClick(event?.target.files)}}
        />
        <MaterialTable
          style={classes.table}
          tableRef = {tableRef}
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
              tooltip: intl.formatMessage({id: 'attachmentButton.addAttachment'}),
              disabled: readonly,
              hidden: readonly,
              onClick: triggerFileInput
            },
            {
              icon: DownloadIcon,
              isFreeAction: false,
              tooltip: intl.formatMessage({id: 'attachmentButton.downloadAttachment'}),
              onClick: (event, data)=>{handleDownloadClick(data)}
            }
          ]}
          components={{
            Action: props => {
              if (props.action.isFreeAction && props.action.icon==='upload') {
                return (<UploadButton label={props.action.tooltip} disabled={props.action.disabled}></UploadButton>);
              }
              return (<MTableAction {...props}></MTableAction>)
            },
          }}
          data={attachments || []}
        />
    </Box>
  );
}
