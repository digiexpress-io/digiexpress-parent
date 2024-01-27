import { Order } from './table-types';

export function descendingComparator<T>(a: T, b: T, orderBy: keyof T) {

  let aValue = a[orderBy];
  let bValue = b[orderBy];

  if ((typeof aValue) === 'string') {
    aValue = (aValue as unknown as string).toLowerCase() as any;
    bValue = (bValue as unknown as string).toLowerCase() as any;
  }


  if (bValue < aValue) {
    return -1;
  }
  if (bValue > aValue) {
    return 1;
  }
  return 0;
}

export function getComparator<T>(
  order: Order,
  orderBy: keyof T,
): (a: T, b: T) => number {

  return order === 'desc'
    ? (a, b) => descendingComparator(a, b, orderBy)
    : (a, b) => -descendingComparator(a, b, orderBy);
}

export function stableSort<T>(init: readonly T[], comparator: (a: T, b: T) => number) {
  const array = [...(init ? init : [])];
  const stabilizedThis = array.map((el, index) => [el, index] as [T, number]);
  stabilizedThis.sort((a, b) => {
    const aValue = a[0];
    const bValue = b[0];

    const order = comparator(aValue, bValue);
    if (order !== 0) {
      return order;
    }
    return a[1] - b[1];
  });
  return stabilizedThis.map((el) => el[0]);
}