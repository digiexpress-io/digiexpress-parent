import React, { useContext, useRef, useMemo, forwardRef, useEffect, useState } from 'react';
import { Box, IconButton, Link, LinkProps as MuiLinkProps } from '@mui/material';
import MaterialTable, { Column, OrderByCollection, Query, QueryResult } from '@material-table/core';
import LockIcon from '@mui/icons-material/Lock';
import EmojiPeopleOutlinedIcon from '@mui/icons-material/EmojiPeopleOutlined';
import RefreshIcon from '@mui/icons-material/Refresh';
import AddIcon from '@mui/icons-material/Add';
import DeleteForeverIcon from '@mui/icons-material/DeleteForever';
import { Link as RouterLink } from '@tanstack/react-router'

import { FormattedDate, useIntl } from 'react-intl';
import moment from 'moment';

import { localizeTable } from '../../util/localizeTable';
import { IamApi, TaskApi, mapIamRolesList } from '@/burger';

import { TableStateContext } from '../../context/TaskSessionContext';


import { PriorityView } from '../../components/task/Priority';
import { StatusViewComponent } from '../../components/task/Status';
import { TableHeader } from '../../components/TableHeader';
import { useFetch } from '@dxs-ts/eveli-fetch';


function getStatusCode(status: TaskApi.TaskStatus | undefined) {
  switch (status) {
    case TaskApi.TaskStatus.NEW: return 1;
    case TaskApi.TaskStatus.OPEN: return 2;
    case TaskApi.TaskStatus.COMPLETED: return 3;
    case TaskApi.TaskStatus.REJECTED: return 4;
    default: return 0;
  }
}

function getPriorityCode(status: TaskApi.TaskPriority | undefined) {
  switch (status) {
    case TaskApi.TaskPriority.LOW: return 1;
    case TaskApi.TaskPriority.NORMAL: return 2;
    case TaskApi.TaskPriority.HIGH: return 3;
    default: return 0;
  }
}

type Props = {
  loadTasks: (query: Query<TaskApi.Task>, columns: Column<any>[], defaultOrder?: OrderByCollection[]) => Promise<QueryResult<TaskApi.Task>>
  groups: IamApi.UserGroup[]
  taskOpenHandler: (id?: string) => void
  taskDeletableHandler?: () => boolean
  newTasks: string[]
}

interface TableState {
  columns: Array<Column<TaskApi.Task>>;
}


type LinkProps = {
  title: string
  id?: string
  keywords?: string[]
}

interface UseRefreshReturnType {
  isFirstRenderAfterRefresh: boolean;
  setRefreshed: React.Dispatch<React.SetStateAction<boolean>>;
}

const useRefresh = (): UseRefreshReturnType => {
  const [isFirstRenderAfterRefresh, setIsFirstRenderAfterRefresh] = useState(true);
  const setRefreshed = useRef(setIsFirstRenderAfterRefresh);

  return { isFirstRenderAfterRefresh, setRefreshed: setRefreshed.current };
};

