import React from 'react';
import { useQuery } from '@tanstack/react-query'
import { LegacyProcessApi } from '../api-legacy-processes';
import { mapToSubjectData } from './mappers'
import { CommsApi } from './comms-types';
import { useIam } from '../api-iam';



export interface UsePropulateProps {
  children: React.ReactNode;
  options: { staleTime: number, queryKey: string };
  getSubjects: CommsApi.GetSubjectsFetchGET;
  replyTo: CommsApi.ReplyToFetchPOST;
}

export interface PopulateCommsContext {
  subjects: readonly CommsApi.Subject[];
  isPending: boolean;
  refresh(): Promise<void>;
  replyTo(comment: CommsApi.ReplyTo): Promise<void>
}

export function usePopulateContext(props: UsePropulateProps): PopulateCommsContext {
  const { user } = useIam();


  const [isInitialLoadDone, setInitialLoadDone] = React.useState(false);
  const { getSubjects, replyTo: replyToFetch, options } = props;
  const { staleTime, queryKey } = options;

  // tanstack query config
  const { data: processes, error, refetch, isPending } = useQuery({
    staleTime,
    queryKey: [queryKey],
    queryFn: () => getSubjects()
      .then(data => data.json())
      .then((data: LegacyProcessApi.Process[]) => data),
  });

  const subjectData = mapToSubjectData(processes ?? [], user);

  // Reload all data
  const refresh: () => Promise<void> = React.useCallback(async () => {
    return refetch().then(() => { });
  }, [refetch]);


  /* ORIGINAL
  const replyTo = (comment: CommsApi.ReplyTo) => React.useCallback(async () => {
    return replyToFetch(comment)
      .then((resp) => resp.json())
      .then((body) => {
        console.log(body);

        return refresh();
      });
  }, [replyToFetch]);

  */

  const replyTo: (comment: CommsApi.ReplyTo) => Promise<void> = React.useCallback(async (comment) => {
    return replyToFetch(comment)
      .then((resp) => resp.json())
      .then((body: CommsApi.Subject) => {
        console.log(body);
        refresh();
        return;
      })


  }, [replyToFetch, refresh]);

  // track initial loading
  React.useEffect(() => {
    if (isInitialLoadDone) {
      return;
    }
    if (subjectData) {
      setInitialLoadDone(true);
    }
  }, [isInitialLoadDone, subjectData]);


  const isContextLoaded = (isInitialLoadDone || !isPending);

  // cache the end result
  return React.useMemo(() => {
    return { subjects: subjectData?.subjects ?? [], isPending: !isContextLoaded, refresh, replyTo };
  }, [subjectData?.hash, isContextLoaded, refresh, replyTo]);
}
