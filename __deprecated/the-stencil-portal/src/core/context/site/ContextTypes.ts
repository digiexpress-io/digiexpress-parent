import * as Api from '../../service';
import { SiteState } from './contextReducer';

interface SiteContextType extends SiteActions {
  service: Api.Service;
  site?: Api.Site;
  views: Record<Api.TopicId, Api.TopicView>
  locale: string;
  getBlob: (topic: Api.Topic) => Api.Blob | undefined;
  actions: SiteActions;
}

interface SiteActions {
  setSite: (site?: Api.Site, newLocale?: Api.LocaleCode) => void;
  setLocale: (newLocale: string) => void;
}


interface SiteActionOverrides {
  setSite?: (newSite?: Api.Site) => Api.Site | undefined;
}

const initContext = (
  state: SiteState,
  service: Api.Service,
  actions: SiteActions,
): SiteContextType => {

  return {
    service: service,
    locale: state.locale,
    site: state.site,
    views: state.views,
    getBlob: (topic) => state.getBlob(topic),
    actions: actions,
    setSite: (site?: Api.Site, newLocale?: Api.LocaleCode) => actions.setSite(site, newLocale),
    setLocale: (newLocale: string) => {
      if (state.locale === newLocale) {
        return;
      }
      service.getSite(newLocale).then(apiSite => actions.setSite(apiSite, newLocale));
    },
  };
}


export type { SiteContextType, SiteActionOverrides, SiteActions };
export { initContext };
