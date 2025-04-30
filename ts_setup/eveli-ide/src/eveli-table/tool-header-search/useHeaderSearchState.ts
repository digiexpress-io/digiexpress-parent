import { Header } from "@tanstack/react-table";




export function useHeaderSearchState(header: Header<unknown, unknown>) {  
  const isString = typeof header.column.getFilterValue() === 'string';
  const valueAsString: string | undefined = isString ? header.column.getFilterValue() as string : undefined;
  const valueAsArray: string[] = isString ? [] : (header.column.getFilterValue() as string[] ?? []);
  

  function nextState(selected: string) {
    const next = valueAsArray.includes(selected) ?
      valueAsArray.filter((value) => value !== selected) : 
      [...valueAsArray, selected];
    return next;
  }
  const filterValue = header.column.getFilterValue();
  const isApplied = filterValue !== undefined && (valueAsString || valueAsArray.length > 0);

  return {
    isApplied,
    valueAsString,
    valueAsArray,
    nextState
  }
}