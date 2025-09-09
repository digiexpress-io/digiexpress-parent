
import { TableState, TableStateInitWith } from "@dxs-ts/xui-table";
import React from "react";
import { useTenantConfigFeatures } from '../api-tenant-config';
import { UserProfileApi } from "@dxs-ts/user-profile";

const dataId = 'last-table-state';

export function useLastTableState(tableId: string) {
  const { userId, backend } = UserProfileApi.useUserProfile();
  const [, setLastSync] = React.useState<string>();
  const { isEnabled } = useTenantConfigFeatures()
  const isSmartTables = isEnabled('SMART_TABLES');
  const settingsId =  `${tableId}-${dataId}`;


  const onNext: (next: TableState) => Promise<void> = React.useCallback(async (next) => {
    setLastSync(prev => {
      if(!prev || next?.hash === prev) { 
        return next.hash;
      }
      if(isSmartTables) {
        backend.updateUiSettings({
          commandType: 'UpsertUiSettings',
          settingsId,
          userId,
          visibility: [],

          // always one value... last state of the table rules
          config: [{
            dataId: dataId,
            label: undefined,
            value: JSON.stringify(next.copy())
          }]
        })
      }
      return next.hash;
    })
  }, [tableId]);

  const onRestore: () => Promise<TableStateInitWith | undefined> = React.useCallback(async () => {
    if(!isSmartTables) {
      return undefined;
    }
    try {
      const data = await backend.findUiSettings(settingsId);
      if(!data?.config) {
        return undefined;
      }

      const config = data.config.find(config => config.dataId === dataId);
      if(config) {
        return JSON.parse(config.value);
      }
    } catch(e) {
      console.error('Failed to parse table settings', e)
    }
    return undefined;
  }, [tableId]);

  return { onNext, onRestore };
}