import React, { useContext, useRef, useMemo, forwardRef, useEffect, useState } from 'react';
import MaterialTable, { Column, OrderByCollection, Query, QueryResult } from '@material-table/core';
import { Box, Link } from '@mui/material';
import LockIcon from '@mui/icons-material/Lock';
import MessageIcon from '@mui/icons-material/Message';
import RefreshIcon from '@mui/icons-material/Refresh';
import AddIcon from '@mui/icons-material/Add';

import { FormattedDate, useIntl } from 'react-intl';
import { localizeTable } from '../../util/localizeTable';
import { Link as RouterLink } from 'react-router-dom';
import moment from 'moment';
import { mapRolesList } from '../../util/rolemapper';

import { TableStateContext } from '../../context/TaskSessionContext';

import { Task, TaskPriority, TaskStatus } from '../../types/task/Task';
import { UserGroup } from '../../types/UserGroup';
import { TaskBackendContext } from '../../context/TaskApiConfigContext';
import { PriorityView } from '../../components/task/Priority';
import { StatusViewComponent } from '../../components/task/Status';

function getStatusCode(status: TaskStatus|undefined) {
  switch (status) {
  case TaskStatus.NEW : return 1;
  case TaskStatus.OPEN : return 2;
  case TaskStatus.COMPLETED : return 3;
  case TaskStatus.REJECTED : return 4;
  default: return 0;
  }
}

function getPriorityCode(status: TaskPriority|undefined) {
  switch (status) {
  case TaskPriority.LOW : return 1;
  case TaskPriority.NORMAL : return 2;
  case TaskPriority.HIGH : return 3;
  default: return 0;
  }
}

type Props = {
  loadTasks: (query:Query<Task>, columns:Column<any>[], defaultOrder?: OrderByCollection[])=>Promise<QueryResult<Task>>
  groups: UserGroup[]
  taskOpenHandler: (id?:number)=>void
  taskDeletableHandler?: (task:Task)=>boolean
  newTasks: number[]
}

