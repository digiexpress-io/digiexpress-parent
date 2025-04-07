import React, { useRef, useState } from 'react';
import MaterialTable, { Column } from '@material-table/core';

import EditIcon from '@mui/icons-material/Edit';

import { FormattedMessage, useIntl } from 'react-intl';

import { Box, IconButton, Tooltip, Typography } from '@mui/material';
import { useFetch } from '@dxs-ts/eveli-fetch';


import { CreateOrEditWorkflowDialog } from './CreateOrEditWorkflowDialog';
import { PublicationApi } from '../api-publications';
import { useMaterialTableLabels } from '../api-mui-table';
import { EveliDateTimeFormatter } from '../eveli-datetime-formatter';


interface TableState {
  columns: Array<Column<PublicationApi.AssetService>>;
}



export const EveliServices: React.FC<{}> = ({  }) => {
  const { workflows, refreshWorkflows } = useFetch('worker/rest/api/assets/workflows.GET', {});
  const { allTags: formTags } = useFetch('worker/rest/api/assets/dialob/tags.GET', {});

  const intl = useIntl();
  const tableLocalization = useMaterialTableLabels();
  const tableRef = useRef();

  const [open, setOpen] = useState(false);
  const [workflow, setWorkflow] = useState<PublicationApi.AssetService | null>(null);

  const formName = (data: PublicationApi.AssetService) => {
    let formLabel = data.body.formName;
    const tag = formTags?.find(t => t.formName === data.body.formName && t.tagName === data.body.formTag);
    if (tag) {
      formLabel = tag.formLabel;
    }
    return `${formLabel} / ${data.body.formTag}`;
  }

  const searchForms = (filter: any, rowData: PublicationApi.AssetService, columnDef: Column<PublicationApi.AssetService>) => {
    return formName(rowData).toLowerCase().includes(filter);
  }

  const sortForms = (form1: PublicationApi.AssetService, form2: PublicationApi.AssetService) => {
    return formName(form1).localeCompare(formName(form2));
  }

  const tableState: TableState = {
    columns: [
      {
        title: intl.formatMessage({ id: 'workflowTableHeader.name' }),
        field: 'body.name',
        headerStyle: { fontWeight: 'bold' },
        defaultSort: 'asc'
      },
      {
        title: intl.formatMessage({ id: 'workflowTableHeader.formName' }),
        field: 'body.formName',
        headerStyle: { fontWeight: 'bold' },
        render: data => formName(data),
        customFilterAndSearch: searchForms,
        customSort: sortForms
      },
      {
        title: intl.formatMessage({ id: 'workflowTableHeader.flowName' }),
        field: 'body.flowName',
        headerStyle: { fontWeight: 'bold' },
      },
      {
        title: intl.formatMessage({ id: 'workflowTableHeader.updated' }),
        field: 'body.updated',
        filtering: false,
        type: 'date',
        render: data => <EveliDateTimeFormatter value={data.body.updated} />,
        headerStyle: { fontWeight: 'bold' }
      },
      {
        render: data =>
          <Box justifySelf='end'>
            <Tooltip title={intl.formatMessage({ id: 'workflowTable.editButton' })}>
              <IconButton onClick={() => {
                setWorkflow(data as PublicationApi.AssetService);
                setOpen(true);
              }}>
                <EditIcon color='primary' />
              </IconButton>
            </Tooltip>
          </Box>
      }
    ]
  };


  return (
    <>
      <MaterialTable
        title={
        <Typography variant='h1'>
          <FormattedMessage id='workflowTable.title'/>
        </Typography>}
        
        localization={tableLocalization}
        columns={tableState.columns}
        tableRef={tableRef}
        options={{
          actionsColumnIndex: -1,
          debounceInterval: 500,
          padding: 'dense',
          filtering: false,
          maxColumnSort: 1,
          search: true,
          paging: false
        }}
        isLoading={false}
        data={workflows || []}
      />
      {workflow && <CreateOrEditWorkflowDialog open={open} setOpen={setOpen} workflow={workflow} onSubmit={() => refreshWorkflows()} dialobTags={formTags || []} />}
    </>
  );
}
