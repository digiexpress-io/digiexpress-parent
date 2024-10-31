import { useEffect, useReducer, useRef, useState, useContext } from 'react';
import { cFetch, CFetchOptions } from '../util/cFetch';
import { SessionRefreshContext } from '../context/SessionRefreshContext';

interface HookOptions {
  poll?: number;
  noRetry?: boolean;
}

export function useFetch<R>(url: string, options?: CFetchOptions & HookOptions): {
  response?: R,
  error?: Error,
  refresh: () => void,
  code?: number
} {
  const [response, setResponse] = useState<R>();
  const [error, setError] = useState<Error>();
  const [code, setCode] = useState<number>();
  const [isLoading, setIsLoading] = useState(true); 

  const session = useContext(SessionRefreshContext);
  // lazy way to enable refreshing.
  // TODO: optimize this or find a more robust library that has `useFetch` in it
  const [refreshCounter, refresh] = useReducer((state) => {
    return state + 1;
  }, 0);
  const optionsRef = useRef(options);

  optionsRef.current = options;
 
  useEffect(() => {
    let didCancel = false;

    const fetchData = async () => {
      try {
        let res = !!optionsRef.current?.noRetry ? await cFetch(url, optionsRef.current) : await session.cFetch(url, optionsRef.current);
        const json = await res.json();
        if(didCancel) return;

        setCode(res.status);
        if(res.ok) {
          setResponse(json);
        } else {
          setError(new Error(json));
        }
      } catch (error) {
        console.error("error in fetch", error)
        if(didCancel) return;
        setError(error as Error);
      }
    };
    fetchData();

    return () => {
      didCancel = true;
    }
  }, [url, refreshCounter, session]);

  const poll = options?.poll;
  useEffect(() => {
    if(!poll) return;

    const timer = setInterval(() => {
      refresh();
    }, poll);

    return () => {
      clearInterval(timer);
    }
  }, [poll]);

  return { response, error, refresh, code };
};
