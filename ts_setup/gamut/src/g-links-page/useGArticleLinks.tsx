import { SiteApi } from '../api-site';


function filterAndSortLinks(links: SiteApi.TopicLink[], filterType: 'form' | 'phone' | 'info' | 'hyperlink') {

  const result = links.filter(link => {
    switch (filterType) {
      case 'form':
        return link.type === 'dialob' || link.type === 'workflow';
      case 'hyperlink':
        return link.type === 'internal' || link.type === 'external';
      case 'phone':
        return link.type === 'phone';
      case 'info':
        return link.value.includes('<info>');
      default:
        return false;
    }
  })
    .sort((a, b) => a.name.localeCompare(b.name));
  return result;
}

export function useGArticleLinks(topic: SiteApi.TopicView | undefined) {

  if (!topic) {
    return {
      formLinks: [],
      hyperlinks: [],
      phoneLinks: [],
      infoLinks: []
    }
  };

  const { links } = topic;

  const formLinks = filterAndSortLinks(links, 'form');
  const hyperlinks = filterAndSortLinks(links, 'hyperlink');
  const phoneLinks = filterAndSortLinks(links, 'phone');
  const infoLinks = filterAndSortLinks(links, 'info');

  return { formLinks, hyperlinks, phoneLinks, infoLinks }
}