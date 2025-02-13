import React from 'react';

import { DataType, TablePagination } from './table-types';
/*
import { initReducer } from './initMethods';



export function initReducer<D extends DataType>(
  setTables: React.Dispatch<React.SetStateAction<TablePagination<D>[]>>,
): TablesReducer<D> {

  const result: TablesReducer<D> = {
    handleOnPageChange(, newPage: number) {
      setTable((state) => state.withPage(newPage));
    }
    
    handleOnRowsPerPageChange(event: React.ChangeEvent<HTMLInputElement>) {
      setTable((state) => state.withRowsPerPage(parseInt(event.target.value, 10)))
    }    
  };
  return Object.freeze(result);
}

export interface TablesContextType<D extends DataType> {
  tables: TablePagination<D>[];
  getByTableId(id: string): TablePagination<D>;
}

export interface TableProps<D extends DataType> {
  children: React.ReactElement;
  init: ImmutableCollection<D>;
}

export interface FactoryCreatedContext<D extends DataType> {
  Context: React.Context<TablesContextType<D>>;
  Provider: React.FC<TableProps<D>>;
  hooks: {
    useTables: () => TablesContextType<D>;
  };
}


export function getInstance<D extends DataType>(): FactoryCreatedContext<D> {

  const TablesContext = React.createContext<TablesContextType<D>>({} as any);

  function TablesProvider(props: TableProps<D>) {
    const { init } = props;
    const [collection, setCollection] = React.useState<ImmutableCollection<D>>(init);
    const reducer: GroupReducer<D> = React.useMemo(() => initReducer(setCollection), [setCollection]);

    const contextValue: TablesContextType<D> = React.useMemo(() => {
      function getByGroupId(id: string): Group {
        const found = collection.groups.find(g => g.id === id);
        return found!;
      }
      function getByTableId(id: string): number { return getByGroupId(id)?.value.length ?? 0; }

      return { reducer, collection, getByGroupId, getByGroupCount};
    }, [collection, reducer]);

    return (<TablesContext.Provider value={contextValue}>{props.children}</TablesContext.Provider>);
  }

  function useTables() {
    const result: TablesContextType<D> = React.useContext(TablesContext);
    return result;
  }

  return {
    Context: TablesContext,
    Provider: TablesProvider,
    hooks: { useTables }
  };
}
*/