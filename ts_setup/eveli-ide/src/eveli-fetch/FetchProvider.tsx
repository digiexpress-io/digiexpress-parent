import React from 'react';


import { HookProps, HookByPath } from './fetch-api';
import { RootFileFetch } from './createFileFetch';



export interface FetchContextType {
  contextPath: string;
  state: RootFileFetch
}

export const FetchContext = React.createContext<FetchContextType>({} as any)

export const FetchProvider: React.FC<{ children: React.ReactNode, tree: RootFileFetch, contextPath: string }> = ({children, tree, contextPath}) => {
  const [state, setState] = React.useState<RootFileFetch>(tree);
  
  const context = React.useMemo(() => {
    return { state, contextPath }
  }, [setState, state, contextPath])

  return (<FetchContext.Provider value={context}>{children}</FetchContext.Provider>);
}

export class FetchParams<
  TFetchPath extends keyof HookByPath,
  TPath extends HookByPath[TFetchPath]['path'],
  TMethod extends HookByPath[TFetchPath]['method'],
  TParams extends HookByPath[TFetchPath]['params'],
> {
  constructor(
    public contextPath: string, 
    public path: TPath,
    public method: TMethod,
    public fetch: typeof window.fetch
  ) {
  }

  url = (type: TParams): string => {
    const replaceParamsIn: string = this.path;
    const pathWithParams = Object.entries(type as Record<string, string>)
      .reduce((current, [key, value]) => current.replace(`$${key}`, value), replaceParamsIn);

    return `${this.contextPath}${pathWithParams}`;
  }
}

export function useRouteParams<
  TFetchPath extends keyof HookByPath,
  TPath extends HookByPath[TFetchPath]['path'],
  TMethod extends HookByPath[TFetchPath]['method'],
  TParams extends HookByPath[TFetchPath]['params'],
>(path: TPath, method: any) {


  const context: FetchContextType = React.useContext(FetchContext);
  const delegateFetch = context.state.fetch();

  return new FetchParams<TFetchPath, TPath, TMethod, TParams>(context.contextPath, path, method, delegateFetch);
}

// WARNING!!! this will not use overriden fetch function instead it will use window.fetch.
export function useNativeRouteParams<
  TFetchPath extends keyof HookByPath,
  TPath extends HookByPath[TFetchPath]['path'],
  TMethod extends HookByPath[TFetchPath]['method'],
  TParams extends HookByPath[TFetchPath]['params'],
>(path: TPath, method: any): Omit<FetchParams<TFetchPath, TPath, TMethod, TParams>, 'fetch'> {

  const context: FetchContextType = React.useContext(FetchContext);
  const delegateFetch = window.fetch;
  return new FetchParams<TFetchPath, TPath, TMethod, TParams>(context.contextPath, path, method, delegateFetch);
}

export function useFetch<TPathId extends keyof HookByPath>(
  id: TPathId,
  props: HookProps<HookByPath[TPathId]['hook']['proxy']>
): ReturnType<HookByPath[TPathId]['hook']['proxy']>  {

  const context: FetchContextType = React.useContext(FetchContext);
  const entry = context.state.get(id);
  return entry.hook.proxy(props) as any;
}