import React from 'react';
import { Tooltip } from '@mui/material';
import { ErrorOutline as ErrorOutlineIcon } from '@mui/icons-material';
import { ColumnDef, sortingFns } from '@tanstack/react-table';
import { useIntl, FormattedMessage } from 'react-intl';
import { DateTime } from 'luxon';

import { StencilComposerApi as Composer } from '@dxs-ts/stencil-api';

import { WithTableStyles } from '@dxs-ts/xui-table';

import type { CellContext } from '@tanstack/react-table';

export const ArticleTimestampsTable: React.FC = () => {
  const intl = useIntl();
  const { site, session } = Composer.useComposer();
  const locales = Object.values(site.locales);
  const articles = Object.values(site.articles);
  const pages = Object.values(site.articlePages);

  // Find the corresponding page for a given article and locale
  const getPage = (articleId: string, localeId: string) =>
    pages.find(p => p.body.article === articleId && p.body.locale === localeId);

  type RowData = {
    id: string;
    name: string;
    timestamps: { [localeCode: string]: Date | null };
  };

  // Transform articles into row data with localized timestamps
  const rows: RowData[] = articles.map(article => {
    const timestamps: { [key: string]: Date | null } = {};
    locales.forEach(locale => {
      const page = getPage(article.id, locale.id);
      const updated = page ? session.getLastUpdated(page.id) : null;
      timestamps[locale.body.value] = updated ? new Date(updated) : null;
    });
    return {
      id: article.id,
      name: article.body.name,
      timestamps,
    };
  });

  const columns: ColumnDef<RowData>[] = [
    {
      id: 'name',
      header: intl.formatMessage({ id: 'article.name' }),
      accessorKey: 'name',
      enableColumnFilter: false,
      enableSorting: true,
      size: 350,
      minSize: 300,
    },
    ...locales.map(locale => ({
      id: locale.body.value,
      header: locale.body.value,
      accessorFn: (row: RowData) => row.timestamps[locale.body.value],
      enableColumnFilter: false,
      enableSorting: true,
      enableHiding: true,
      sortingFn: sortingFns.datetime,
      size: 180,
      minSize: 160,
      cell: (info: CellContext<RowData, unknown>) => {
        const value = info.getValue() as Date | null;
        return value
          ? DateTime.fromJSDate(value).setLocale('fi').toFormat('d.M.yyyy HH:mm')
          : (
            <Tooltip title={<FormattedMessage id="locales.nopage" />}>
              <ErrorOutlineIcon sx={{ color: 'error.main' }} />
            </Tooltip>
          );
      }
    }))
    ,
  ];

  return (
    <>
      <WithTableStyles
        columns={columns}
        data={rows}
        options={{ 
          initialPageSize: 30, 
          tableId: 'stencil-content-assists' 
        }}
      />
    </>
  );
};
