import React, { useContext, useRef, forwardRef, useEffect, useState } from 'react';

import MaterialTable, { Column } from '@material-table/core';

import { alpha, Container, Typography, useTheme } from '@mui/material';
import RefreshIcon from '@mui/icons-material/Refresh';
import AddIcon from '@mui/icons-material/Add';
import { FormattedMessage, useIntl } from 'react-intl';

import { TaskApi } from '../api-task';
import { useMaterialTableLabels } from '../api-mui-table';

import { EveliTaskTableContext } from './EveliTaskTableProvider';
import { useTasksTableState } from './useTasksTableState';
import { useFetch } from '@dxs-ts/eveli-fetch';
import { useNavigate } from '@tanstack/react-router';
import { EveliPermissions } from '@/eveli-permissions';




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
  const navigate = useNavigate();

  return (
    <EveliPermissions id='CREATE_TASK'>
      <div style={{ cursor: 'pointer' }} onClick={() => {
        navigate({
          from: '/secured/$locale/worker/tasks',
          to: '/secured/$locale/worker/tasks/create',
        });
      }}>
        <AddIcon />
      </div>
    </EveliPermissions>
  );
};

export const EveliTasks: React.FC = ({ }) => {
  const intl = useIntl();
  const theme = useTheme();
  const tableLocalization = useMaterialTableLabels();
  const tableContext = useContext(EveliTaskTableContext);
  const { isFirstRenderAfterRefresh, setRefreshed } = useRefresh();
  const { loadTasks } = useFetch('worker/rest/api/tasks.GET', {});


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
          },
          '& .MuiTableCell-root': {
            borderBottom: `1px solid ${theme.palette.divider}`
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
          return loadTasks(query, tableState.columns);
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

