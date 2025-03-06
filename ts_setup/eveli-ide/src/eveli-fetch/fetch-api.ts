


export type HookProps<THook extends Function> = THook extends (args: infer A) => any ? A : never
export type Hook<TProps, TReturnType> = (props: TProps) => TReturnType
export type ProxyHook<TProps, TReturnType> = (props: TProps) => TReturnType
export type HttpMethod = 'GET' | 'POST' | 'DELETE' | 'PUT';
