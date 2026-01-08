
import React from 'react';
import { ColumnDef } from '@tanstack/react-table';
import { useIntl } from 'react-intl';

import { WithTableStyles } from '@dxs-ts/xui-table';
import { DashboardItem, useDialobForms } from '@dxs-ts/eveli-api';
import { FormTableDateTime } from '../form-table-date-time';
import { filterDateGte_lastSaved, filterDateGte_latestTagDate } from '../form-table-filters';
import { FormTableDownloadAll } from '../form-table-download-all';
import { FormTableToolbarRow } from '../form-table-toolbar-row';
import { FormTableTitleRow } from '../form-table-title-row';
import { FormTableLabelRow } from '../form-table-label-row';





export const FormTable: React.FC<{}> = () => {

  const { forms } = useDialobForms();
  const intl = useIntl();

  const columns: ColumnDef<DashboardItem, any>[] = [

    {
      header: intl.formatMessage({ id: 'adminUI.formConfiguration.label' }),
      accessorKey: 'metadata.label',
      size: 500,
      minSize: 500,
      enableSorting: true,
      enableResizing: true,
      enableColumnFilter: true,
      cell: info => <FormTableTitleRow value={info.row.original} />,
    },
    {
      header: intl.formatMessage({ id: 'adminUI.formConfiguration.latestTagName' }),
      accessorKey: 'latestTagName',
      size: 50,
      minSize: 50,
      enableSorting: true,
      enableResizing: true,
      enableColumnFilter: true,
    },
    {
      header: intl.formatMessage({ id: 'adminUI.formConfiguration.latestTagDate' }),
      accessorKey: 'latestTagDate',
      size: 100,
      minSize: 100,
      enableSorting: true,
      enableResizing: true,
      enableColumnFilter: true,
      meta: { enableDate: true },
      filterFn: filterDateGte_latestTagDate,
      cell: info => <FormTableDateTime value={info.getValue()} />,
    },
    {
      header: intl.formatMessage({ id: 'adminUI.formConfiguration.lastSaved' }),
      accessorKey: 'metadata.lastSaved',
      size: 100,
      minSize: 100,
      enableSorting: true,
      enableResizing: true,
      enableColumnFilter: true,
      meta: { enableDate: true },
      filterFn: filterDateGte_lastSaved,
      cell: info => <FormTableDateTime value={info.getValue()} />,
    },
    {
      header: intl.formatMessage({ id: 'adminUI.formConfiguration.labels' }),
      accessorKey: 'metadata.labels',
      size: 400,
      minSize: 400,
      enableSorting: true,
      enableResizing: true,
      enableColumnFilter: true,
      meta: {
        enableSelection: true
      },
      filterFn: (row, _columnId, filterValue) => {
        const labels: string[] = row.original.metadata?.labels ?? [];
        if (!filterValue || filterValue.length === 0) {
          return true;
        }
        return (
          filterValue.some((val: string) => labels.includes(val))
        );
      },
      cell: row => <FormTableLabelRow value={row.row.original} />,
    },
    {
      header: '',
      accessorKey: 'metadata.tools',
      size: 100,
      enableColumnFilter: false,
      enableSorting: false,
      minSize: 100,
      cell: row => <FormTableToolbarRow value={row.row.original} />,
    }
  ]

  return (
    <WithTableStyles
      data={forms} columns={columns} options={{ tableId: 'dialob_dashboard' }}
      slots={{ drawer: { 'export-data': FormTableDownloadAll } }}
    />
  );
}