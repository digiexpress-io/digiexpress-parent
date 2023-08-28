import React from 'react';
import { Box, TablePagination, TableContainer, Table } from '@mui/material';
import Client from '@taskclient';


interface TableRenderProps<R> {
  loading: boolean;
  content: Client.TablePagination<R>,
  setContent: React.Dispatch<React.SetStateAction<Client.TablePagination<R>>>,
}

interface TableProps<Record, Ext extends object> {
  render: {
    ext: Ext;
    Header: React.ElementType<TableRenderProps<Record> & Ext>;
    Rows: React.ElementType<TableRenderProps<Record> & Ext>;
  },
  data: {
    defaultOrderBy: keyof Record,
    loading: boolean;
    records: Record[] | undefined
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
    if(popperOpen && !anchorEl) {
      throw new Error("anchor must be defined when opening popper");
    }
    if(popperId !== this._popperId && anchorEl) {
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
  
  
  return (<DescriptorTableContext.Provider value={contextValue}>{ children }</DescriptorTableContext.Provider>);
};

const useTable = () => {
  const result: DescriptorTableContextType = React.useContext(DescriptorTableContext);
  return result;
}


function CustomTable<Record extends object, Ext extends {}>(props: TableProps<Record, Ext>) {
  const { records, loading, defaultOrderBy } = props.data;
  const { Header, Rows, ext } = props.render;

  const [content, setContent] = React.useState(new Client.TablePaginationImpl<Record>({
    src: records ?? [],
    orderBy: defaultOrderBy,
    sorted: false
  }));

  React.useEffect(() => {
    setContent((c: Client.TablePagination<Record>) => c.withSrc(records ?? []));
  }, [records, setContent]);

  return (<Provider>
    <Box sx={{ width: '100%' }}>
      <TableContainer>
        <Table size='small'>
          {/** @ts-ignore */}
          <Header {...ext} content={content} loading={loading} setContent={setContent} />
          {/** @ts-ignore */}
          <Rows {...ext} content={content} loading={loading} setContent={setContent} />
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
              onPageChange={(_event, newPage) => setContent((state: Client.TablePagination<Record>) => state.withPage(newPage))}
              onRowsPerPageChange={(event: React.ChangeEvent<HTMLInputElement>) => setContent((state: Client.TablePagination<Record>) => state.withRowsPerPage(parseInt(event.target.value, 10)))}
            />)
        }
      </Box>
    </Box>
  </Provider>
  );
}



export { Provider, useTable, CustomTable };
export type { DescriptorTableStateBuilder, DescriptorTableContextType, DescriptorTableState, TableRenderProps };


