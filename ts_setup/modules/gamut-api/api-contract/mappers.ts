import { DateTime } from 'luxon';

import { Md5 } from 'ts-md5';
import { ContractApi } from './contract-types';
import { LegacyProcessApi } from '../api-legacy-processes';
import { OfferApi } from '../api-offer';
import { SiteApi } from '../api-site';



export function mapToContractData(data: LegacyProcessApi.Process[], site: SiteApi.Site | undefined): {
  hash: string;
  contracts: readonly ContractApi.Contract[];
} {
  const md5 = new Md5();
  const contracts: ContractApi.Contract[] = [];
  const groupedByTaskId = groupBy(data.filter(d => d.assigned), 'taskId');

  for (const proc of data) {
    if (!proc.taskId || proc.assigned) {
      continue;
    }
    const contract = mapToContract(proc, groupedByTaskId[proc.taskId], site);
    md5
      .appendStr(proc.id)
      .appendStr(proc.taskStatus ?? '')
      .appendStr(proc.reviewUri ?? '')
      .appendStr(proc.taskCreated ?? '')
      .appendStr(proc.taskUpdated ?? '');

    if (contract.booking) {
      md5
        .appendStr(contract.booking.scheduledAt.toISO() ?? '')
    }

    for (const doc of contract.documents) {
      md5
        .appendStr(doc.created.toISO() ?? '')
        .appendStr(doc.id)
        .appendStr(doc.name)
        .appendStr(doc.size + '')
    }

    contracts.push(contract);
  }

  return { contracts: Object.freeze(contracts), hash: md5.end() + '' };
}

function mapToContract(data: LegacyProcessApi.Process, grouped: LegacyProcessApi.Process[] | undefined, site: SiteApi.Site | undefined): ContractApi.Contract {
  const docs: readonly ContractApi.ContractDocument[] = Object.freeze(data.attachments.map(({ id, name, size, created }) => Object.freeze({
    id,
    name,
    size,
    created: DateTime.fromISO(created)
  })));

  return Object.freeze({
    id: data.taskId!,
    referenceId: data.taskRef!,
    exchangeId: data.id,
    status: data.taskStatus! as any, 
    reviewUri: data.reviewUri!,
    documents: docs,
    product: {} as any,
    offer: OfferApi.mapper(data, site),
    assigned: data.assigned,
    subforms: data.subActions,
    booking: undefined,
    created: DateTime.fromISO(data.taskCreated!),
    updated: data.taskUpdated ? DateTime.fromISO(data.taskUpdated) : undefined
  });
}


function groupBy<T, K extends keyof T>(array: T[], key: K): Record<string, T[]> {
  return array.reduce((groups, item) => {
    const groupKey = String(item[key]);
    if (!groups[groupKey]) {
      groups[groupKey] = [];
    }
    groups[groupKey].push(item);
    return groups;
  }, {} as Record<string, T[]>);
}
