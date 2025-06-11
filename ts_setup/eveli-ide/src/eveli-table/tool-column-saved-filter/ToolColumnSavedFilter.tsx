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


type DialogState = (
  { type: 'CREATE' } |
  { type: 'UPDATE', value: SavedFilter })

export const ToolColumnSavedFilter: React.FC<ToolColumnSavedFilterProps> = (props) => {
  const classes = useUtilityClasses();
  const intl = useIntl();
  const backend = useSavedTableFilters(props.tableId);
  const [tableState, setTableState] = props.state;
  const [dialog, setDialog] = React.useState<DialogState>();
  const filters = (backend.filters ?? []);


  function handleClearFilters(event: React.MouseEvent<HTMLElement>, columnId: string) {

  }
  function toggleFilter(event: React.MouseEvent<HTMLElement>, col: Column<any, unknown>, value: string) {
  }

  function handleSaveDefault() {
    backend.onSave(tableState, {
      type: 'CREATE',
      label: 'default'
    })
  }
  function handleOpenSaveAs() {
    setDialog({ type: 'CREATE' });
  }
  function handleClose() {
    setDialog(undefined);
  }
  function handleOpenUpdate(value: SavedFilter) {
    setDialog({ type: 'UPDATE', value });
  }

  function handleDelete(value: SavedFilter) {
    backend.onSave(tableState, { type: 'DELETE', dataId: value.id })
  }

  return (
    <Root className={classes.root}>
      {dialog?.type === 'CREATE' && <SaveAsDialog ownerState={props} onClose={handleClose} />}
      {dialog?.type === 'UPDATE' && <UpdateFilterNameDialog ownerState={props} onClose={handleClose} value={dialog.value} />}

      <div className={classes.optionButtons}>
        <Button onClick={handleSaveDefault}>
          {intl.formatMessage({ id: 'eveli.table.saveasdefault', defaultMessage: 'Save as default' })}
        </Button>
        <Button onClick={handleOpenSaveAs} variant='outlined'>
          {intl.formatMessage({ id: 'eveli.table.saveas', defaultMessage: 'New filter from current' })}
        </Button>
      </div>

      {filters.map(filter => (
        <FilterItem key={filter.id}
          value={filter}
          ownerState={props}
          onDelete={() => handleDelete(filter)}
          onEdit={() => handleOpenUpdate(filter)}
          onRestore={() => setTableState(prev => prev.restore(filter.filter))} 
        />
      ))}
    </Root>
  )
}


const SaveAsDialog: React.FC<{
  ownerState: ToolColumnSavedFilterProps;
  onClose: () => void
}> = (props) => {
  const backend = useSavedTableFilters(props.ownerState.tableId);
  const intl = useIntl();
  const [name, setName] = React.useState<string>('');

  function handleChangeName(event: React.ChangeEvent<HTMLInputElement>) {
    setName(event.currentTarget.value)
  }

  function handleSaveAs(event: React.SyntheticEvent) {
    event.preventDefault();
    backend.onSave(props.ownerState.state[0], { label: name, type: 'CREATE' }).then(() => props.onClose());
  }


  function handleCancel() {
    props.onClose();
  }

  return (
    <Dialog open={true} onClose={props.onClose} slotProps={{ paper: { component: 'form', onSubmit: handleSaveAs } }}>
      <DialogTitle>{intl.formatMessage({ id: 'eveli.table.saveas.title', defaultMessage: 'Save current configuration as filter' })}</DialogTitle>
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



const UpdateFilterNameDialog: React.FC<{
  ownerState: ToolColumnSavedFilterProps;
  value: SavedFilter;
  onClose: () => void;
}> = (props) => {

  const backend = useSavedTableFilters(props.ownerState.tableId);
  const intl = useIntl();
  const [name, setName] = React.useState<string>(props.value.name);

  function handleChangeName(event: React.ChangeEvent<HTMLInputElement>) {
    setName(event.currentTarget.value);
  }

  function handleUpdateName(event: React.SyntheticEvent) {
    event.preventDefault();
    backend.onSave(props.ownerState.state[0], { label: name, type: 'UPDATE', dataId: props.value.id }).then(() => props.onClose());
  }

  function handleCancel() {
    props.onClose();
  }

  return (
    <Dialog open={true} onClose={props.onClose} slotProps={{ paper: { component: 'form', onSubmit: handleUpdateName } }}>
      <DialogTitle>{intl.formatMessage({ id: 'eveli.table.updatefilter.title', defaultMessage: 'Update filter name' })}</DialogTitle>
      <DialogContent>
        <TextField
          fullWidth
          value={name}
          onChange={handleChangeName}
          label={intl.formatMessage({ id: 'eveli.table.saveas.name', defaultMessage: 'Filter name' })}
        />
      </DialogContent>
      <DialogActions>
        <Button variant="outlined" onClick={handleCancel}>
          {intl.formatMessage({ id: 'button.cancel' })}
        </Button>
        <Button type="submit" onClick={handleUpdateName} disabled={!name.trim()}>
          {intl.formatMessage({ id: 'button.accept' })}
        </Button>
      </DialogActions>
    </Dialog>
  );
};


const FilterItem: React.FC<{
  value: SavedFilter;
  ownerState: ToolColumnSavedFilterProps;

  onDelete: () => void;
  onEdit: () => void;
  onRestore: () => void;
}> = ({ onDelete, onEdit, onRestore, value, ownerState }) => {

  const classes = useUtilityClasses();
  const enabled = ownerState.state[0].isActive(value.filter);

  return (
    <StyledFilterItem>
      <ListItem dense disableGutters disablePadding>
        <ListItemButton onClick={onRestore}>
          {enabled ? <CheckCircleIcon className={classes.activeFilter} /> : <CheckCircleIcon visibility='hidden' />}
          <ListItemText>
            <Typography variant='subtitle2'>{value.name}</Typography>
          </ListItemText>
        </ListItemButton>
      </ListItem>
      <Box flexGrow={1} />

      <>
        <ModeEditOutlineOutlinedIcon onClick={onEdit} />
        <DeleteOutlineIcon onClick={onDelete} />
      </>

    </StyledFilterItem>
  )
}
