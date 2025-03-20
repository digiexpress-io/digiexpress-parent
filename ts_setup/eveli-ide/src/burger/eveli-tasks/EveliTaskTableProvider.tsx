import React, { createContext, useState } from 'react'


export interface TableState {
  sort: any;
  setSort:(sort:any)=>void;
  filters: any;
  setFilters: (filter:any)=>void;
  paging: any;
  setPaging: (paging:any)=>void;
}

export const EveliTaskTableContext = createContext<TableState>({
  sort:undefined, setSort:()=>{}, 
  filters:undefined, setFilters:()=>{},
  paging:undefined, setPaging: ()=>{}});

export const EveliTaskTableProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [filters, setFilters] = useState<any>();
  const [sort, setSort] = useState<any>();
  const [paging, setPaging] = useState<any>();

  const tableState: TableState = {
    filters,
    setFilters,
    sort,
    setSort,
    paging,
    setPaging
  }

  return (
    <EveliTaskTableContext.Provider value={tableState}>
      {children}
    </EveliTaskTableContext.Provider>
  )
}