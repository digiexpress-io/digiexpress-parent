import { DashboardItem } from "@/api-dialob-form";
import { FilterFnOption } from "@tanstack/react-table";



export const filterDateGte_latestTagDate: FilterFnOption<DashboardItem> = (row, _columnId: string, filterValue: Date) => {
  const latestTagDate = row.original.latestTagDate;

  if(!latestTagDate) {
    return false;
  }

  return new Date(latestTagDate) >= filterValue;
}

export const filterDateGte_lastSaved: FilterFnOption<DashboardItem> = (row, _columnId: string, filterValue: Date) => {
  const lastSaved = row.original.metadata.lastSaved;

  if(!lastSaved) {
    return false;
  }

  return new Date(lastSaved) >= filterValue;
}

