import React from 'react';
import { DialobApi } from './dialob-types';


// Root props for Provider
export interface DialobProps {
  fetchPost: DialobApi.FetchPOST;
  fetchGet: DialobApi.FetchGET;
  syncWait?: number | undefined;
  children: React.ReactNode;
}
export const DialobContext = React.createContext<DialobApi.DialobContextType>({} as any);

export const DialobProvider: React.FC<DialobProps> = (props) => {
  const { fetchPost, fetchGet, syncWait } = props;

  const contextValue: DialobApi.DialobContextType = React.useMemo(() => {
    return Object.freeze({ fetchPost, fetchGet, syncWait })
  }, [fetchPost, fetchGet, syncWait]);

  return (<DialobContext.Provider value={contextValue}>{props.children}</DialobContext.Provider >);
}



export const useDialob = () => {
  const result: DialobApi.DialobContextType = React.useContext(DialobContext);
  return result;
}

