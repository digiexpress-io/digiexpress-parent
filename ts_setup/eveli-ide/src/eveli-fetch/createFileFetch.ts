import { HookByPath, Hook, HookProps, ProxyHook, useRouteParams, useNativeRouteParams } from '@dxs-ts/eveli-fetch';



export function createRootFileFetch(contextPath: string, fetch: () => typeof window.fetch) {
  return new RootFileFetch(contextPath, {} as any, fetch);
} 

export class RootFileFetch {
  constructor(public contextPath: string, public children: HookByPath, public fetch: () => typeof window.fetch) {
  }
  update(children: HookByPath) {
    return new RootFileFetch(this.contextPath, children, this.fetch);
  }

  get(key: keyof HookByPath) {
    return this.children[key];
  }
}


export function createFileFetch<
  TFetchPath extends keyof HookByPath
>(path: TFetchPath) {  

  return new FetchBuilder(path).createHook;
}

export class HookImpl<
  TFetchId extends keyof HookByPath,
  THook extends Hook<THookProps, THookReturnType>, 
  TPath extends HookByPath[TFetchId]['path'],
  TMethod extends HookByPath[TFetchId]['method'],
  TParams extends HookByPath[TFetchId]['params'],

  THookProps = HookProps<THook>, 
  THookReturnType = ReturnType<THook>
> {
  private _id: TFetchId;
  private _original: ProxyHook<THookProps, THookReturnType>;
  private _options: {
    path: TPath,
    method: TMethod,
  } = {} as any;
  
  constructor(id: TFetchId, original: ProxyHook<THookProps, THookReturnType>) {
    this._id = id;
    this._original = original;
    this._options = {} as any;
  }
  get options() {
    return this._options;
  }
  get id(): TFetchId {
    return this._id;
  }
  get path(): TPath {
    return this._options.path;
  }
  get method(): TMethod {
    return this._options.method;
  }
  get hook() {
    return this
  }
  get params(): any {
    return {} as any;
  }
  proxy = (proxyOptions: THookProps): THookReturnType => {
    return this._original(proxyOptions);
  }
  useParams = () => {
    return useRouteParams<TFetchId, TPath, TMethod, TParams>(this._options.path, this._options.method);
  }
  useNativeParams = () => {
    return useNativeRouteParams<TFetchId, TPath, TMethod, TParams>(this._options.path, this._options.method);
  }
  update = (options: {
    path: HookByPath[TFetchId]['path'],
    method: HookByPath[TFetchId]['method'],
  }) => {
    this._options = options as any;
    return this as (
      HookImpl<
        TFetchId, 
        Hook<THookProps, THookReturnType>, 
        TPath,
        TMethod,
        TParams
      >
    );
  }
}

export class FetchBuilder<TFetchId extends keyof HookByPath> {
  private _id: TFetchId
  constructor(id: TFetchId) {
    this._id = id;
  }

  createHook = <
    THook extends Hook<THookProps, THookReturnType>, 
    THookProps = HookProps<THook>, 
    THookReturnType = ReturnType<THook>
>
  (options: { hook: ProxyHook<THookProps, THookReturnType> }): HookImpl<
    TFetchId, 
    Hook<THookProps, THookReturnType>,
    HookByPath[TFetchId]['path'],
    HookByPath[TFetchId]['method'],
    HookByPath[TFetchId]['params']
  > => {

    const original: ProxyHook<THookProps, THookReturnType> = options.hook;
    return new HookImpl<
      TFetchId, 
      Hook<THookProps, THookReturnType>, 
      HookByPath[TFetchId]['path'],
      HookByPath[TFetchId]['method'],
      HookByPath[TFetchId]['params']
    >(this._id, original);
  }
}