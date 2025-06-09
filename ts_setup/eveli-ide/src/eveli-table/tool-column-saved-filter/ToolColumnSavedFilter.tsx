import React from 'react';
import {  Button, ButtonGroup, generateUtilityClass, styled, Dialog, DialogTitle, DialogContent, TextField, DialogActions } from '@mui/material';

import composeClasses from '@mui/utils/composeClasses';
import { Column, Table } from '@tanstack/react-table';
import { useIntl } from 'react-intl';
import { TableState } from '../table-state';
import { useSavedTableFilters } from '../table-state/saved-table-filters';

export interface ToolColumnSavedFilterProps {
  table: Table<any>;
  tableId: string;
  state: [TableState, React.Dispatch<React.SetStateAction<TableState>>];
}


export const ToolColumnSavedFilter: React.FC<ToolColumnSavedFilterProps> = (props) => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  const backend = useSavedTableFilters(props.tableId);
  const [openSaveAs, setOpenSaveAs] = React.useState<boolean>(false);
  const [tableState, setTableState] = props.state;


  function handleClearFilters(event: React.MouseEvent<HTMLElement>, columnId: string) {
    
  }
  function toggleFilter(event: React.MouseEvent<HTMLElement>, col: Column<any, unknown>, value: string) {
  }

  function handleSaveDefault() {
    backend.onSave(tableState)
  }
  function handleOpenSaveAs() {
    setOpenSaveAs(true);
  }
  function handleCloseSaveAs() {
    setOpenSaveAs(false);
  }

  return (
    <Root className={classes.root}>
      <SaveAsDialog {...props} open={openSaveAs} onClose={handleCloseSaveAs}/>

      <ButtonGroup size='small' >
        <Button onClick={handleSaveDefault}>
          {intl.formatMessage({ id: 'eveli.table.saveasdefault', defaultMessage: 'Save as default' })}
        </Button>
        <Button onClick={handleOpenSaveAs}>
          {intl.formatMessage({ id: 'eveli.table.saveas', defaultMessage: 'Save as ...' })}
        </Button>
      </ButtonGroup>

      {(backend.filters ?? []).map(filter => (<div key={filter.id} onClick={() => {
        setTableState(prev => prev.restore(filter.filter))
      }}>{filter.name}</div>))}
    </Root>
  )
}


const SaveAsDialog: React.FC<ToolColumnSavedFilterProps & { open: boolean, onClose: () => void}> = (props) => {
  const backend = useSavedTableFilters(props.tableId);
  const intl = useIntl();
  const [name, setName] = React.useState<string>('');

  function handleChangeName(event: React.ChangeEvent<HTMLInputElement>) {
    setName(event.currentTarget.value)
  }

  function handleSaveAs(event: React.SyntheticEvent) {
    event.preventDefault();
    backend.onSave(props.state[0], { label: name, type: 'CREATE' }).then(() => props.onClose());
  }

  return (
    <Dialog open={props.open} onClose={props.onClose} slotProps={{ paper: { component: 'form', onSubmit: handleSaveAs } }}>
      <DialogTitle>{intl.formatMessage({ id: 'eveli.table.saveas.title', defaultMessage: 'Save current filter as' })}</DialogTitle>
      <DialogContent>
        <TextField autoFocus required fullWidth variant='standard' margin='dense' type='text'
          id='filter.name' name='filter.name'
          value={name}
          onChange={handleChangeName}
          label={intl.formatMessage({ id: 'eveli.table.saveas.name', defaultMessage: 'Filter name' })}
        />
      </DialogContent>
      <DialogActions>
        <Button onClick={handleSaveAs}>{intl.formatMessage({ id: 'button.cancel' })}</Button>
        <Button type='submit'> {intl.formatMessage({ id: 'button.accept' })}</Button>
      </DialogActions>
    </Dialog>)
}


const FiltersRootClassName = 'EveliTableDrawerSavedFilters';


const Root = styled('div', {
  name: FiltersRootClassName,
  slot: 'Root',
  overridesResolver: (_props, styles) => {
    return [
      styles.root
    ];
  },

})(({ theme }) => {
  return {
    width: '100%',
    padding: theme.spacing(1),
    gap: theme.spacing(1),
    display: 'flex',
    alignItems: 'left',
    flexDirection: 'column'
  };
});



const useUtilityClasses = () => {
  const slots = {
    root: ['root']
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(FiltersRootClassName, slot);
  return composeClasses(slots, getUtilityClass, {});
}
