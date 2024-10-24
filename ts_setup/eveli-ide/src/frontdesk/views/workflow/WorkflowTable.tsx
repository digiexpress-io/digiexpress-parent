import MaterialTable, { Column } from '@material-table/core';
import moment from 'moment';
import React, { useRef, useState } from 'react';
import { FormattedDate, FormattedTime, useIntl } from 'react-intl';
import { useConfig } from '../../context/ConfigContext';
import { useFetch } from '../../hooks/useFetch';
import { DialobFormTag } from '../../types';
import { Workflow } from '../../types/Workflow';

import { localizeTable } from '../../util/localizeTable';
import { NewWorkflow } from './NewWorkflow';
interface TableState  {
  columns: Array<Column<Workflow>>;
}

interface WorkflowTableProps {
  workflows? : Workflow[]
  refreshWorkflows: ()=>void
  historyView?: boolean
}

export const WorkflowTable: React.FC<WorkflowTableProps> = ({workflows, refreshWorkflows, historyView=false}) => {
  const intl = useIntl();
  const config = useConfig();
  const tableLocalization = localizeTable((id: string) => intl.formatMessage({ id }));
  const tableRef = useRef();
  const { response:formTags } = useFetch<DialobFormTag[]>(`${config.api}/forms/tags`);
  const [open, setOpen] = useState(false);
  const [workflow, setWorkflow] = useState<Workflow|null>(null);

 
  const formName = (data: Workflow) => {
    let formLabel = data.formName;
    const tag = formTags?.find(t => t.formName === data.formName && t.tagName === data.formTag);
    if (tag) {
      formLabel = tag.formLabel;
    }
    return `${formLabel} / ${data.formTag}`;
  }

  const searchForms = (filter: any, rowData: Workflow, columnDef: Column<Workflow>) => {
    return formName(rowData).toLowerCase().includes(filter);
  }

  const sortForms = (form1:Workflow, form2: Workflow) => {
    return formName(form1).localeCompare(formName(form2));
  }

  const formatDateTime = (time:any) => {
    if (time) {
      const localTime = moment.utc(time).local().toDate();
      return (
        <React.Fragment>
          <FormattedDate value={localTime} />&nbsp;<FormattedTime value={localTime} />
        </React.Fragment>
      )
    }
    return "-";
  }

  const tableState: TableState = {
    columns: [
      {
        title: intl.formatMessage({id: 'workflowTableHeader.name'}),
        field: 'name',
        headerStyle: { fontWeight: 'bold' },
        defaultSort: 'asc'
      },
      {
        title: intl.formatMessage({id: 'workflowTableHeader.formName'}),
        field: 'formName',
        headerStyle: { fontWeight: 'bold' },
        render: data => formName(data),
        customFilterAndSearch: searchForms,
        customSort: sortForms
      },
      {
        title: intl.formatMessage({id: 'workflowTableHeader.flowName'}),
        field: 'flowName',
        headerStyle: { fontWeight: 'bold' },
      },
      {
        title: intl.formatMessage({id: 'workflowTableHeader.updated'}),
        field: 'updated',
        filtering: false,
        type: 'date',
        render: data => formatDateTime(data.updated),
        headerStyle: { fontWeight: 'bold' }
      }
    ]
  };

  return (
      <>
      <MaterialTable
        title = {intl.formatMessage({id: 'workflowTable.title'})}
        localization={tableLocalization}
        columns={tableState.columns}
        tableRef={tableRef}
        options={{
          actionsColumnIndex: -1,
          debounceInterval: 500,
          padding: 'dense',
          filtering: false,
          sorting: true,
          search: true,
          paging: false
        }}
        actions={historyView || !config.modifiableAssets ? [] : [
          {
            icon: 'add',
            tooltip: intl.formatMessage({id: 'workflowTable.addButton'}),
            isFreeAction: true,
            onClick: () => {setWorkflow(null);setOpen(true);}
          },
          {
            icon: 'edit',
            tooltip: intl.formatMessage({id: 'workflowTable.editButton'}),
            onClick: (event, data) => {setWorkflow(data as Workflow);setOpen(true)}
          }
        ]}
       
        isLoading={false}
        data={workflows||[]}
      />
      <NewWorkflow open={open} setOpen={setOpen} workflow={workflow} onSubmit={() => refreshWorkflows()} 
        dialobTags={formTags || []} /> 
      </>
  );
}
