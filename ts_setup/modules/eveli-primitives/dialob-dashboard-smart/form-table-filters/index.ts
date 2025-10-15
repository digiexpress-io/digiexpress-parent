import { DashboardItem } from "@dxs-ts/eveli-api";
import { anyDateFilter, TableDateFilter } from "@dxs-ts/xui-table";
import { FilterFnOption } from "@tanstack/react-table";



export const filterDateGte_latestTagDate: FilterFnOption<DashboardItem> = (row, _columnId: string, filterValue: TableDateFilter) => {
  const latestTagDate = row.original.latestTagDate;
  return anyDateFilter(latestTagDate, filterValue);
}

export const filterDateGte_lastSaved: FilterFnOption<DashboardItem> = (row, _columnId: string, filterValue: TableDateFilter) => {
  const lastSaved = row.original.metadata.lastSaved;
  return anyDateFilter(lastSaved, filterValue);
}

