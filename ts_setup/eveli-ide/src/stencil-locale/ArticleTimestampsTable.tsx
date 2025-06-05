import React from 'react';
import { Tooltip } from '@mui/material';
import ErrorOutlineIcon from '@mui/icons-material/ErrorOutline';
import { ColumnDef } from '@tanstack/react-table';
import { useIntl, FormattedMessage } from 'react-intl';

import { StencilComposerApi as Composer } from '@/stencil-setup';
import { WithTableStyles } from '@/eveli-table';

import type { CellContext } from '@tanstack/react-table';

export const ArticleTimestampsTable: React.FC = () => {
  const intl = useIntl();
  const { site, session } = Composer.useComposer();
  const locales = Object.values(site.locales);
  const articles = Object.values(site.articles);
  const pages = Object.values(site.pages);

  // Find the corresponding page for a given article and locale
  const getPage = (articleId: string, localeId: string) =>
    pages.find(p => p.body.article === articleId && p.body.locale === localeId);

  type RowData = {
    id: string;
    name: string;
    [localeCode: string]: string | null;
  };

  // Transform articles into row data with localized timestamps
  const rows: RowData[] = articles.map(article => {
    const row: RowData = {
      id: article.id,
      name: article.body.name,
    };

    locales.forEach(locale => {
      const page = getPage(article.id, locale.id);
      const updated = page ? session.getLastUpdated(page.id) : null;
      row[locale.body.value] = updated ? new Date(updated).toLocaleDateString() : null;
    });

    return row;
  });

  // Define columns: first is article name, others are per-locale
  const columns: ColumnDef<RowData>[] = [
    {
      header: () => intl.formatMessage({ id: 'article.name' }),
      accessorKey: 'name',
      enableColumnFilter: false,
      enableSorting: true,
    },
    ...locales.map(locale => ({
      header: locale.body.value,
      accessorKey: locale.body.value,
      enableColumnFilter: false,
      enableSorting: true,
      enableHiding: true,
      cell: (info: CellContext<RowData, unknown>) => {
        const value = info.getValue();
        return value ? value : (
          <Tooltip title={<FormattedMessage id="locales.nopage" />}>
            <ErrorOutlineIcon sx={{ color: 'error.main' }} />
          </Tooltip>
        );
      }      
    })),
  ];

  return (
    <>
      <WithTableStyles
        columns={columns}
        data={rows}
        options={{ initialPageSize: 30 }}
      />
    </>
  );
};
