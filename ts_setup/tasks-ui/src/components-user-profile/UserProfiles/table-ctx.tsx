import React from 'react';
import { Box, TablePagination, TableContainer, Table } from '@mui/material';

import { UserProfile } from 'client';
import Pagination from 'table';

import { UserProfileDescriptor, UserProfileDescriptorImpl } from 'descriptor-user-profile';
import { PopperProvider, usePopper } from 'descriptor-popper';


type UserProfilePagination = Pagination.TablePagination<UserProfileDescriptor>;

type UserProfileSearchState = {
  records: UserProfileDescriptor[];
  searchString: string;
  isSearchStringValid: boolean;
  withSearchString(searchString: string): UserProfileSearchState;
  withRecords(searchResult: UserProfile[]): UserProfileSearchState;
}
type UserProfileSearchStateInit = Omit<UserProfileSearchState, 'withSearchString' | 'withRecords'>;


interface TableConfigProps {
  loading: boolean;
  content: UserProfilePagination;
  group: UserProfileSearchState;
  setContent: React.Dispatch<React.SetStateAction<UserProfilePagination>>;
}

interface TableProps {
  children: {
    Header: React.ElementType<TableConfigProps>;
    Rows: React.ElementType<TableConfigProps>;
  },
  group: UserProfileSearchState;
  defaultOrderBy: keyof UserProfileDescriptor;
  loading: boolean;
}

class ImmutableUserProfileSearchState implements UserProfileSearchState {
  private _records: UserProfileDescriptor[];
  private _searchString: string;
  private _isSearchStringValid: boolean;

  constructor(init: UserProfileSearchStateInit) {
    this._records = init.records;
    this._searchString = init.searchString;
    this._isSearchStringValid = init.isSearchStringValid;
  }
  withSearchString(searchString: string): UserProfileSearchState {
    const isSearchStringValid: boolean = searchString.trim().length > 2;
    return new ImmutableUserProfileSearchState({ ...this.clone(), searchString, isSearchStringValid });
  }
  withRecords(searchResult: UserProfile[]): UserProfileSearchState {
    const records: UserProfileDescriptor[] = searchResult.map(customer => new UserProfileDescriptorImpl(customer));
    return new ImmutableUserProfileSearchState({ ...this.clone(), records });
  }

  clone(): UserProfileSearchStateInit {
    const init = this;
    return {
      records: init.records,
      searchString: init.searchString,
      isSearchStringValid: init.isSearchStringValid
    }
  }

  get records() { return this._records }
  get searchString() { return this._searchString }
  get isSearchStringValid() { return this._isSearchStringValid }
}

function initUserProfileSearchState(): UserProfileSearchState {
  return new ImmutableUserProfileSearchState({
    searchString: '',
    isSearchStringValid: true,
    records: []
  });
}





const useTable = usePopper;

function CustomerTable(props: TableProps) {
  const { loading, defaultOrderBy, group } = props;
  const { Header, Rows } = props.children;
  const { records } = group;

  const [content, setContent] = React.useState<UserProfilePagination>(new Pagination.TablePaginationImpl<UserProfileDescriptor>({
    src: records ?? [],
    orderBy: defaultOrderBy,
    sorted: false
  }));

  React.useEffect(() => {
    setContent((c: UserProfilePagination) => c.withSrc(records ?? []));
  }, [records, setContent]);

  return (<PopperProvider>
    <Box sx={{ width: '100%' }}>
      <TableContainer>
        <Table size='small'>
          <Header content={content} loading={loading} setContent={setContent} group={group} />
          <Rows content={content} loading={loading} setContent={setContent} group={group} />
        </Table>
      </TableContainer>
      <Box display='flex' sx={{ paddingLeft: 1, marginTop: -2 }}>
        <Box alignSelf="center" flexGrow={1}></Box> {
          loading ? null :
            (<TablePagination
              rowsPerPageOptions={content.rowsPerPageOptions}
              component="div"
              count={(records ?? []).length}
              rowsPerPage={content.rowsPerPage}
              page={content.page}
              onPageChange={(_event, newPage) => setContent((state: UserProfilePagination) => state.withPage(newPage))}
              onRowsPerPageChange={(event: React.ChangeEvent<HTMLInputElement>) => setContent((state: UserProfilePagination) => state.withRowsPerPage(parseInt(event.target.value, 10)))}
            />)
        }
      </Box>
    </Box>
  </PopperProvider>
  );
}

export { useTable, CustomerTable, initUserProfileSearchState };
export type { TableConfigProps, UserProfileSearchState };


