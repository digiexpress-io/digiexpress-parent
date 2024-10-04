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
}

export interface PopulateCommsContext {
  subjects: readonly CommsApi.Subject[];
  isPending: boolean;
  refresh(): Promise<void>;
}

export function usePopulateContext(props: UsePropulateProps): PopulateCommsContext {
  const { user } = useIam();


  const [isInitialLoadDone, setInitialLoadDone] = React.useState(false);
  const { getSubjects, options } = props;
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
    return { subjects: subjectData?.subjects ?? [], isPending: !isContextLoaded, refresh };
  }, [subjectData?.hash, isContextLoaded, refresh]);
}
