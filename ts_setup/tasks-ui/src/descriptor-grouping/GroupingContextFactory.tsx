import React from 'react';

import { Collection, DataType, GroupReducer } from './grouping-types';
import { initReducer } from './initMethods';
import { ImmutableCollection } from './ImmutableCollection';

export interface GroupingContextType<D extends DataType> {
  reducer: GroupReducer<D>;
  collection: Collection<D>;
}

export interface GroupingProps<D extends DataType> {
  children: React.ReactElement;
  config: ImmutableCollection<D>;
}

export interface FactoryCreatedContext<D extends DataType> {
  Context: React.Context<GroupingContextType<D>>;
  Provider: React.FC<GroupingProps<D>>;
  hooks: {
    useGrouping: () => GroupingContextType<D>;
  };
}

export function getInstance<D extends DataType>(): FactoryCreatedContext<D> {

  const GroupingContext = React.createContext<GroupingContextType<D>>({} as any);

  function GroupingProvider(props: GroupingProps<D>) {
    const { config } = props;
    const [collection, setCollection] = React.useState<ImmutableCollection<D>>(config);
    const reducer: GroupReducer<D> = React.useMemo(() => initReducer(setCollection), [setCollection]);
    const contextValue: GroupingContextType<D> = React.useMemo(() => ({ reducer, collection }), [collection, reducer]);

    return (<GroupingContext.Provider value={contextValue}>{props.children}</GroupingContext.Provider>);
  }

  function useGrouping() {
    const result: GroupingContextType<D> = React.useContext(GroupingContext);
    return result;
  }

  return {
    Context: GroupingContext,
    Provider: GroupingProvider,
    hooks: { useGrouping }
  };
}