import React from 'react'

import { CommsApi } from './comms-types';
import { usePopulateContext } from './usePopulateContext';




export const CommsContext = React.createContext<CommsApi.CommsContextType>({} as any);


export const CommsProvider: React.FC<{
  children: React.ReactNode;
  options: { staleTime: number, queryKey: string };
  getSubjects: CommsApi.GetSubjectsFetchGET;
  replyTo: CommsApi.ReplyToFetchPOST;
  markViewed: CommsApi.ViewSubjectFetchPUT;
}> = (props) => {
  const data = usePopulateContext(props);
  const [readAt, setReadAt] = React.useState<Record<string, number>>({});

  const subjectsWithOverride = React.useMemo(() => {
    return data.subjects.map((s) => {
      const lastTs = (s.lastExchange?.created ? s.lastExchange.created.toMillis() : s.created.toMillis());
      const viewedOverride = (readAt[s.id] ?? 0) >= lastTs;
      return viewedOverride ? { ...s, isViewed: true } : s;
    });
  }, [data.subjects, readAt]);

  const [sortOrder, setSortOrder] = React.useState<CommsApi.SubjectSortOrder>('DESC');
  const sortedByDate = subjectsWithOverride
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
    const exchanges = subjectsWithOverride.filter((c) => c.exchange.length).length;
    const unread = subjectsWithOverride.filter((c) => !c.isViewed).length;

    const contextValue: CommsApi.CommsContextType = {
      subjects: sortedByDate,
      isPending: data.isPending,
      subjectStats: Object.freeze({ exchanges, unread }),
      getSubject: (id) => subjectsWithOverride.find((subject) => subject.id === id),
      toggleSubjectSortOrder,
      sortOrder,
      replyTo: data.replyTo,
      refresh: data.refresh,
      markViewed: (subjectId) => {
        // optimistic: mark read up to the current latest message (or created)
        const s = data.subjects.find(x => x.id === subjectId);
        const ts = s
          ? (s.lastExchange?.created ? s.lastExchange.created.toMillis() : s.created.toMillis())
          : Date.now();
        setReadAt(prev => ({ ...prev, [subjectId]: ts }));
        return props.markViewed(subjectId).then(() => { /* no-op */ });
      }
    };

    return (<CommsContext.Provider value={contextValue}>{props.children}</CommsContext.Provider>);
  }, [subjectsWithOverride, sortedByDate, data.isPending, data.replyTo, data.refresh, toggleSubjectSortOrder, sortOrder, props]);
}

export function useComms(): CommsApi.CommsContextType {
  const result: CommsApi.CommsContextType = React.useContext(CommsContext);
  return result;
}

