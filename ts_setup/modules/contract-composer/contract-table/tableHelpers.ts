import { ContractApi } from '@dxs-ts/contract-api';
import { FilterFnOption, Row } from "@tanstack/react-table";


const statusOrder: Record<ContractApi.ContractStatusType, number> = {
  ACTIVE: 0,
}

export function taskSortingFn(rowA: Row<ContractApi.ContractSummary>, rowB: Row<ContractApi.ContractSummary>, columnId: string) {
  const a = rowA.original[columnId as keyof ContractApi.ContractSummary];
  const b = rowB.original[columnId as keyof ContractApi.ContractSummary];

  switch (columnId) {
    case 'contractStatus': {
      const aVal = statusOrder[a as ContractApi.ContractStatusType] ?? -1;
      const bVal = statusOrder[b as ContractApi.ContractStatusType] ?? -1;
      return aVal - bVal;
    }
    default: {
      if (typeof a === 'string' && typeof b === 'string') {
        return a.localeCompare(b);
      }
      return 0;
    }
  }
}

export const filterContractRefOrSubjectFn: FilterFnOption<ContractApi.ContractSummary> = (row, _columnId: string, filterValue: string[]) => {
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

export const filterStringOrArrayFn: FilterFnOption<ContractApi.ContractSummary> = (row, columnId: string, initFilters: string | string[]) => {
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

