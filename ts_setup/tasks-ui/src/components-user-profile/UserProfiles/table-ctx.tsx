import React from 'react';
import { Box, TablePagination, TableContainer, Table } from '@mui/material';
import { UserProfileDescriptor, UserProfileDescriptorImpl } from 'descriptor-user-profile';
import { UserProfileAndOrg, UserProfile } from 'client';
import Pagination from 'table';

type UserProfilePagination = Pagination.TablePagination<UserProfileDescriptor>;

type UserProfileSearchState = {
  records: UserProfileDescriptor[];
  searchString: string;
  isSearchStringValid: boolean;
  profile: UserProfileAndOrg;
  today: Date;
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


interface DescriptorTableContextType {
  setState: SetState;
  state: DescriptorTableState;
}

type Mutator = (prev: DescriptorTableStateBuilder) => DescriptorTableStateBuilder;
type SetState = (mutator: Mutator) => void;


class ImmutableUserProfileSearchState implements UserProfileSearchState {
  private _records: UserProfileDescriptor[];
  private _searchString: string;
  private _isSearchStringValid: boolean;
  private _profile: UserProfileAndOrg;
  private _today: Date;

  constructor(init: UserProfileSearchStateInit) {
    this._records = init.records;
    this._searchString = init.searchString;
    this._isSearchStringValid = init.isSearchStringValid;
    this._profile = init.profile;
    this._today = init.today;
  }
  withSearchString(searchString: string): UserProfileSearchState {
    const isSearchStringValid: boolean = searchString.trim().length > 2;
    return new ImmutableUserProfileSearchState({ ...this.clone(), searchString, isSearchStringValid });
  }
  withRecords(searchResult: UserProfile[]): UserProfileSearchState {
    const records: UserProfileDescriptor[] = searchResult.map(customer => new UserProfileDescriptorImpl(customer, this._profile, this._today));
    return new ImmutableUserProfileSearchState({ ...this.clone(), records });
  }

  clone(): UserProfileSearchStateInit {
    const init = this;
    return {
      records: init.records,
      searchString: init.searchString,
      isSearchStringValid: init.isSearchStringValid,
      profile: init.profile,
      today: init.today
    }
  }

  get today() { return this._today }
  get profile() { return this._profile }
  get records() { return this._records }
  get searchString() { return this._searchString }
  get isSearchStringValid() { return this._isSearchStringValid }
}

function initUserProfileSearchState(profile: UserProfileAndOrg): UserProfileSearchState {
  return new ImmutableUserProfileSearchState({
    searchString: '',
    isSearchStringValid: true,
    profile,
    today: new Date(),
    records: []
  });
}



const DescriptorTableContext = React.createContext<DescriptorTableContextType>({} as DescriptorTableContextType);



interface DescriptorTableState {
  popperOpen: boolean;
  popperId?: string;
  anchorEl?: HTMLElement;
}

class DescriptorTableStateBuilder implements DescriptorTableState {
  private _popperOpen: boolean;
  private _popperId?: string;
  private _anchorEl?: HTMLElement;

  constructor(init: DescriptorTableState) {
    this._popperOpen = init.popperOpen;
    this._anchorEl = init.anchorEl;
    this._popperId = init.popperId;
  }
  withPopperOpen(popperId: string, popperOpen: boolean, anchorEl?: HTMLElement): DescriptorTableStateBuilder {
    if (popperOpen && !anchorEl) {
      throw new Error("anchor must be defined when opening popper");
    }
    if (popperId !== this._popperId && anchorEl) {
      return new DescriptorTableStateBuilder({ popperId, popperOpen: true, anchorEl });
    }

    return new DescriptorTableStateBuilder({ popperId, popperOpen, anchorEl });
  }
  get popperId() { return this._popperId }
  get popperOpen() { return this._popperOpen }
  get anchorEl() { return this._anchorEl }
}

const initTableState = new DescriptorTableStateBuilder({ popperOpen: false });

const Provider: React.FC<{ children: React.ReactElement }> = ({ children }) => {
  const [state, setState] = React.useState(initTableState);
  const setter: SetState = React.useCallback((mutator: Mutator) => setState(mutator), [setState]);
  const contextValue: DescriptorTableContextType = React.useMemo(() => {
    return { state, setState: setter };
  }, [state, setter]);


  return (<DescriptorTableContext.Provider value={contextValue}>{children}</DescriptorTableContext.Provider>);
};

const useTable = () => {
  const result: DescriptorTableContextType = React.useContext(DescriptorTableContext);
  return result;
}

function CustomerTable(props: TableProps) {
  const { loading, defaultOrderBy, group } = props;
  const { Header, Rows } = props.children;
  const { records } = group;

  const [content, setContent] = React.useState(new Pagination.TablePaginationImpl<UserProfileDescriptor>({
    src: records ?? [],
    orderBy: defaultOrderBy,
    sorted: false
  }));

  React.useEffect(() => {
    setContent((c: UserProfilePagination) => c.withSrc(records ?? []));
  }, [records, setContent]);

  return (<Provider>
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
  </Provider>
  );
}

export { Provider, useTable, CustomerTable, initUserProfileSearchState };
export type { DescriptorTableStateBuilder, DescriptorTableContextType, DescriptorTableState, TableConfigProps, UserProfileSearchState };


