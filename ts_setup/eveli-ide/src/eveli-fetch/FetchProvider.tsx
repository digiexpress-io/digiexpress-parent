import React from 'react';


import { HookProps, HookByPath } from './fetch-api';
import { RootFileFetch } from './createFileFetch';



export type FetchOverrides = Partial<Record<keyof HookByPath, (parentFetch: typeof window.fetch) => typeof window.fetch>>;


export interface FetchContextType {
  contextPath: string;
  state: RootFileFetch;
  overrides: FetchOverrides | undefined;
  setContextPath: (newContextPath: string) => void;
}

export const FetchContext = React.createContext<FetchContextType>({} as any)

export const FetchProvider: React.FC<{ 
  children: React.ReactNode, tree: RootFileFetch, initContextPath: string,
  overrides?: FetchOverrides
}> = ({children, tree, initContextPath, overrides}) => {
  const [state, setState] = React.useState<RootFileFetch>(tree);
  const [contextPath, setContextPath] = React.useState<string>(initContextPath);
  
  const context = React.useMemo(() => {
    return { state, contextPath, setContextPath, overrides }
  }, [setContextPath, state, contextPath, overrides])

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
>(path: TPath, method: any, id: TFetchPath) {


  const context: FetchContextType = React.useContext(FetchContext);
  const override = context.overrides && context.overrides[id];
  if(override) {
    console.log(`fetch ${id} has override`); 
  }
  const delegateFetch = context.state.fetch();
  const finalFetch = override ? override(delegateFetch) : delegateFetch;

  return new FetchParams<TFetchPath, TPath, TMethod, TParams>(context.contextPath, path, method, finalFetch);
}

// WARNING!!! this will not use overriden fetch function instead it will use window.fetch.
export function useNativeRouteParams<
  TFetchPath extends keyof HookByPath,
  TPath extends HookByPath[TFetchPath]['path'],
  TMethod extends HookByPath[TFetchPath]['method'],
  TParams extends HookByPath[TFetchPath]['params'],
>(path: TPath, method: any, id: TFetchPath): Omit<FetchParams<TFetchPath, TPath, TMethod, TParams>, 'fetch'> {

  const context: FetchContextType = React.useContext(FetchContext);
  const delegateFetch = window.fetch;
  return new FetchParams<TFetchPath, TPath, TMethod, TParams>(context.contextPath, path, method, delegateFetch);
}

export function useFetchConfig() {
  const context: FetchContextType = React.useContext(FetchContext);
  return context;
}

export function useFetch<TPathId extends keyof HookByPath>(
  id: TPathId,
  props: HookProps<HookByPath[TPathId]['hook']['proxy']>
): ReturnType<HookByPath[TPathId]['hook']['proxy']>  {

  const context: FetchContextType = React.useContext(FetchContext);
  const entry = context.state.get(id);
  return entry.hook.proxy(props) as any;
}