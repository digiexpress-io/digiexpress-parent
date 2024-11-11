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

  for (const proc of data) {
    
    if (!proc.taskId) {
      continue;
    }
    const contract = mapToContract(proc, site);
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

function mapToContract(data: LegacyProcessApi.Process, site: SiteApi.Site | undefined): ContractApi.Contract {
  const docs: readonly ContractApi.ContractDocument[] = Object.freeze(data.attachments.map(({ id, name, size, created }) => Object.freeze({
    id,
    name,
    size,
    created: DateTime.fromISO(created)
  })));

  return Object.freeze({
    id: data.id,
    exchangeId: data.taskId!,
    status: data.taskStatus! as any, 
    reviewUri: data.reviewUri!,
    documents: docs,
    product: {} as any,
    offer: OfferApi.mapper(data, site),

    booking: undefined,
    created: DateTime.fromISO(data.taskCreated!),
    updated: data.taskUpdated ? DateTime.fromISO(data.taskUpdated) : undefined
  });
}