import { Column, Query } from "@material-table/core";

export const createQueryString = <T extends {}> (query:Query<T>, columns:Column<any>[], dateFields?: string[]) => {
  let result = `page=${query.page || 0}&size=${query.pageSize || 20}`;
  if (query.orderByCollection && query.orderByCollection.length >0) {
    query.orderByCollection.forEach(el => {
      if(el.sortOrder > 0){
        result = `${result}&sort=${String(columns[el.orderBy].field)},${el.orderDirection}`;
      }
    });
  }
  if (query.filters.length) {
    query.filters.forEach(filter => {
      let filterValue = filter.value;
      if (filter.value === 'checked') {
        filterValue = 'true';
      }
      else if (filter.value === 'unchecked') {
        filterValue = 'false';
      }
      if (dateFields && dateFields.indexOf(String(filter.column.field)) > -1) {
        filterValue = (filter.value as Date).toISOString();
      }
      if (!!filterValue && (!Array.isArray(filterValue) || filterValue.length > 0)) {
        result = result + `&${String(filter.column.field)}=${filterValue}`
      }
    })
  }
  return result;
}