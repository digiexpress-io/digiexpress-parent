import React, { useContext } from 'react';
import { Typography } from '@mui/material';
import MaterialTable, { Column, Query, QueryResult } from '@material-table/core';

import { useIntl } from 'react-intl';
import { useConfig } from '../../context/ConfigContext';
import { SessionRefreshContext } from '../../context/SessionRefreshContext';
import { Process } from '../../types/Process';

import { localizeTable } from '../../util/localizeTable';
import { createQueryString } from '../../util/tableQuery';
import { TableHeader } from 'frontdesk/components/TableHeader';


interface TableState  {
  columns: Array<Column<Process>>;
}

export const ProcessTable: React.FC = () => {
  const intl = useIntl();
  const session = useContext(SessionRefreshContext);
  const config = useConfig();
  const tableLocalization = localizeTable((id: string) => intl.formatMessage({ id }));

  const loadProcesses = (query: Query<Process>):Promise<QueryResult<Process>> => {
    let queryString = createQueryString(query, tableState.columns);
    return session.cFetch(`${config.serviceUrl}rest/api/worker/processes?${queryString}`, {
      headers: {
        'Accept': 'application/json'
      },
    })
    .then(response=>response.json())
    .then(json=> {
      return {
        data: json.content, // array of data
        page: json.pageable.pageNumber, // current page we are on, starts with 0 = first page
        totalCount: json.numberOfElements // total entries on all the pages combined
      }
    })
  };
  


  const tableState: TableState = {
    columns: [
      {
        title: intl.formatMessage({id: 'processTableHeader.workflow'}),
        field: 'workflowName',
        headerStyle: { fontWeight: 'bold' },
      },
      {
        title: intl.formatMessage({id: 'processTableHeader.questionnaireId'}),
        field: 'questionnaire',
        filtering: false,
        headerStyle: { fontWeight: 'bold' },
      },
      {
        title: intl.formatMessage({id: 'processTableHeader.status'}),
        field: 'status',
        headerStyle: { fontWeight: 'bold' },
        lookup: {
          'ANSWERED': intl.formatMessage({id: 'process.status.ANSWERED'}),
          'CREATED': intl.formatMessage({id: 'process.status.CREATED'}),
          'ANSWERING': intl.formatMessage({id: 'process.status.ANSWERING'}),
          'IN_PROGRESS': intl.formatMessage({id: 'process.status.IN_PROGRESS'}),
          'WAITING': intl.formatMessage({id: 'process.status.WAITING'}),
          'COMPLETED': intl.formatMessage({id: 'process.status.COMPLETED'}),
          'REJECTED': intl.formatMessage({id: 'process.status.REJECTED'}),
        },
      },
      {
        title: intl.formatMessage({id: 'processTableHeader.created'}),
        field: 'created',
        filtering: false,
        headerStyle: { fontWeight: 'bold' },
      }
    ]
  };

  return (
      <>
      <MaterialTable
        title={<TableHeader id='processTable.title' />}
        localization={tableLocalization}
        columns={tableState.columns}
        options={{
          actionsColumnIndex: -1,
          debounceInterval: 500,
          padding: 'dense',
          filtering: true,
          paging: true,
          pageSize: 20
        }}

       
        isLoading={false}
        data={query=> {
          return loadProcesses(query)
        }
        }
      />
      </>
  );
}
