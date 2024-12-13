import React from "react";
import { SiteBackendContext, SiteBackendContextType } from "./SiteBackendProvider";
import { SiteApi } from "./site-types";


export function useSite() {
  const result: SiteBackendContextType = React.useContext(SiteBackendContext);
  const { site, views, feedback, voteOnReply } = result;
  const topics = React.useMemo(() => getSortedTopics(views), [views]);


  const getLink = (linkId: string): SiteApi.TopicLink => {
    const site = result.site;
    if (!site) {
      throw new Error("Site not loaded")
    }
    return site.links[linkId];
  }

  function getTopicGroups(itemsInColumn: number | undefined = 8): SiteApi.TopicGroup[] {
    return createTopicGroups(topics, itemsInColumn);
  }

  return { getTopicGroups, getLink, site, views, feedback, voteOnReply };
}


function createTopicGroups(topics: SiteApi.TopicView[], itemsInColumn: number): SiteApi.TopicGroup[] {
  const totalColumns = Array(Math.ceil(topics.length / itemsInColumn));
  const slices: (SiteApi.TopicView[])[] = Array.apply({}, totalColumns).map((_empty, column) => {
    const start = column * itemsInColumn;
    const end = Math.min(start + itemsInColumn, topics.length);
    return topics.slice(start, end);
  });

  return slices.map((topics, column) => ({ column, topics, next: column < totalColumns.length - 1 }));
}
function getSortedTopics(views: Record<string, SiteApi.TopicView>) {
  return Object.values(views ?? []).sort((a, b) => a.topic.id.localeCompare(b.topic.id));
}