interface TableState  {
  columns: Array<Column<Task>>;
}
type LinkProps = {
  title: string
  address?: string
  id?: number
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

export const TasksTable:React.FC<Props> = 
 ({loadTasks, groups, taskOpenHandler, taskDeletableHandler, newTasks}) =>{

  const taskBackend = useContext(TaskBackendContext);
  const intl = useIntl();
  const tableLocalization = localizeTable((id: string) => intl.formatMessage({ id }));
  const tableRef = useRef<any>();
  const tableContext = useContext(TableStateContext);
  const { isFirstRenderAfterRefresh, setRefreshed } = useRefresh();

  useEffect(() => {
    try {
      if(localStorage.getItem("filters")){
        tableContext.setFilters(JSON.parse(localStorage.getItem("filters") as string));
      }
    } catch (error) {
      console.error(error);
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if(tableContext.filters){
      localStorage.setItem("filters", JSON.stringify(tableContext.filters));
    }else{
      localStorage.removeItem("filters");
    }
  }, [tableContext.filters]);

  const addTask = () => {
    taskOpenHandler();
  }

  const deleteTask = (taskId:any) => {
    return taskBackend.deleteTask(taskId)
    .then(result=> {
      return result;
    })
  }

  const formatTime = (time:any) => {
    if (time) {
      return (
        <React.Fragment>
          <FormattedDate value={time} />
        </React.Fragment>
      )
    }
    return "-";
  }

  const formatDate = (time:any) => {
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

  const TaskLink:React.FC<LinkProps> = ({title, address, id, keywords}) => {
    const renderLink = useMemo(
      // @ts-ignore
      () => forwardRef((itemProps, ref) => <RouterLink to={{pathname:address}} ref={ref} {...itemProps} />),
      [address],
    );
    if (!address) {
      return (
        <React.Fragment>
          {title}
        </React.Fragment>
      );
    }
    const link = (
      <Link href="#" component={renderLink as any}>
        {title}
      </Link>
    );
    if (id && newTasks.includes(id)) {
      if (keywords && keywords[0].includes('Protected')) {
        return (
          <Box display="flex" alignItems="center" justifyContent="space-between">
            {link}
            <Box display="flex">
              <LockIcon color='secondary' fontSize='small'  />
              <MessageIcon color='secondary' fontSize='small' />
            </Box>
          </Box>
        )
      } else {
        return (
          <Box display="flex" alignItems="center" justifyContent="space-between">
            {link}
            <MessageIcon fontSize='small' color='secondary' />
          </Box>
        )
      }
    }
    if (keywords && keywords[0]?.includes('Protected')) {
      return (
        <Box display="flex" alignItems="center" justifyContent="space-between">
          {link}
          <LockIcon color='secondary' fontSize='small'  />
        </Box>
      )
    }
    return link;  
  }

  const orderCollection = tableContext.sort || [
    { orderBy: 0, orderByField: "priority", orderDirection: 'desc', sortOrder: 1 },
    { orderBy: 6, orderByField: "dueDate", orderDirection: 'asc', sortOrder: 2 }
  ];
  
  const onOrderCollectionChange = (orderByCollection:any) => {
    tableContext.setSort(orderByCollection);
  };

  const tableState: TableState = {
    columns: [
      {
        title: intl.formatMessage({id: 'spoTasksTableHeader.priority'}),
        field: 'priority',
        lookup: {
          'LOW': intl.formatMessage({id: 'task.priority.low'}),
          'NORMAL': intl.formatMessage({id: 'task.priority.normal'}),
          'HIGH': intl.formatMessage({id: 'task.priority.high'}),
        },
        headerStyle: { fontWeight: 'bold' },
        defaultFilter: tableContext.filters?.find((filter: any) => filter.column.field === 'priority')?.value || [],
        render: data => (<PriorityView withLabel value={data.priority} meta={{}} />),
        customSort: (a, b) => a.priority && b.priority ? getPriorityCode(a.priority) - getPriorityCode(b.priority) : 0,
        hidden: tableRef.current?.state.columns.find((column: any) => column.field === "priority").hidden
      },
      {
        title: intl.formatMessage({id: 'spoTasksTableHeader.taskName'}),
        field: 'subject',
        headerStyle: { fontWeight: 'bold' },
        defaultFilter:  tableContext.filters?.find((filter: any) => filter.column.field === 'subject')?.value || "",
        render: data => <TaskLink title={(data.subject || '') + ' ' + (data.taskRef || '') || '-'} address={`/ui/tasks/task/${data.id}`} id={data.id} keywords={data?.keyWords}/>,
        hidden: tableRef.current?.state.columns.find((column: any) => column.field === "subject").hidden
      },
      {
        title: intl.formatMessage({id: 'spoTasksTableHeader.clientName'}),
        field: 'clientIdentificator',
        defaultFilter:  tableContext.filters?.find((filter: any) => filter.column.field === 'clientIdentificator')?.value || "",
        headerStyle: { fontWeight: 'bold' },
        hidden: tableRef.current?.state.columns.find((column: any) => column.field === "clientIdentificator").hidden
      },
      {
        title: intl.formatMessage({id: 'spoTasksTableHeader.status'}),
        field: 'status',
        lookup: {
          'NEW': intl.formatMessage({id: 'task.status.new'}),
          'OPEN': intl.formatMessage({id: 'task.status.open'}),
          'REJECTED': intl.formatMessage({id: 'task.status.rejected'}),
          'COMPLETED': intl.formatMessage({id: 'task.status.completed'}),
        },
        defaultFilter: tableContext.filters?.find((filter: any) => filter.column.field === 'status')?.value || ['NEW', 'OPEN'],
        headerStyle: { fontWeight: 'bold' },
        render: data => (<StatusViewComponent withLabel value={data.status} meta={{}}/>),
        customSort: (a, b) => a.status && b.status ? getStatusCode(a.status) - getStatusCode(b.status) : 0,
        hidden: tableRef.current?.state.columns.find((column: any) => column.field === "status").hidden
      },
      {
        title: intl.formatMessage({id: 'spoTasksTableHeader.assigned'}),
        field: 'assignedRoles',
        headerStyle: { fontWeight: 'bold' },
        defaultFilter: tableContext.filters?.find((filter: any) => filter.column.field === 'assignedRoles')?.value || '',
        render: data => mapRolesList(data.assignedRoles).join(),
        sorting: false,
        hidden: tableRef.current?.state.columns.find((column: any) => column.field === "assignedRoles").hidden
      },
      {
        title: intl.formatMessage({id: 'spoTasksTableHeader.assignedUser'}),
        field: 'assignedUser',
        defaultFilter: tableContext.filters?.find((filter: any) => filter.column.field === 'assignedUser')?.value || '',
        headerStyle: { fontWeight: 'bold' },
        hidden: tableRef.current?.state.columns.find((column: any) => column.field === "assignedUser").hidden
      },
      {
        title: intl.formatMessage({id: 'spoTasksTableHeader.dueDate'}),
        field: 'dueDate',
        filtering: false,
        headerStyle: { fontWeight: 'bold' },
        type: 'date',
        render: data => formatTime(data.dueDate),
        hidden: tableRef.current?.state.columns.find((column: any) => column.field === "dueDate").hidden
      },
      {
        title: intl.formatMessage({id: 'spoTasksTableHeader.created'}),
        field: 'created',
        filtering: false,
        render: data => formatDate(data.created),
        headerStyle: { fontWeight: 'bold' },
        hidden: tableRef.current?.state.columns.find((column: any) => column.field === "created").hidden 
      }
    ]
  };

  return (
    <Box>
        <MaterialTable
          tableRef = {tableRef}
          icons={{ Filter: forwardRef(() => <div />) }}
          title={intl.formatMessage({id: 'tasksView.title'})}
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
              icon: () => <RefreshIcon />,
              isFreeAction: true,
              tooltip: intl.formatMessage({id: 'taskButton.refresh'}),
              onClick: () => tableRef.current.onQueryChange()
            },
            {
              icon: AddIcon,
              isFreeAction: true,
              tooltip: intl.formatMessage({id: 'taskButton.addTask'}),
              onClick: addTask
            }
          ]}
          editable={{
            isDeleteHidden: rowData=> taskDeletableHandler ? !taskDeletableHandler(rowData) : false,
            onRowDelete: oldData => {
              return deleteTask(oldData.id);
            }
          }}
          data={ query => {
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
          onChangeColumnHidden={(hiddenColumn: Column<Task>, hidden: boolean) => {
            if(tableRef.current)
              tableRef.current.onQueryChange();
          }}
        />
    </Box>
  );
}
