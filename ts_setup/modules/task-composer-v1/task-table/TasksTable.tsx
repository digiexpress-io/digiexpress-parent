import React, { useContext, useRef, forwardRef, useEffect, useState } from 'react';
import { FormattedMessage, useIntl } from 'react-intl';

import MaterialTable, { Column, Query } from '@material-table/core';

import { alpha, Container, Typography, useTheme } from '@mui/material';
import { Refresh as RefreshIcon } from '@mui/icons-material';
import { Add as AddIcon } from '@mui/icons-material';

import { TaskApi, useTaskBackend } from '@dxs-ts/task-api';
import { createMuiTableQueryString, useMaterialTableLabels } from '../api-mui-table';
import { TasksTableContext } from './TasksTableProvider';
import { useTasksTableState } from './useTasksTableState';



interface UseRefreshReturnType {
  isFirstRenderAfterRefresh: boolean;
  setRefreshed: React.Dispatch<React.SetStateAction<boolean>>;
}

const useRefresh = (): UseRefreshReturnType => {
  const [isFirstRenderAfterRefresh, setIsFirstRenderAfterRefresh] = useState(true);
  const setRefreshed = useRef(setIsFirstRenderAfterRefresh);

  return { isFirstRenderAfterRefresh, setRefreshed: setRefreshed.current };
};


const AddTaskAction: React.FC<{}> = (props) => {
  const backend = useTaskBackend();
  if(!backend.permissions.isCreateTaskAllowed) {
    return (<></>);
  }

  return (
    
      <div style={{ cursor: 'pointer' }} onClick={() => {
        backend.navigate.createOneTask();
      }}>
        <AddIcon />
      </div>
  );
};

export const TasksTable: React.FC = ({ }) => {
  const intl = useIntl();
  const theme = useTheme();
  const backend = useTaskBackend();
  const tableLocalization = useMaterialTableLabels();
  const tableContext = useContext(TasksTableContext);
  const { isFirstRenderAfterRefresh, setRefreshed } = useRefresh();
  
  useEffect(() => {
    try {
      if (localStorage.getItem("filters")) {
        tableContext.setFilters(JSON.parse(localStorage.getItem("filters") as string));
      }
    } catch (error) {
      console.error(error);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (tableContext.filters) {
      localStorage.setItem("filters", JSON.stringify(tableContext.filters));
    } else {
      localStorage.removeItem("filters");
    }
  }, [tableContext.filters]);

  const orderCollection = tableContext.sort || [
    { orderBy: 0, orderByField: "priority", orderDirection: 'desc', sortOrder: 1 },
    { orderBy: 7, orderByField: "dueDate", orderDirection: 'asc', sortOrder: 2 }
  ];

  const onOrderCollectionChange = (orderByCollection: any) => {
    tableContext.setSort(orderByCollection);
  };
  const tableState = useTasksTableState();



 const loadTasks = async (query:Query<TaskApi.Task>, columns:Column<any>[], tableContext: { paging: any, setPaging: (paging:any)=>void} ) => {
    // store paging info to allow restoring of page on navigation back
    let page = query.page;
    let pageSize = query.pageSize;
    const currentPaging = tableContext.paging;
    if (page !== currentPaging?.page || pageSize !== currentPaging?.pageSize) {
      tableContext.setPaging({page, pageSize});
    }

    let visibleColumns: any = [];
    const hiddenColumns = columns.map((column: any) => {
      if(column.hidden){
        return column.field
      }else{
        visibleColumns.push(column.field)
        return undefined;
      }
    })

    let queryString = createMuiTableQueryString(
      {...query, 
        filters: query.filters.filter((item: any) => !hiddenColumns.includes(item.column.field)), 
        orderByCollection: query.orderByCollection.reduce((accumulator: any[], item: any) => {
          if (item.sortOrder > 0) {
            if (!hiddenColumns.includes(columns[item.orderBy].field)) {
              accumulator.push({
                ...item,
                orderBy: visibleColumns.findIndex((visibleColumn: any) => visibleColumn === columns[item.orderBy].field)
              });
            }
          }
          return accumulator;
        }, [])
      }, 
      columns.filter((column: any) => !column.hidden)
    );
    

    return backend.persistence.paginateTasks(queryString);
  }


  return (
    <Container maxWidth='xl'>
      <MaterialTable
        tableRef={tableState.tableRef}
        icons={{ Filter: forwardRef(() => <div />) }}
        title={<Typography variant='h1'><FormattedMessage id='tasksView.title' /></Typography>}
        localization={tableLocalization}
        columns={tableState.columns}

        sx={{
          '& .MuiInputBase-root': {
            height: '3rem',
            alignContent: 'center',
            paddingLeft: '0px !important',
            paddingRight: '0px !important',
          },
          '& .MuiInputBase-inputTypeSearch': {
            paddingLeft: '0px !important',
          },
          '& .MuiSelect-select': {
            height: '3rem',
            alignContent: 'center',
          },
          '& .MuiTableRow-root:nth-of-type(even)': {
            backgroundColor: alpha(theme.palette.secondary.main, 0.7),
          },
          '& .MuiTableRow-root:nth-of-type(odd)': {
            backgroundColor: theme.palette.background.default,
          },
          '& .MuiTableRow-root.MuiTableRow-head': {
            backgroundColor: alpha(theme.palette.secondary.main, 0.7),
            textTransform: 'uppercase',
            '.MuiButtonBase-root': { // font for table headers that are buttons
              fontSize: '10pt'
            },
            'div': {
              fontSize: '10pt', // table headers that are not buttons
            }
          },
          '& .MuiTableCell-root': {
            borderBottom: `1px solid ${theme.palette.divider}`,
          },

        }}
        options={{
          filterCellStyle: {
            paddingBottom: theme.spacing(2),
            paddingTop: 'unset',
            paddingLeft: theme.spacing(0.5),
            paddingRight: theme.spacing(0.5)
          },
          columnsButton: true,
          filtering: true,
          search: false,
          pageSize: tableContext.paging?.pageSize || 10,
          initialPage: tableContext.paging?.page || 0,
          padding: "dense",
          actionsColumnIndex: -1,
          debounceInterval: 500,
          idSynonym: 'id',
          maxColumnSort: 3,
          defaultOrderByCollection: orderCollection,
          showColumnSortOrder: true,
        }}
        actions={[
          {
            icon: RefreshIcon,
            isFreeAction: true,
            tooltip: intl.formatMessage({ id: 'taskButton.refresh' }),
            onClick: () => tableState.tableRef.current.onQueryChange()
          },
          {
            icon: AddTaskAction as any,
            isFreeAction: true,
            tooltip: intl.formatMessage({ id: 'taskButton.addTask' }),
            onClick: () => { }
          }
        ]}
        data={query => {
          if (isFirstRenderAfterRefresh && tableContext.filters) {
            query.filters = tableContext.filters;
          }
          setRefreshed(false);
          return loadTasks(query, tableState.columns, tableContext);
        }}
        onFilterChange={
          (filters: any) => {
            tableContext.setFilters(filters);
          }
        }
        onOrderCollectionChange={onOrderCollectionChange}
        onChangeColumnHidden={(hiddenColumn: Column<TaskApi.Task>) => {
          if (tableState.tableRef.current)
            tableState.tableRef.current.onQueryChange();
        }}
      /> 
    </Container>
  );
}

