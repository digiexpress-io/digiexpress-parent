import React, { useRef } from 'react';

import { FormattedDate, FormattedNumber, FormattedTime, useIntl, } from 'react-intl';
import MaterialTable, { Column, MTableAction  } from '@material-table/core';
import { Box, Button} from '@mui/material';
import FileUploadIcon from '@mui/icons-material/FileUpload';
import DownloadIcon from '@mui/icons-material/Download';

import { localizeTable } from '../../util/localizeTable';
import { Attachment } from '../../types';
import { useAttachmentConfig } from '../../context/AttachmentContext';

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
  columns: Array<Column<Attachment>>;
}

interface Props {
  taskId: number,
  readonly: boolean,
  attachments: Attachment[], 
  setAttachments: React.Dispatch<React.SetStateAction<Attachment[]>>
}

export const AttachmentTable:React.FC<Props> = ({ taskId, readonly, attachments, setAttachments }) =>{

  const intl = useIntl();
  const tableLocalization = localizeTable((id: string) => intl.formatMessage({ id }));
  const tableRef = useRef();
  const attachmentContext = useAttachmentConfig();

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
        attachmentContext.addAttachment(taskId, file)
        ?.then(response=>{
          attachmentContext.loadAttachments(taskId)
          .then(attachments => {
            setAttachments(attachments);
          });
        })
      })
    }
  }
  const handleDownloadClick = (data: Attachment | Attachment[]) => {
    let attachment = Array.isArray(data) ? data[0] : data;
    const link = attachmentContext.downloadAttachmentLink(taskId, attachment.name);
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
            sorting: true,
            padding: "dense",
            actionsColumnIndex: -1,
            paging: false
          }}
          actions={[
            {
              icon: FileUploadIcon,
              isFreeAction: true,
              tooltip: intl.formatMessage({id: 'attachmentButton.addAttachment'}),
              disabled: readonly,
              hidden: readonly,
              onClick: ()=>{}
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
