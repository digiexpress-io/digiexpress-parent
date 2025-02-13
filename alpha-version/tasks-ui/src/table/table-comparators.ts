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

  // Data, Data Index
  const dataByIndex = array.map((el, index) => [el, index] as [T, number]);
  

  dataByIndex.sort((a, b) => {
    const aData = a[0];
    const bData = b[0];
    const order = comparator(aData, bData);
    
    if (order !== 0) {
      return order;
    }

    // fallback to id if sorting result is same
    const aId = (aData as any)['id'];
    const bId = (bData as any)['id'];
    if(aId && bId) {

      const afId: string = aId + "";
      const bfId: string = bId + "";
      const fallback = afId.localeCompare(bfId);
      if(fallback !== 0) {
        return fallback;
      }
    }

    return a[1] - b[1];
  });


  return dataByIndex.map((el) => el[0]);
}