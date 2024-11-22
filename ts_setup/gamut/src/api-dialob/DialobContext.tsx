import React from 'react';
import { useQuery } from '@tanstack/react-query'
import { DialobApi } from './dialob-types';


// Root props for Provider
export interface DialobProps {
  fetchActionPost: DialobApi.FetchActionPOST;
  fetchActionGet: DialobApi.FetchActionGET;
  fetchReviewGet: DialobApi.FetchReviewGET;

  syncWait?: number | undefined;
  children: React.ReactNode;
}
export const DialobContext = React.createContext<DialobApi.DialobContextType>({} as any);

export const DialobProvider: React.FC<DialobProps> = (props) => {
  const { fetchActionPost, fetchActionGet, fetchReviewGet, syncWait } = props;

  const contextValue: DialobApi.DialobContextType = React.useMemo(() => {
    return Object.freeze({ fetchActionPost, fetchActionGet, fetchReviewGet, syncWait })
  }, [fetchActionPost, fetchActionGet, fetchReviewGet, syncWait]);

  return (<DialobContext.Provider value={contextValue}>{props.children}</DialobContext.Provider >);
}

export const useDialob = () => {
  const result: DialobApi.DialobContextType = React.useContext(DialobContext);
  return result;
}

export function useDialobReview(props: { id: string }): {

  isPending: boolean,
  review: { form: any, session: any } | undefined
} {
  const { id } = props;
  const { fetchReviewGet } = useDialob();

  const { data: review, error, refetch, isPending } = useQuery({
    staleTime: 5000,
    queryKey: ['reviews/' + id],
    queryFn: () => fetchReviewGet(props.id)
      .then(data => data.json())
      .then((data: { form: any, session: any }) => {
        return data;
      }),
  });

  return { isPending, review }
}
