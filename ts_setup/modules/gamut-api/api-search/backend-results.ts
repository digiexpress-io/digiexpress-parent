import React from 'react';

import { SiteApi } from '../api-site';
import { SearchApi } from './search-types';


const EMPTY: SearchApi.SemanticResults = { topics: [], forms: [] };

/** Map backend hits by workflow / topic id, not display name. */
function mapBackendResultsWithUnmapped(
  views: Record<SiteApi.TopicId, SiteApi.TopicView>,
  results: readonly SearchApi.BackendSearchResult[],
  filterMode: SearchApi.FilterMode
): { mapped: SearchApi.SemanticResults, unmapped: number } {

  const formsByWorkflowId = buildFormLookup(views);

  const forms: SearchApi.LinkToForm[] = [];
  const topics: SiteApi.TopicView[] = [];
  const seenForms = new Set<SiteApi.TopicLinkId>();
  const seenTopics = new Set<SiteApi.TopicId>();
  let unmapped = 0;

  for (const result of results) {
    const form = formsByWorkflowId.get(result.workflowId);
    if (!form) {
      unmapped++;
    } else if (!seenForms.has(form.linkToForm.id)) {
      seenForms.add(form.linkToForm.id);
      forms.push(form);
    }

    const topic = result.topicId ? views[result.topicId] : undefined;
    if (topic && !seenTopics.has(topic.id)) {
      seenTopics.add(topic.id);
      topics.push(topic);
    }
  }

  if (unmapped > 0) {
    console.debug(`search: ${unmapped} of ${results.length} results are not in the current site`);
  }

  const showTopics = filterMode === 'ALL' || filterMode === 'TOPICS';
  const showForms = filterMode === 'ALL' || filterMode === 'FORM_LINKS';
  return {
    unmapped,
    mapped: {
      topics: showTopics ? topics : [],
      forms: showForms ? forms : [],
    },
  };
}

/** Prefer the synthetic search topic as owner when the same workflow appears there. */
function buildFormLookup(
  views: Record<SiteApi.TopicId, SiteApi.TopicView>
): Map<SiteApi.TopicLinkId, SearchApi.LinkToForm> {

  const lookup = new Map<SiteApi.TopicLinkId, SearchApi.LinkToForm>();
  const all = Object.values(views);
  const ordered = [...all.filter(view => view.topic.searchOnly), ...all.filter(view => !view.topic.searchOnly)];

  for (const view of ordered) {
    for (const link of view.workflows) {
      if (lookup.has(link.id)) {
        continue;
      }
      lookup.set(link.id, { linkToForm: link, topic: view, label: link.name });
    }
  }
  return lookup;
}

export function useSemanticResults(
  views: Record<SiteApi.TopicId, SiteApi.TopicView>,
  state: SearchApi.SearchState | undefined,
  backend: SearchApi.BackendSearchState
): SearchApi.SemanticResults | undefined {

  const filterMode = state?.searchOptionType ?? 'ALL';
  const { results, status } = backend;

  return React.useMemo(() => {
    if (status === 'pending') {
      return EMPTY;
    }
    if (status === 'unavailable') {
      return undefined;
    }
    if (!results) {
      return undefined;
    }
    const { mapped, unmapped } = mapBackendResultsWithUnmapped(views, results, filterMode);
    if (results.length > 0 && unmapped === results.length) {
      return undefined;
    }
    return mapped;
  }, [views, results, filterMode, status]);
}
