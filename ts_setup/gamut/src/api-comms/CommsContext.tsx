import React from 'react'

import { CommsApi } from './comms-types';
import { usePopulateContext } from './usePopulateContext';




export const CommsContext = React.createContext<CommsApi.CommsContextType>({} as any);


export const CommsProvider: React.FC<{
  children: React.ReactNode;
  options: { staleTime: number, queryKey: string };
  getSubjects: CommsApi.GetSubjectsFetchGET;
  replyTo: CommsApi.ReplyToFetchPOST;
}> = (props) => {
  const data = usePopulateContext(props);

  return React.useMemo(() => {
    const exchanges = data.subjects.filter((c) => c.exchange.length);

    const contextValue: CommsApi.CommsContextType = {
      subjects: data.subjects,
      isPending: data.isPending,
      subjectStats: Object.freeze({ exchanges: exchanges.length }),
      getSubject: (id) => data.subjects.find((subject) => subject.id === id),
      replyTo: data.replyTo, 
      refresh: data.refresh,
    };

    return (<CommsContext.Provider value={contextValue}>{props.children}</CommsContext.Provider>);
  }, [data, props]);
}


export function useComms(): CommsApi.CommsContextType {
  const result: CommsApi.CommsContextType = React.useContext(CommsContext);
  return result;
}

