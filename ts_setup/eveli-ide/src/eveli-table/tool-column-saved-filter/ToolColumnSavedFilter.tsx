import React from 'react';
import {
  Button, Dialog, DialogTitle, DialogContent, TextField, DialogActions,
  Typography, Box, ListItem, ListItemText, ListItemButton,
} from '@mui/material';
import ModeEditOutlineOutlinedIcon from '@mui/icons-material/ModeEditOutlineOutlined';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';


import { Column, Table } from '@tanstack/react-table';
import { useIntl } from 'react-intl';
import { TableState } from '../table-state';
import { SavedFilter, useSavedTableFilters } from '../table-state/saved-table-filters';
import { Root, StyledFilterItem, useUtilityClasses } from './useUtilityClasses';


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

  const filters = (backend.filters ?? []);

  const currentFilterValue = JSON.stringify(tableState.copy());
  const active = filters
    .filter(({ filter }) => JSON.stringify(filter) === currentFilterValue)
    .map(e => e.id);


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
      <SaveAsDialog {...props} open={openSaveAs} onClose={handleCloseSaveAs} />

      <div className={classes.optionButtons}>
        <Button onClick={handleSaveDefault}>
          {intl.formatMessage({ id: 'eveli.table.saveasdefault', defaultMessage: 'Save as default' })}
        </Button>
        <Button onClick={handleOpenSaveAs} variant='outlined'>
          {intl.formatMessage({ id: 'eveli.table.saveas', defaultMessage: 'Save as ...' })}
        </Button>
      </div>

      {filters.map(filter => (
        <FilterItem key={filter.id} enabled={active.includes(filter.id)} onClick={() => {
          setTableState(prev => prev.restore(filter.filter))
        }} filter={filter}
        />
      ))}
    </Root>
  )
}


const SaveAsDialog: React.FC<ToolColumnSavedFilterProps & { open: boolean, onClose: () => void }> = (props) => {
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

  function handleCancel() {
    setName('')
    props.onClose();
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
        <Button variant='outlined' onClick={handleCancel}>{intl.formatMessage({ id: 'button.cancel' })}</Button>
        <Button type='submit' onClick={handleSaveAs} disabled={!name.trim()}> {intl.formatMessage({ id: 'button.accept' })}</Button>
      </DialogActions>
    </Dialog>)
}





const FilterItem: React.FC<{ filter: SavedFilter, onClick: () => void, enabled: boolean }> = ({ filter, enabled, onClick }) => {
  const classes = useUtilityClasses();

  return (
    <StyledFilterItem>
      <ListItem dense disableGutters disablePadding>
        <ListItemButton onClick={onClick}>
          {enabled ? <CheckCircleIcon className={classes.activeFilter} /> : <CheckCircleIcon visibility='hidden' />}
          <ListItemText>
            <Typography variant='subtitle2'>{filter.name}</Typography>
          </ListItemText>
        </ListItemButton>
      </ListItem>

      <Box flexGrow={1} />
      <>
        <ModeEditOutlineOutlinedIcon />
        <DeleteOutlineIcon />
      </>

    </StyledFilterItem>
  )
}
