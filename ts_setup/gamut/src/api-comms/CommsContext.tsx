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

  const [sortOrder, setSortOrder] = React.useState<CommsApi.SubjectSortOrder>('DESC');
  const sortedByDate = data.subjects
    .filter((c) => !!c.created)
    .sort((a, b) => {
      const dateA = a.lastExchange?.created ? a.lastExchange.created.toMillis() : a.created.toMillis();
      const dateB = b.lastExchange?.created ? b.lastExchange.created.toMillis() : b.created.toMillis();
      return sortOrder === 'ASC' ? dateA - dateB : dateB - dateA;
    });

  function toggleSubjectSortOrder() {
    setSortOrder((prev) => (prev === 'ASC' ? 'DESC' : 'ASC'));
  };


  return React.useMemo(() => {
    const exchanges = data.subjects.filter((c) => c.exchange.length);

    const contextValue: CommsApi.CommsContextType = {
      subjects: sortedByDate,
      isPending: data.isPending,
      subjectStats: Object.freeze({ exchanges: exchanges.length }),
      getSubject: (id) => data.subjects.find((subject) => subject.id === id),
      toggleSubjectSortOrder,
      sortOrder,
      replyTo: data.replyTo, 
      refresh: data.refresh,
    };

    return (<CommsContext.Provider value={contextValue}>{props.children}</CommsContext.Provider>);
  }, [data, props, sortOrder]);
}


export function useComms(): CommsApi.CommsContextType {
  const result: CommsApi.CommsContextType = React.useContext(CommsContext);
  return result;
}

