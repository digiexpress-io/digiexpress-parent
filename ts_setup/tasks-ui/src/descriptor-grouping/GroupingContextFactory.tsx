import React from 'react';

import { Collection, DataType, GroupReducer, Group } from './grouping-types';
import { ImmutableCollection } from './ImmutableCollection';

export interface GroupingContextType<D extends DataType> {
  reducer: GroupReducer<D>;
  collection: Collection<D>;
  getByGroupId(id: string): Group;
  getByGroupCount(id: string): number;
}

export interface GroupingProps<D extends DataType> {
  children: React.ReactElement;
  init: ImmutableCollection<D>;
}

export interface FactoryCreatedContext<D extends DataType> {
  Context: React.Context<GroupingContextType<D>>;
  Provider: React.FC<GroupingProps<D>>;
  hooks: {
    useGrouping: () => GroupingContextType<D>;
  };
}

function initReducer<D extends DataType>(
  setCollection: React.Dispatch<React.SetStateAction<ImmutableCollection<D>>>,
): GroupReducer<D> {

  const result: GroupReducer<D> = {
    withData(newInput) {
      setCollection(prev => prev.withOrigin(newInput));
    },
    withGroupBy(classifierName, definition, groupValues) {
      setCollection(prev => prev.withGroupBy(classifierName, definition, groupValues));
    },
  };
  return Object.freeze(result);
}

export function getInstance<D extends DataType>(): FactoryCreatedContext<D> {

  const GroupingContext = React.createContext<GroupingContextType<D>>({} as any);

  function GroupingProvider(props: GroupingProps<D>) {
    const { init } = props;
    const [collection, setCollection] = React.useState<ImmutableCollection<D>>(init);
    const reducer: GroupReducer<D> = React.useMemo(() => initReducer(setCollection), [setCollection]);

    const contextValue: GroupingContextType<D> = React.useMemo(() => {
      function getByGroupId(id: string): Group {
        const found = collection.groups.find(g => g.id === id);
        return found!;
      }
      function getByGroupCount(id: string): number { return getByGroupId(id)?.value.length ?? 0; }

      return { reducer, collection, getByGroupId, getByGroupCount};
    }, [collection, reducer]);

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