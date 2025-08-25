
import { TableState, TableStateInitWith } from "./table-state-types";
import React from "react";
import { useFetch } from '@dxs-ts/envir-fetch';
import { useIam } from "@dxs-ts/eveli-api";
import { useTenantConfigFeatures } from "@dxs-ts/eveli-api";

const dataId = 'last-table-state';

export function useLastTableState(tableId: string) {
  const { user } = useIam();
  const [, setLastSync] = React.useState<string>();
  const { restApi } = useFetch('worker/rest/api/userprofiles/$profileId.GET', {});
  const { isEnabled } = useTenantConfigFeatures()
  const isSmartTables = isEnabled('SMART_TABLES');
  const settingsId =  `${tableId}-${dataId}`;


  const onNext: (next: TableState) => Promise<void> = React.useCallback(async (next) => {
    setLastSync(prev => {
      if(!prev || next?.hash === prev) { 
        return next.hash;
      }
      if(isSmartTables) {
        restApi.updateUiSettings({
          commandType: 'UpsertUiSettings',
          settingsId,
          userId: user.name,
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
      const data = await restApi.findUiSettings(settingsId);
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