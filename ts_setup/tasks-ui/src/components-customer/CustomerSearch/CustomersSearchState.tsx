import React from 'react';

import { CustomerDescriptor, ImmutableCustomerDescriptor, Customer, ImmutableCustomerStore } from 'descriptor-customer';
import Table from 'table';
import { useBackend } from 'descriptor-backend';


export type CustomerPagination = Table.TablePagination<CustomerDescriptor>;
export type SetCustomerPagination = React.Dispatch<React.SetStateAction<CustomerPagination>>;

function initTable(): CustomerPagination {
  return new Table.TablePaginationImpl<CustomerDescriptor>({
    src: [],
    orderBy: 'created',
    order: 'asc',
    sorted: true,
    rowsPerPage: 15,
  })
}

export function useCustomerSearchState() {
  const backend = useBackend();
  const [content, setContent] = React.useState(initTable());
  const [state, setState] = React.useState<ImmutableCustomersSearchState>(new ImmutableCustomersSearchState({}));
  const [loading, setLoading] = React.useState<boolean>(false);
  const { searchString, isSearchStringValid, records: found } = state;
  
  React.useEffect(() => {
    if (isSearchStringValid) {
      setLoading(true);
      new ImmutableCustomerStore(backend.store).findCustomers(searchString).then(newRecords => {
        setState(prev => prev.withRecords(newRecords));
        setLoading(false);
      });
    }
  }, [searchString, isSearchStringValid]);
  
  React.useEffect(() => setContent(prev => prev.withSrc(found)), [found]);  



  return React.useMemo(() => {
    function setStoring(key: string, _direction: string) {
      setContent(prev => prev.withOrderBy(key as (keyof CustomerDescriptor)));
    }
    function setSearchString(value: string) {
      setState(prev => prev.withSearchString(value))
    } 
  
    return {
      content, setContent,
      loading,
      searchString, 
      setSearchString,
      setStoring,
      isSearchStringValid

    };
  }, [content, loading, setContent, state, isSearchStringValid, searchString]);
}


export interface CustomersSearchState {
  records: CustomerDescriptor[];
  searchString: string;
  isSearchStringValid: boolean;
}

export interface CustomerStateReducer {
  withSearchString(searchString: string): CustomersSearchState;
  withRecords(searchResult: Customer[]): CustomersSearchState;
}


export class ImmutableCustomersSearchState implements CustomersSearchState, CustomerStateReducer {
  private _records: CustomerDescriptor[];
  private _searchString: string;
  private _isSearchStringValid: boolean;

  constructor(init: Partial<CustomersSearchState>) {
    this._records = init.records ?? [];
    this._searchString = init.searchString ?? '';
    this._isSearchStringValid = init.isSearchStringValid ?? false;
  }
  withSearchString(searchString: string): ImmutableCustomersSearchState {
    const isSearchStringValid: boolean = searchString.trim().length > 2;
    return new ImmutableCustomersSearchState({ ...this.clone(), searchString, isSearchStringValid });
  }
  withRecords(searchResult: Customer[]): ImmutableCustomersSearchState {
    const records: CustomerDescriptor[] = searchResult.map(customer => new ImmutableCustomerDescriptor(customer));
    return new ImmutableCustomersSearchState({ ...this.clone(), records });
  }

  clone(): CustomersSearchState {
    const init = this;
    return {
      records: init.records,
      searchString: init.searchString,
      isSearchStringValid: init.isSearchStringValid,
    }
  }
  get records() { return this._records }
  get searchString() { return this._searchString }
  get isSearchStringValid() { return this._isSearchStringValid }
}