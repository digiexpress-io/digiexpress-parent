
import React from 'react';
import { ColumnDef } from '@tanstack/react-table';

import { WithTableStyles } from '@/eveli-table';
import { useIntl } from 'react-intl';
import { DasboardItem, useDialobForms } from '@/api-dialob-form';
import { FormTableDateTime } from '../form-table-date-time';
import { filterDateGte_lastSaved, filterDateGte_latestTagDate } from '../form-table-filters';
import { FormTableDownloadAll } from '../form-table-download-all';





export const FormTable: React.FC<{}> = () => {

  const { forms } = useDialobForms();
  const intl = useIntl();

  const columns: ColumnDef<DasboardItem, any>[] = [

    {
      header: intl.formatMessage({ id: 'adminUI.formConfiguration.label' }),
      accessorKey: 'metadata.label',
      size: 200,
      minSize: 200,
      enableSorting: true,
      enableResizing: true,
      enableColumnFilter: true,
    },
    {
      header: intl.formatMessage({ id: 'adminUI.formConfiguration.latestTagName' }),
      accessorKey: 'latestTagName',
      size: 100,
      minSize: 100,
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
      meta: { enableDateGTE: true },
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
      meta: { enableDateGTE: true },
      filterFn: filterDateGte_lastSaved,
      cell: info => <FormTableDateTime value={info.getValue()} />,
    },
    {
      header: intl.formatMessage({ id: 'adminUI.formConfiguration.labels' }),
      accessorKey: 'metadata.labels',
      size: 100,
      minSize: 100,
      enableSorting: true,
      enableResizing: true,
      enableColumnFilter: true,
    }
  ]

  return (
    <WithTableStyles 
      data={forms} columns={columns} options={{ tableId: 'dialob_dashboard' }}
      slots={{ drawer: { 'export-data': FormTableDownloadAll }}}
    />
  );
}