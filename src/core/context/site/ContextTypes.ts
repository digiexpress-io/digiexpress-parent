import * as Api from '../../service';
import { SiteState, ContextAction } from './contextReducer';

interface SiteContextType {
  service: Api.Service;
  site?: Api.Site;
  locale: string;
  topic?: Api.Topic;
  link?: Api.TopicLink;

  getBlob: (topic?: Api.Topic) => Api.Blob | undefined;
  setSite: (site?: Api.Site) => void;
  setLink: (newLink?: Api.TopicLink) => void;
  setLocale: (newLocale: string) => void;
  setTopic: (newTopic: Api.Topic) => void;
}

interface SiteConfigEvents {
  setTopic?: (newTopic: Api.Topic) => void;
  setLink?: (newLink?: Api.TopicLink) => void;
  setSite?: (newSite: Api.Site) => void;
}

const initContext = (
  state: SiteState,
  service: Api.Service,
  dispatch: React.Dispatch<ContextAction>,
  events?: SiteConfigEvents
  ): SiteContextType => {

  return {
    service: service,
    locale: state.locale,
    site: state.site,
    topic: state.topic,

    getBlob: (topic?: Api.Topic) => {
      if (!state.site) {
        return undefined;
      }
      if(!topic && !state.topic) {
        return undefined;
      }
      
      let targetTopic = topic ? topic: state.topic;
      if(!targetTopic?.blob) {
        return undefined
      }
      return state.site.blobs[targetTopic.blob];
    },
    setSite: (site?: Api.Site) => {
      dispatch({ type: "setSite", site })
      if(site && events && events.setSite) {
        events.setSite(site);  
      }
      
    },

    setTopic: (newTopic: Api.Topic) => {
      dispatch({ type: "setTopic", topic: newTopic })
      if(events && events.setTopic) {
        events.setTopic(newTopic);  
      }
      
    },

    setLocale: (newLocale: string) => {
      if (state.locale === newLocale) {
        return;
      }
      service.getSite(newLocale).then(site => {
        dispatch({ type: "setSite", site: site })
        dispatch({ type: "setLocale", locale: newLocale })

        if (state.topic) {
          dispatch({ type: "setTopic", topic: site.topics[state.topic.id] })
        }

      });

    },
    setLink: (newLink?: Api.TopicLink) => {
      if(events && events.setLink) {
        events.setLink(newLink);  
      }
      if (newLink && !(newLink.type === "dialob" || newLink.type === "workflow")) {
        return;
      }
      dispatch({ type: "setLink", link: newLink })
    },
  };
}

export type { SiteContextType, SiteConfigEvents };
export { initContext };
