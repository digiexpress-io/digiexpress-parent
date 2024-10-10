import React from 'react';
import {
  Box, Typography, Table, TableContainer, TableBody, TableCell, TableRow, TableHead, Paper,
  IconButton, Popover, List, ListItem, ListItemText, ListItemButton, ListItemIcon
} from '@mui/material';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutline';
import EditIcon from '@mui/icons-material/Create';
import WarningAmberRoundedIcon from '@mui/icons-material/WarningAmberRounded';
import CheckIcon from '@mui/icons-material/Check';

import { FormattedMessage, useIntl } from 'react-intl';

import Burger from '@the-wrench-io/react-burger';
import { Composer, StencilClient } from '../context';


interface SelectedValue {
  locale: StencilClient.LocaleId;
  value: string;
}

interface LocaleLabelsProps {
  selected: SelectedValue[];
  disablePaper?: boolean;
  onChange: (selected: SelectedValue[]) => void;
  onChangeStart: () => void;
  onKeyDown?: (event: React.KeyboardEvent<HTMLInputElement>) => void;
}


const toSelectedRecord = (input: SelectedValue[]): Record<string, SelectedValue> => {
  const result: Record<string, SelectedValue> = {};

  for (const selected of input) {
    result[selected.locale] = selected;
  }

  return result;
}


