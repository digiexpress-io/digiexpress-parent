import { LedgerApi } from '@dxs-ts/ledger-api';
import { FilterFnOption, Row } from "@tanstack/react-table";



export function taskSortingFn(rowA: Row<LedgerApi.LedgerSummary>, rowB: Row<LedgerApi.LedgerSummary>, columnId: string) {
  const a = rowA.original[columnId as keyof LedgerApi.LedgerSummary];
  const b = rowB.original[columnId as keyof LedgerApi.LedgerSummary];

  switch (columnId) {

    default: {
      if (typeof a === 'string' && typeof b === 'string') {
        return a.localeCompare(b);
      }
      return 0;
    }
  }
}

export const filterContractRefOrSubjectFn: FilterFnOption<LedgerApi.LedgerSummary> = (row, _columnId: string, filterValue: string[]) => {
  const cleanedFilterValues = Array.isArray(filterValue) ? filterValue.map((filter) => filter.toLowerCase()) : [(filterValue as string).toLowerCase()];

  if (!filterValue || filterValue.length === 0) {
    return true;
  }

  const extracted: string[] = [
    row.original.contractNumber?.toLowerCase() || ''
  ]


  return cleanedFilterValues.some((filter) => {
    return extracted.some(value => value.includes(filter));
  })
}


function normalize(input: string | string[]): string[] {
  const normalized: string[] = [];
  if (Array.isArray(input)) {
    normalized.push(...input);
  } else if (input) {
    normalized.push(input);
  }

  return normalized
    .filter(value => !!value?.trim())
    .map(value => value.toLowerCase())
}

export const filterStringOrArrayFn: FilterFnOption<LedgerApi.LedgerSummary> = (row, columnId: string, initFilters: string | string[]) => {
  const filters = normalize(initFilters);
  if (filters.length === 0) {
    return true;
  }
  const rawValue: string | string[] | undefined | null = row.getValue(columnId);
  if (rawValue === null || rawValue === undefined) {
    return false;
  }
  const valueToFilter = normalize(rawValue);
  return filters.some((filter) => valueToFilter.some((target) => target.includes(filter))
  );
}

