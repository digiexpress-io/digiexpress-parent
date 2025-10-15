
import React from "react";
import { TableState, TableStateInitWith } from "../table-api";
import { useTable } from "../table-provider";
const dataId = 'last-table-state';



export function useLastTableState(tableId: string) {

  const [, setLastSync] = React.useState<string>();
  const { find, save, persistent: isSmartTables } = useTable();
  const settingsId = `${tableId}-${dataId}`;


  const onNext: (next: TableState) => Promise<void> = React.useCallback(async (next) => {
    setLastSync(prev => {
      if (!prev || next?.hash === prev) {
        return next.hash;
      }
      if (isSmartTables) {
        save(settingsId, [{
          dataId: dataId,
          label: undefined,
          value: JSON.stringify(next.copy())
        }])
      }
      return next.hash;
    })
  }, [tableId]);

  const onRestore: () => Promise<TableStateInitWith | undefined> = React.useCallback(async () => {
    if (!isSmartTables) {
      return undefined;
    }
    try {
      const data = await find(settingsId);
      if (!data?.config) {
        return undefined;
      }

      const config = data.config.find(config => config.dataId === dataId);
      if (config) {
        return JSON.parse(config.value);
      }
    } catch (e) {
      console.error('Failed to parse table settings', e)
    }
    return undefined;
  }, [tableId]);

  return { onNext, onRestore };
}