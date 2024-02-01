import React from 'react';


import Context from 'context';
import Table from 'table';

import { TenantEntryDescriptor } from 'descriptor-dialob';
import { LayoutList, LayoutListFiller, LayoutListItem } from 'components-generic';

import DialobItemActive from './DialobItemActive';
import { DialobItem } from './DialobItem';
import { DialobListPagination } from './DialobListPagination';
import { DialobListNav } from './DialobListNav';


type FormPaginationType = Table.TablePagination<TenantEntryDescriptor>;


const initTable = (records: TenantEntryDescriptor[]) => new Table.TablePaginationImpl<TenantEntryDescriptor>({
  src: records,
  orderBy: 'formTitle',
  order: 'asc',
  sorted: true,
  rowsPerPage: 15,
});

const FormItem: React.FC<{ 
  index: number;
  active: TenantEntryDescriptor | undefined;
  value: TenantEntryDescriptor;
  onClick: (entry: TenantEntryDescriptor | undefined) => void;
}> = ({ value, onClick, active, index }) => {

  function handleOnClick() {
    onClick(value);
  }

  return (
    <LayoutListItem key={value.source.id} index={index} active={active?.source.id === value.source.id} onClick={handleOnClick}>
      <DialobItem key={value.source.id} entry={value} />
    </LayoutListItem>);
}

const Forms: React.FC<{ 
  table: FormPaginationType;
  active: TenantEntryDescriptor | undefined;
  onClick: (entry: TenantEntryDescriptor | undefined) => void;
}> = ({ table, onClick, active }) => {

  return (<>
    { table.entries.map((value, index) => (<FormItem index={index} key={value.source.id} value={value} active={active} onClick={onClick} />)) }
    <LayoutListFiller value={table} />
  </>);
}

function applySearchString(desc: TenantEntryDescriptor, searchString: string): boolean {
  const formName: boolean = desc.formName?.toLowerCase().indexOf(searchString) > -1;
  return desc.formName.toLowerCase().indexOf(searchString) > -1 || formName;
}

function filterTable(table: FormPaginationType, tenantEntries: TenantEntryDescriptor[], searchString: string | undefined): FormPaginationType {
  if(!searchString) {
    return table.withSrc(tenantEntries).withPage(0);
  }
  
  const cleaned = searchString.toLowerCase();
  const filtered: TenantEntryDescriptor[] = [];
  for (const value of tenantEntries) {
    if (!applySearchString(value, cleaned)) {
      continue;
    }
    filtered.push(value);
  }

  return table.withSrc(filtered).withPage(0);
}

const DialobFormList: React.FC = () => {

  const tenants = Context.useDialobTenant();
  const entries = tenants.state.tenantEntries;

  const [searchString, setSearchString] = React.useState<string>();
  const [selected, setSelected] = React.useState<TenantEntryDescriptor>();
  const [table, setTable] = React.useState<FormPaginationType>(initTable([]));

  React.useEffect(() => {
    setTable(filterTable(table, entries, searchString));
  }, [entries, searchString]);

  const navigation = null;
  const pagination = <DialobListPagination state={table} setState={setTable} />;
  const active = <DialobItemActive entry={selected} setActiveDialob={setSelected} />;
  const items = <Forms active={selected} onClick={setSelected} table={table} />;

  return (<>
    <DialobListNav onSearch={setSearchString} />
    <LayoutList slots={{ navigation, active, items, pagination }} />
  </>
  );
}

export const DialobList: React.FC = () => {
  const entries = Context.useDialobTenant();
  if (entries.loading) {
    return <>...loading</>
  }

  return (<DialobFormList />);
}

