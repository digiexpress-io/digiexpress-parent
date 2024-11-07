import { DateTime } from 'luxon';

import { Md5 } from 'ts-md5';
import { OfferApi } from './offer-types';
import { LegacyProcessApi } from '../api-legacy-processes';



export function mapToOfferData(data: LegacyProcessApi.Process[]): {
  hash: string;
  offers: readonly OfferApi.Offer[];
} {
  const md5 = new Md5();
  const offers: OfferApi.Offer[] = [];

  for (const proc of data) {
    if (!(proc.status === 'CREATED' 
      //|| proc.status === 'ANSWERED'
    )) {
      continue;
    }

    const offer = mapToOffer(proc);
    md5
      .appendStr(proc.id)
      .appendStr(proc.name)
      .appendStr(proc.formId)
      .appendStr(proc.formUri)
      .appendStr(proc.created)
      .appendStr(proc.updated ?? '')
      .appendStr(offer.productGroupId);

    offers.push(offer);
  }

  return { offers: Object.freeze(offers), hash: md5.end() + '' };
}

export function mapToOffer(data: LegacyProcessApi.Process): OfferApi.Offer {
  const productGroupId = (
    (data.inputParentContextId ? '' : data.inputParentContextId + '/') +
    data.inputContextId
  );

  return Object.freeze({
    created: DateTime.fromISO(data.created),
    updated: DateTime.fromISO(data.updated),
    formId: data.formId,
    formUri: data.formUri,

    id: data.id,
    name: data.name,
    productGroupId
  });
}