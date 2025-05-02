import { DateTime } from 'luxon';

import { Md5 } from 'ts-md5';
import { OfferApi } from './offer-types';
import { LegacyProcessApi } from '../api-legacy-processes';
import { SITE_SEARCH_TOPIC, SiteApi } from '../api-site';



export function mapToOfferData(data: LegacyProcessApi.Process[], site: SiteApi.Site | undefined): {
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

    const offer = mapToOffer(proc, site);
    md5
      .appendStr(proc.id)
      .appendStr(proc.name)
      .appendStr(proc.formId)
      .appendStr(proc.formUri)
      .appendStr(proc.created)
      .appendStr(proc.updated ?? '');

    offers.push(offer);
  }

  return { offers: Object.freeze(offers), hash: md5.end() + '' };
}

export function mapToOffer(data: LegacyProcessApi.Process, site: SiteApi.Site | undefined): OfferApi.Offer {

  const optionalSiteData: { 
    parentPageId: string, 
    pageId: string, 
    productId: string,
    otherLocales: {
      locale: string;
      productId: string;
    }[]

   } = { pageId: "", parentPageId: "", productId: "", otherLocales: []}


  if(site) {
    try {
      const page = Object.values(site.topics).find(topic => topic.id.substring(4) === data.inputContextId);
      const parentPage = Object.values(site.topics).find(topic => topic.id.substring(4) === data.inputParentContextId);

      const productInCurrentLocale = page ? Object.values(site.links)
        .filter(link => link.type === 'workflow')
        .find(link => link.value === data.name) : null;

      const productInOtherLocale = toOtherTopicLinkLocales(site, data.name);

      optionalSiteData.pageId = page?.id ?? SITE_SEARCH_TOPIC;
      optionalSiteData.parentPageId = parentPage?.id ?? "";
      optionalSiteData.productId = productInCurrentLocale?.id ?? "";
      optionalSiteData.otherLocales = productInOtherLocale ?? [];
    } catch(error) {
      console.error("failed to match site data", error)
    }
  }


  return Object.freeze({
    created: DateTime.fromISO(data.created),
    updated: DateTime.fromISO(data.updated),
    formId: data.formId,
    formUri: data.formUri,

    id: data.id,
    name: data.name,
    ...optionalSiteData
  });
}


export function toOtherTopicLinkLocales(site: SiteApi.Site, workflowName: string) {
  const productInOtherLocale = Object.keys(site.workflowsInOtherLocales)
  .reduce<{
    locale: string;
    productId: string;
    product: SiteApi.TopicLink
  }[]>((collector, locale) => {
    const product = site.workflowsInOtherLocales[locale]
      .filter(link => link.type === 'workflow')
      .find(link => link.value === workflowName)

    if(product) {
      collector.push({
        locale, productId: product.id, product
      })
    }
    return collector;
  }, [])

  return productInOtherLocale;
}