const LocaleLabels: React.FC<LocaleLabelsProps> = (props) => {
  const { site } = Composer.useSession();
  const intl = useIntl();
  const [anchorEl, setAnchorEl] = React.useState<HTMLButtonElement | null>(null);
  const [selected, setSelected] = React.useState<Record<string, SelectedValue>>(toSelectedRecord(props.selected));
  const alreadyDefinedLabel = intl.formatMessage({ id: "sitelocale.label.select.alreadyDefined" });
  const [edit, setEdit] = React.useState<SelectedValue | null>(null);

  const selection: { id: StencilClient.LocaleId; value: string, added: boolean }[] = Object.values(site.locales)
    .map(locale => ({
      id: locale.id,
      value: locale.body.value,
      added: selected[locale.id] ? true : false
    }));

  const rows = Object.values(selected).sort((l0, l1) => l0.locale.localeCompare(l1.locale));
  const selectLocaleToAdd = Boolean(anchorEl);

  const handleEditEnd = () => {
    if (!edit) {
      return;
    }
    const newSelection: Record<string, SelectedValue> = {};
    Object.values(selected).filter(s => s.locale !== edit.locale).forEach(s => newSelection[s.locale] = s);
    newSelection[edit.locale] = { locale: edit.locale, value: edit.value };
    setSelected(newSelection);
    props.onChange(Object.values(newSelection));
    setEdit(null);
  }

  const handleAddLabel = (id: StencilClient.LocaleId) => {
    const newLabel: SelectedValue = { locale: id, value: 'new-text-here' };
    const newSelection: Record<string, SelectedValue> = {};
    Object.values(selected).forEach(s => newSelection[s.locale] = s);
    newSelection[newLabel.locale] = newLabel;
    setSelected(newSelection);
    props.onChange(Object.values(newSelection));
  }

  const handleRemoveLabel = (id: StencilClient.LocaleId) => {
    const newSelection: Record<string, SelectedValue> = {};
    Object.values(selected).filter(s => s.locale !== id).forEach(s => newSelection[s.locale] = s);
    setSelected(newSelection);
    props.onChange(Object.values(newSelection));
  }


  const editField = edit ? (
    <Burger.TextField
      label="sitelocale.label.table.editLocaleValue"
      value={edit.value}
      onEnter={() => handleEditEnd()}
      onChange={(newValue) => setEdit({ locale: edit.locale, value: newValue })}
    />
  ) : null;

  const table = (<Table size="small">
    <TableHead sx={{ backgroundColor: "table.main" }}>
      <TableRow sx={{ borderBottom: 0 }}>
        <TableCell colSpan={3} sx={{ borderBottom: 0 }}>
          <Typography variant="h4" sx={{ fontWeight: 'bold', marginBottom: 1, color: "mainContent.dark" }}>
            <FormattedMessage id={"locales.label.table.title"} />
          </Typography>
        </TableCell>

        <TableCell sx={{ borderBottom: 0 }} align="right">
          <IconButton sx={{ color: 'uiElements.main' }}
            disabled={(edit ? true : false)}
            onClick={(event) => setAnchorEl(event.currentTarget)}>
            <AddCircleOutlineIcon />
          </IconButton>
        </TableCell>
      </TableRow>

      <TableRow>
        <TableCell sx={{ width: "100px" }} colSpan={2} align="left" color="mainContent.contrastText"><FormattedMessage id="locales.label.table.locale" /></TableCell>
        <TableCell sx={{}} colSpan={2} align="left"><FormattedMessage id="locales.label.table.value" /></TableCell>
      </TableRow>
    </TableHead>
    <TableBody>
      {rows.length === 0 ? <TableRow>
        <TableCell colSpan={4}>
          <Typography variant="h5" sx={{ marginBottom: 1, marginTop: 1 }}><FormattedMessage id="transferlist.noItemsSelected" /></Typography>
          <Box display="flex" alignItems="center">
            <WarningAmberRoundedIcon sx={{ color: 'warning.main' }} />
            <Typography variant="caption" sx={{ fontWeight: 'bold', color: "uiElements.main" }}>
              <FormattedMessage id={"locales.label.title.helper"} />
            </Typography>
          </Box>
        </TableCell>
      </TableRow> : null}

      {rows.map((row, index) => (
        <TableRow hover key={index} sx={{ height: "85px" }}>
          <TableCell sx={{ width: "40px" }}>
            <IconButton onClick={() => handleRemoveLabel(row.locale)} sx={{ color: 'uiElements.main' }}>
              <DeleteOutlineIcon />
            </IconButton>
          </TableCell>
          <TableCell align="left">{site.locales[row.locale]?.body.value}</TableCell>
          <TableCell align="left" onClick={() => {
            if (!edit) {
              setEdit(row);
              props.onChangeStart();
            }
          }}>
            {edit?.locale === row.locale ? editField : row.value}
          </TableCell>
          <TableCell align="right">
            <IconButton sx={{ color: 'uiElements.main' }}
              disabled={(edit && edit.locale !== row.locale || edit && edit.value.length === 0) ? true : false}
              onClick={() => {
                if (edit) {
                  handleEditEnd()
                } else {
                  setEdit(row);
                  props.onChangeStart();
                }
              }}>
              {edit?.locale === row.locale ? <CheckIcon /> : <EditIcon />}
            </IconButton>
          </TableCell>
        </TableRow>
      ))}
    </TableBody>
  </Table>);

  return (
    <>
      <Popover id={selectLocaleToAdd ? 'selectLocaleToAdd' : undefined}
        open={selectLocaleToAdd}
        anchorEl={anchorEl}
        onClose={() => setAnchorEl(null)}
        anchorOrigin={{
          vertical: 'bottom',
          horizontal: 'left',
        }}
      >
        <List>
          {selection.map((item, index) => (<ListItem disablePadding key={index}>
            <ListItemButton disabled={item.added} onClick={() => handleAddLabel(item.id)}>
              <ListItemIcon sx={{ color: 'uiElements.main' }}>
                <AddCircleOutlineIcon />
              </ListItemIcon>
              <ListItemText primary={`${item.value} ${item.added ? " - " + alreadyDefinedLabel : ""}`} />
            </ListItemButton>
          </ListItem>))}
        </List>
      </Popover>

      <Box sx={{ marginTop: 1 }}>
        {props.disablePaper ? table : (<TableContainer component={Paper}>{table}</TableContainer>)}
      </Box>
    </>
  );
}

export type { LocaleLabelsProps, SelectedValue }
export { LocaleLabels }

