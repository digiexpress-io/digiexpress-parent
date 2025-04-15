import React from 'react';
import { ColumnDef, flexRender, getCoreRowModel, useReactTable } from '@tanstack/react-table'
import { useFetch } from '@dxs-ts/eveli-fetch';
import { TaskApi } from '@/api-task';
import { EveliTable, EveliTableCell, EveliTableHeaderCell1 } from './EveliTable';
import { EveliTableHeaderRoot, EveliTableRowRoot } from './useUtilityClasses';




export const TableTester: React.FC = () => {
  const { getTasks } = useFetch('worker/rest/api/tasks.GET', {})
  const [data, setData] = React.useState<TaskApi.Task[]>([]);

  React.useEffect(() => {
    getTasks().then(results => {
      setData(results.data)
    });
  }, []);





  const columns: ColumnDef<TaskApi.Task, any>[] = [
    {
      header: 'Priority',
      accessorKey: 'priority',
    },
    {
      header: 'Name',
      accessorKey: 'subject',
    },
    {
      header: 'Info',
      accessorKey: 'additionalInfo',
    },
    {
      header: 'Client',
      accessorKey: 'clientIdentificator',
    },
    {
      header: 'Status',
      accessorKey: 'status',
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
    }, {
      header: 'Created',
      accessorKey: 'created',
    },
  ]

  const table = useReactTable({ columns, data, getCoreRowModel: getCoreRowModel() })
  console.log("table", data, table.firstPage)

  return (
    <EveliTable>
      {table.getHeaderGroups().map(headerGroup => (
        <EveliTableHeaderRoot key={headerGroup.id}>
          {headerGroup.headers.map(header => (
            <EveliTableHeaderCell1 key={header.id}>
              {header.isPlaceholder ? null : flexRender(
                header.column.columnDef.header,
                header.getContext()
              )}
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
    </EveliTable>)
}
