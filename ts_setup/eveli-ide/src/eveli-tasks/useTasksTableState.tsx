import React from "react";
import { useIntl, FormattedDate } from "react-intl";
import { Column } from '@material-table/core';
import { IconButton } from '@mui/material';
import DeleteForeverIcon from '@mui/icons-material/DeleteForever';
import moment from 'moment'; // TODO dead library

import { useFetch } from '@dxs-ts/eveli-fetch';
import { TaskApi } from '../api-task';

import { EveliTaskTableContext } from "./EveliTaskTableProvider";
import { TaskLink } from "./TaskLink";
import { TaskStatusIndicator } from "./TaskStatusIndicator";
import { TaskPriorityIndicator } from "./TaskPriorityIndicator";
import { EveliPermissions } from "@/eveli-permissions";
import { TaskAdditionalInfo } from "./TaskAdditionalInfo";


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

function getStatusCode(status: TaskApi.TaskStatus | undefined) {
  switch (status) {
    case TaskApi.TaskStatus.NEW: return 1;
    case TaskApi.TaskStatus.OPEN: return 2;
    case TaskApi.TaskStatus.COMPLETED: return 3;
    case TaskApi.TaskStatus.REJECTED: return 4;
    case TaskApi.TaskStatus.WAITING: return 5;
    case TaskApi.TaskStatus.DELEGATED: return 6;
    case TaskApi.TaskStatus.TRANSFERRED: return 7;
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

export interface TableState {
  columns: Array<Column<TaskApi.Task>>;
  tableRef: React.MutableRefObject<any>;
}

export function useTasksTableState(): TableState {
  const { deleteTask } = useFetch('worker/rest/api/tasks/$taskId.DELETE', {});
  const { groups } = useFetch('$org/groupsList.GET', {});
  const intl = useIntl();
  const tableContext = React.useContext(EveliTaskTableContext);
  const tableRef = React.useRef<any>();

  const mapRoleToGroupName = (role: string):string => {
    if (role) {
      return groups?.find(grp=> grp.id === role)?.groupName || role;
    }
    return role;
  }
  const mapRolesToGroupNames = (roles?: string[]|null):string[] => {
    if (roles) {
      return roles?.map(role => mapRoleToGroupName(role))
    }
    return [];
  }

  return {
    tableRef,
    columns: [
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
        render: data => (<TaskPriorityIndicator value={data.priority} />),
        customSort: (a, b) => a.priority && b.priority ? getPriorityCode(a.priority) - getPriorityCode(b.priority) : 0,
        hidden: tableRef.current?.state.columns.find((column: any) => column.field === "priority").hidden
      },
      {
        title: intl.formatMessage({ id: 'spoTasksTableHeader.taskName' }),
        field: 'subject',
        headerStyle: { fontWeight: 'bold' },
        defaultFilter: tableContext.filters?.find((filter: any) => filter.column.field === 'subject')?.value || "",
        render: data => <TaskLink title={data.subject || '-'} id={data.id} keywords={data?.keyWords} />,
        hidden: tableRef.current?.state.columns.find((column: any) => column.field === "subject").hidden,
      },
      {
        title: 'Reference ID',
        field: 'taskRef',
        filtering: true,
        hidden: true
      },      
      {
        title: intl.formatMessage({ id: 'spoTasksTableHeader.additionalInfo' }),
        field: 'additionalInfo',
        filtering: true,
        render: data => (<TaskAdditionalInfo task={data} />),
        defaultFilter: tableContext.filters?.find((filter: any) => filter.column.field === 'additionalInfo')?.value || "",
        headerStyle: { fontWeight: 'bold' },
        hidden: tableRef.current?.state.columns.find((column: any) => column.field === "additionalInfo").hidden
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
          'TRANSFERRED': intl.formatMessage({ id: 'task.status.transferred' }),
          'DELEGATED': intl.formatMessage({ id: 'task.status.delegated' }),
          'WAITING':  intl.formatMessage({ id: 'task.status.waiting' }),
        },
        defaultFilter: tableContext.filters?.find((filter: any) => filter.column.field === 'status')?.value || ['NEW', 'OPEN'],
        headerStyle: { fontWeight: 'bold' },
        render: data => (<TaskStatusIndicator value={data.status} />),
        customSort: (a, b) => a.status && b.status ? getStatusCode(a.status) - getStatusCode(b.status) : 0,
        hidden: tableRef.current?.state.columns.find((column: any) => column.field === "status").hidden
      },
      {
        title: intl.formatMessage({ id: 'spoTasksTableHeader.assigned' }),
        field: 'assignedRoles',
        headerStyle: { fontWeight: 'bold' },
        defaultFilter: tableContext.filters?.find((filter: any) => filter.column.field === 'assignedRoles')?.value || '',
        render: data => mapRolesToGroupNames(data.assignedRoles).join(),
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
      },
      {
        render: (data) => {
          return (<EveliPermissions id='DELETE_TASK'>
            <div onClick={() => {
              deleteTask(data.id!).then(() => {
                tableRef.current.onQueryChange();
              });

            }}>
              <IconButton><DeleteForeverIcon color='error' /></IconButton></div>
          </EveliPermissions>
          )
        },
      }
    ]
  }
}