export const TasksTable: React.FC<Props> =
  ({ loadTasks, groups, taskOpenHandler, taskDeletableHandler, newTasks }) => {


    const intl = useIntl();
    const tableLocalization = localizeTable((id: string) => intl.formatMessage({ id }));
    const tableRef = useRef<any>();
    const tableContext = useContext(TableStateContext);
    const { isFirstRenderAfterRefresh, setRefreshed } = useRefresh();
    const { deleteTask } = useFetch('worker/rest/api/tasks/$taskId.DELETE', {});

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

    const addTask = () => {
      taskOpenHandler();
    }

    const formatTime = (time: any) => {
      if (time) {
        return (
          <React.Fragment>
            <FormattedDate value={time} />
          </React.Fragment>
        )
      }
      return "-";
    }

    const formatDate = (time: any) => {
      if (time) {
        const localTime = moment.utc(time).local().toDate();
        return (
          <React.Fragment>
            <FormattedDate value={localTime} />
          </React.Fragment>
        )
      }
      return "-";
    }

    const TaskLink: React.FC<LinkProps> = ({ title, id, keywords }) => {
      const renderLink = useMemo(
        () => forwardRef<any, MuiLinkProps>((itemProps, ref) => <RouterLink
            ref={ref}
            from='/secured/$locale/worker'
            to='/secured/$locale/worker/tasks/$taskId' 
            params={{ taskId: `${id}`}} 
            children={itemProps.children}
          />),
        [],
      );
      const link = (
        <Link href="#" component={renderLink as any}>
          {title}
        </Link>
      );
      if (id && newTasks.includes(id)) {
        if (keywords && keywords[0]?.includes('Protected')) {
          return (
            <Box display="flex" alignItems="center" justifyContent="space-between">
              {link}
              <Box display="flex">
                <LockIcon color='secondary' fontSize='small' sx={{ marginLeft: 1 }} />
                <EmojiPeopleOutlinedIcon color='primary' fontSize='small' sx={{ marginLeft: 1 }} />
              </Box>
            </Box>
          )
        } else {
          return (
            <Box display="flex" alignItems="center" justifyContent="space-between">
              {link}
              <EmojiPeopleOutlinedIcon fontSize='small' color='primary' sx={{ marginLeft: 1 }} />
            </Box>
          )
        }
      }
      if (keywords && keywords[0]?.includes('Protected')) {
        return (
          <Box display="flex" alignItems="center" justifyContent="space-between">
            {link}
            <LockIcon color='primary' fontSize='small' sx={{ marginLeft: 1 }} />
          </Box>
        )
      }
      return link;
    }

    const orderCollection = tableContext.sort || [
      { orderBy: 0, orderByField: "priority", orderDirection: 'desc', sortOrder: 1 },
      { orderBy: 6, orderByField: "dueDate", orderDirection: 'asc', sortOrder: 2 }
    ];

    const onOrderCollectionChange = (orderByCollection: any) => {
      tableContext.setSort(orderByCollection);
    };
    const isDeleteHidden: boolean | undefined = taskDeletableHandler ? !taskDeletableHandler() : false;


    const tableState: TableState = {
      columns: [
        {
          render: (data) => {
            return <div onClick={() => deleteTask(data.id!)}><IconButton color='error'><DeleteForeverIcon /></IconButton></div>;
          },
          hidden: isDeleteHidden
        },

        {
          title: intl.formatMessage({ id: 'spoTasksTableHeader.priority' }),
          field: 'priority',
          lookup: {
            'LOW': intl.formatMessage({ id: 'task.priority.low' }),
            'NORMAL': intl.formatMessage({ id: 'task.priority.normal' }),
            'HIGH': intl.formatMessage({ id: 'task.priority.high' }),
          },
          headerStyle: { fontWeight: 'bold' },
          defaultFilter: tableContext.filters?.find((filter: any) => filter.column.field === 'priority')?.value || [],
          render: data => (<PriorityView withLabel value={data.priority} meta={{}} />),
          customSort: (a, b) => a.priority && b.priority ? getPriorityCode(a.priority) - getPriorityCode(b.priority) : 0,
          hidden: tableRef.current?.state.columns.find((column: any) => column.field === "priority").hidden
        },
        {
          title: intl.formatMessage({ id: 'spoTasksTableHeader.taskName' }),
          field: 'subject',
          headerStyle: { fontWeight: 'bold' },
          defaultFilter: tableContext.filters?.find((filter: any) => filter.column.field === 'subject')?.value || "",
          render: data => <TaskLink title={(data.subject || '') + ' ' + (data.taskRef || '') || '-'} id={data.id} keywords={data?.keyWords} />,
          hidden: tableRef.current?.state.columns.find((column: any) => column.field === "subject").hidden,

        },
        {
          title: intl.formatMessage({ id: 'spoTasksTableHeader.clientName' }),
          field: 'clientIdentificator',
          defaultFilter: tableContext.filters?.find((filter: any) => filter.column.field === 'clientIdentificator')?.value || "",
          headerStyle: { fontWeight: 'bold' },
          hidden: tableRef.current?.state.columns.find((column: any) => column.field === "clientIdentificator").hidden
        },
        {
          title: intl.formatMessage({ id: 'spoTasksTableHeader.status' }),
          field: 'status',
          lookup: {
            'NEW': intl.formatMessage({ id: 'task.status.new' }),
            'OPEN': intl.formatMessage({ id: 'task.status.open' }),
            'REJECTED': intl.formatMessage({ id: 'task.status.rejected' }),
            'COMPLETED': intl.formatMessage({ id: 'task.status.completed' }),
          },
          defaultFilter: tableContext.filters?.find((filter: any) => filter.column.field === 'status')?.value || ['NEW', 'OPEN'],
          headerStyle: { fontWeight: 'bold' },
          render: data => (<StatusViewComponent withLabel value={data.status} meta={{}} />),
          customSort: (a, b) => a.status && b.status ? getStatusCode(a.status) - getStatusCode(b.status) : 0,
          hidden: tableRef.current?.state.columns.find((column: any) => column.field === "status").hidden
        },
        {
          title: intl.formatMessage({ id: 'spoTasksTableHeader.assigned' }),
          field: 'assignedRoles',
          headerStyle: { fontWeight: 'bold' },
          defaultFilter: tableContext.filters?.find((filter: any) => filter.column.field === 'assignedRoles')?.value || '',
          render: data => mapIamRolesList(data.assignedRoles).join(),
          sorting: false,
          hidden: tableRef.current?.state.columns.find((column: any) => column.field === "assignedRoles").hidden
        },
        {
          title: intl.formatMessage({ id: 'spoTasksTableHeader.assignedUser' }),
          field: 'assignedUser',
          defaultFilter: tableContext.filters?.find((filter: any) => filter.column.field === 'assignedUser')?.value || '',
          headerStyle: { fontWeight: 'bold' },
          hidden: tableRef.current?.state.columns.find((column: any) => column.field === "assignedUser").hidden
        },
        {
          title: intl.formatMessage({ id: 'spoTasksTableHeader.dueDate' }),
          field: 'dueDate',
          filtering: false,
          headerStyle: { fontWeight: 'bold' },
          type: 'date',
          render: data => formatTime(data.dueDate),
          //hidden: tableRef.current?.state.columns.find((column: any) => column.field === "dueDate").hidden
        },
        {
          title: intl.formatMessage({ id: 'spoTasksTableHeader.created' }),
          field: 'created',
          filtering: false,
          render: data => formatDate(data.created),
          headerStyle: { fontWeight: 'bold' },
          hidden: tableRef.current?.state.columns.find((column: any) => column.field === "created").hidden
        }
      ]
    };



    return (
      <MaterialTable
        tableRef={tableRef}
        icons={{ Filter: forwardRef(() => <div />) }}
        title={<TableHeader id='tasksView.title' />}
        localization={tableLocalization}
        columns={tableState.columns}
        options={{
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
          showColumnSortOrder: true
        }}
        actions={[
          {
            icon: RefreshIcon,
            isFreeAction: true,
            tooltip: intl.formatMessage({ id: 'taskButton.refresh' }),
            onClick: () => tableRef.current.onQueryChange()
          },
          {
            icon: AddIcon,
            isFreeAction: true,
            tooltip: intl.formatMessage({ id: 'taskButton.addTask' }),
            onClick: addTask
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
        onChangeColumnHidden={(hiddenColumn: Column<TaskApi.Task>, hidden: boolean) => {
          if (tableRef.current)
            tableRef.current.onQueryChange();
        }}
      />
    );
  }
