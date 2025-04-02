import { SiteApi } from "./site-types";
import { IntlShape } from "react-intl";



export function getSearchTopics(site: SiteApi.Site, intl: IntlShape): { 
  topics: SiteApi.Topic[]
} {
  const { locale, links } = site;
  const title = intl.formatMessage({id: 'gamut.search.popover.title'});

  const id = '__search_topic';

  const forms = Object.values(links)
    .filter(link => link.workflow)
    .reduce<Record<string, SiteApi.TopicLink>>((collector, form) => {
      if(collector[form.name]) {
        return collector;
      }
      collector[form.name] = form;
      return collector;
    }, {});

  return { 
    topics: [
      {
        id: id,
        name: title,
        searchOnly: true,
        headings: [{
          id: id,
          level: 1,
          order: 1,
          name: title
        }],
        links: Object.values(forms).map(e => e.id),
        blob: undefined,
        parent: undefined
      }
    ]
  };
}