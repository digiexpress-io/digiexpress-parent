import { Column } from "@tanstack/react-table";


export function useHeaderItems(header: Column<unknown, unknown>): string[] {
  const almostUniqueValues = header.getFacetedUniqueValues();
  const filterItems: string[] = Array.from(new Set(Array.from(almostUniqueValues.keys())
    .map(key => key as (string | string[]))
    .filter(item => item)
    .filter(item => item.length > 0)
    .map(item => {
      if ((typeof item) === 'string') {
        return [item];
      }
      return item;
    })
    .flatMap(items => items)));
  return filterItems;
}