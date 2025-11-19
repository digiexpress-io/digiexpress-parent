import React from "react";

import { DialobApi } from "@dxs-ts/gamut-api";
import { useFetch } from "@dxs-ts/envir-fetch";


export function useDialobReviewNav() {
  const { fetchReviewActionsPost, fetchReviewActionsGet } = useFetch('worker/rest/api/tasks/$taskId/review-actions.GET', {});
  const [, setActions] = React.useState<DialobApi.Action[]>([]);

  
  const updateActions = React.useCallback(async function(actions: DialobApi.Action[]): Promise<DialobApi.Action[]> {
    const next = await new Promise<DialobApi.Action[]>(resolve => {
      setActions(prev => {
        const allActions = _flattenNavigationActions([...prev, ...actions]);
        resolve(allActions);
        return allActions;
      });
    });
    return next;
  }, []);

  const fetchActionPost = React.useCallback(async function(sessionId: string, next: DialobApi.Action[], rev: number): Promise<Response> {
    const updated = await updateActions(next);
    return fetchReviewActionsPost(sessionId, updated, rev);
  }, []);

  const fetchActionGet: DialobApi.FetchActionGET = React.useCallback(async (sessionId: string) => {
    return fetchReviewActionsGet(sessionId)
      .then((data) => {
        return data;
      });
  }, [])

  return { fetchActionPost, fetchActionGet };
}

function _flattenNavigationActions(actions: DialobApi.Action[]): DialobApi.Action[] {
  const result: DialobApi.Action[] = [];

  for (const action of actions) {
    const lastAction = result[result.length - 1];

    // If current action cancels out the last one, remove the last one
    if (lastAction &&
      ((action.type === 'NEXT' && lastAction.type === 'PREVIOUS') ||
        (action.type === 'PREVIOUS' && lastAction.type === 'NEXT'))) {
      result.pop();
    } else {
      result.push(action);
    }
  }

  return result;
}