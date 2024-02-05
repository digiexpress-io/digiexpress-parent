import { CustomerDescriptor, ImmutableCustomerDescriptor, Customer } from 'descriptor-customer';

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