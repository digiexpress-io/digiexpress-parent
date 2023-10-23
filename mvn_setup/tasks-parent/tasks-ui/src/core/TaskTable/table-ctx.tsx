import React from 'react';
import { Box, TablePagination, TableContainer, Table } from '@mui/material';
import { TaskDescriptor, Group } from 'taskdescriptor';
import Pagination from 'table';

type TaskPagination = Pagination.TablePagination<TaskDescriptor>;

interface TableConfigProps {
  loading: boolean;
  group: Group
  content: TaskPagination,
  setContent: React.Dispatch<React.SetStateAction<TaskPagination>>,
}

interface TableProps {
  config: {
    Header: React.ElementType<TableConfigProps>;
    Rows: React.ElementType<TableConfigProps>;
  },
  data: {
    group: Group,
    defaultOrderBy: keyof TaskDescriptor,
    loading: boolean;
  }
}



interface DescriptorTableContextType {
  setState: SetState;
  state: DescriptorTableState,
}

type Mutator = (prev: DescriptorTableStateBuilder) => DescriptorTableStateBuilder;
type SetState = (mutator: Mutator) => void;

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


function CustomTable(props: TableProps) {
  const { loading, defaultOrderBy, group } = props.data;
  const { Header, Rows } = props.config;
  const { records } = group;

  const [content, setContent] = React.useState(new Pagination.TablePaginationImpl<TaskDescriptor>({
    src: records ?? [],
    orderBy: defaultOrderBy,
    sorted: false
  }));

  React.useEffect(() => {
    setContent((c: TaskPagination) => c.withSrc(records ?? []));
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
              onPageChange={(_event, newPage) => setContent((state: TaskPagination) => state.withPage(newPage))}
              onRowsPerPageChange={(event: React.ChangeEvent<HTMLInputElement>) => setContent((state: TaskPagination) => state.withRowsPerPage(parseInt(event.target.value, 10)))}
            />)
        }
      </Box>
    </Box>
  </Provider>
  );
}



export { Provider, useTable, CustomTable };
export type { DescriptorTableStateBuilder, DescriptorTableContextType, DescriptorTableState, TableConfigProps };


