import { Header } from "@tanstack/react-table";




export function useHeaderSearchState(header: Header<unknown, unknown>) {
  const filterValue = header.column.getFilterValue();

  const isString = typeof header.column.getFilterValue() === 'string';
  const valueAsString: string | undefined = isString ? header.column.getFilterValue() as string : undefined;
  const valueAsArray: string[] = isString ? [] : (header.column.getFilterValue() as string[] ?? []);
  const valueAsDate: Date | undefined = (filterValue as { date?: Date })?.date;


  function nextState(selected: string) {
    const next = valueAsArray.includes(selected) ?
      valueAsArray.filter((value) => value !== selected) :
      [...valueAsArray, selected];
    return next;
  }


  const isApplied = !!filterValue && (
    valueAsString ||
    valueAsArray.length > 0 ||
    valueAsDate
  );


  return {
    isApplied,
    valueAsString,
    valueAsArray,
    nextState
  }
}