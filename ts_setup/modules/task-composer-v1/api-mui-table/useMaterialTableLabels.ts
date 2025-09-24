import { Localization } from "@material-table/core";
import { useIntl } from "react-intl";

export function useMaterialTableLabels(): Localization { 
  const intl = useIntl();
  const t = (id: string) => intl.formatMessage({ id })

  return {
    body: {
      emptyDataSourceMessage: t('table.body.emptyDataSourceMessage'),
      deleteTooltip: t('table.body.deleteTooltip'),
      filterRow: {
        filterTooltip: t('table.body.filterRow.filterTooltip'),
      },
      editRow: {
        deleteText: t('table.body.editRow.deleteMessage'),
        cancelTooltip:  t('button.cancel'),
        saveTooltip: t('button.accept'),
      },
    },
    header: {
      actions: t('table.header.actions')
    },
    pagination: {
      labelDisplayedRows: t('table.pagination.labelDisplayedRows'),
      labelRowsPerPage: t('table.pagination.labelRowsPerPage'),
      firstAriaLabel: t('table.pagination.firstPage'),
      firstTooltip: t('table.pagination.firstPage'),
      previousAriaLabel: t('table.pagination.previousPage'),
      previousTooltip: t('table.pagination.previousPage'),
      nextAriaLabel: t('table.pagination.nextPage'),
      nextTooltip: t('table.pagination.nextPage'),
      lastAriaLabel: t('table.pagination.lastPage'),
      lastTooltip: t('table.pagination.lastPage')
    },
    toolbar: {
      searchTooltip: t('table.toolbar.search'),
      searchPlaceholder: t('table.toolbar.search'),
      showColumnsTitle: t('table.toolbar.showColumnsTitle'),
      addRemoveColumns: t('table.toolbar.addRemoveColumns')
    }
  }
}
