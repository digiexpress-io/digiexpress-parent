import React from 'react';


interface FsNullLoaderProps<T> {
  state: T | null | undefined;
  children: (state: T) => React.ReactNode;
  fallback?: React.ReactNode;
}

export function FsNullLoader<T>({ state, children, fallback = null }: FsNullLoaderProps<T>) {
  if (state == null) {
    return <>{fallback}</>
  }
  return <>{children(state)}</>;
}