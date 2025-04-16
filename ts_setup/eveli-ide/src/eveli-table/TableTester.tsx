import React from 'react';
import { ColumnDef, createColumnHelper, flexRender, getCoreRowModel, getSortedRowModel, Row, SortingState, useReactTable } from '@tanstack/react-table'
import { useFetch } from '@dxs-ts/eveli-fetch';
import { TaskApi } from '@/api-task';
import { EveliTable, EveliTableCell, EveliTableHeaderCell1 } from './EveliTable';
import { EveliTableHeaderRoot, EveliTableRowRoot } from './useUtilityClasses';
import { EveliTableDrawerButtonColumn } from './EveliTableDrawerButtonColumn';
import { Box } from '@mui/system';


const priorityOrder: Record<string, number> = {
  LOW: 0,
  NORMAL: 1,
  HIGH: 2,
};


function prioritySortingFn(rowA: Row<TaskApi.Task>, rowB: Row<TaskApi.Task>, columnId: string) {
  const a = priorityOrder[String(rowA.original[columnId as keyof TaskApi.Task])] ?? -1;
  const b = priorityOrder[String(rowB.original[columnId as keyof TaskApi.Task])] ?? -1;
  if (a > b) return 1;
  if (a < b) return -1;
  return 0;
}

export const TableTester: React.FC = () => {
  const { getTasks } = useFetch('worker/rest/api/tasks.GET', {})
  const [data, setData] = React.useState<TaskApi.Task[]>([]);
  const [sorting, setSorting] = React.useState<SortingState>([])

  React.useEffect(() => {
    getTasks().then(results => {
      setData(results.data)
    });
  }, []);



  const columnHelper = createColumnHelper<TaskApi.Task>();

  const columns: ColumnDef<TaskApi.Task, any>[] = [

    {
      header: 'Priority',
      accessorKey: 'priority',
      enableSorting: true,
      sortingFn: prioritySortingFn,
      footer: 'footer 1'
    },
    {
      header: 'Name',
      accessorKey: 'subject',
    },
    {
      header: 'Info',
      accessorKey: 'additionalInfo',
      enableSorting: false
    },
    {
      header: 'Client',
      accessorKey: 'clientIdentificator',
      footer: 'footer 2'
    },
    {
      header: 'Status',
      accessorKey: 'status',
      enableSorting: true,
    },
    {
      header: 'Roles',
      accessorKey: 'assignedRoles',
    },
    {
      header: 'Assignee',
      accessorKey: 'assignedUser',
    },
    {
      header: 'Due',
      accessorKey: 'dueDate',
    },
    {
      header: 'Created',
      accessorKey: 'created',
    },

  ]

  const table = useReactTable({
    columns,
    data,
    getCoreRowModel: getCoreRowModel(),
    getSortedRowModel: getSortedRowModel(),
    onSortingChange: setSorting,
    state: {
      sorting,
    },
  })
  console.log("xxx", table.getState().sorting)

  return (
    <Box display='flex'>
      <EveliTable>
        {table.getHeaderGroups().map(headerGroup => (
          <EveliTableHeaderRoot key={headerGroup.id}>
            {headerGroup.headers.map(header => (
              <EveliTableHeaderCell1
                key={header.id}
                column={header.column}
                sortDirection={header.column.getIsSorted()}
              >
                {header.isPlaceholder ? null : flexRender(header.column.columnDef.header, header.getContext())}
              </EveliTableHeaderCell1>
            ))}
          </EveliTableHeaderRoot>
        ))}
        {table.getRowModel().rows.map(row => (
          <EveliTableRowRoot key={row.id}>
            {row.getVisibleCells().map(cell => (
              <EveliTableCell key={cell.id}>
                {flexRender(cell.column.columnDef.cell, cell.getContext())}
              </EveliTableCell>
            ))}
          </EveliTableRowRoot>
        ))}
        <tfoot>
          {table.getFooterGroups().map(footerGroup => (
            <tr key={footerGroup.id}>
              {footerGroup.headers.map(header => (
                <th key={header.id}>
                  {header.isPlaceholder
                    ? null
                    : flexRender(
                      header.column.columnDef.footer,
                      header.getContext()
                    )}
                </th>
              ))}
            </tr>
          ))}
        </tfoot>

      </EveliTable>
      <EveliTableDrawerButtonColumn onColumnsClick={() => { }} onFiltersClick={() => { }} />
    </Box >
  )

}
