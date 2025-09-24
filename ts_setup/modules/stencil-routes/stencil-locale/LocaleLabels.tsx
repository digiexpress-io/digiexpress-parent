import React from 'react';
import {
  Box, Typography, IconButton, Popover, List, ListItem, ListItemText, ListItemButton, ListItemIcon,
  Grid2, useTheme, Divider, TextField
} from '@mui/material';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutline';
import EditIcon from '@mui/icons-material/Create';
import WarningAmberRoundedIcon from '@mui/icons-material/WarningAmberRounded';
import CheckIcon from '@mui/icons-material/Check';

import { FormattedMessage, useIntl } from 'react-intl';
import { StencilComposerApi as Composer } from '@dxs-ts/stencil-api';
import { StencilApi } from '@dxs-ts/stencil-api';


interface SelectedValue {
  locale: StencilApi.LocaleId;
  value: string;
}

interface LocaleLabelsProps {
  selected: SelectedValue[];
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
  const theme = useTheme();
  const intl = useIntl();
  const [anchorEl, setAnchorEl] = React.useState<HTMLButtonElement | null>(null);
  const [selected, setSelected] = React.useState<Record<string, SelectedValue>>(toSelectedRecord(props.selected));
  const alreadyDefinedLabel = intl.formatMessage({ id: "sitelocale.label.select.alreadyDefined" });
  const [edit, setEdit] = React.useState<SelectedValue | null>(null);

  const selection: { id: StencilApi.LocaleId; value: string, added: boolean }[] = Object.values(site.locales)
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

  const handleAddLabel = (id: StencilApi.LocaleId) => {
    const newLabel: SelectedValue = { locale: id, value: 'new-text-here' };
    const newSelection: Record<string, SelectedValue> = {};
    Object.values(selected).forEach(s => newSelection[s.locale] = s);
    newSelection[newLabel.locale] = newLabel;
    setSelected(newSelection);
    props.onChange(Object.values(newSelection));
  }

  const handleRemoveLabel = (id: StencilApi.LocaleId) => {
    const newSelection: Record<string, SelectedValue> = {};
    Object.values(selected).filter(s => s.locale !== id).forEach(s => newSelection[s.locale] = s);
    setSelected(newSelection);
    props.onChange(Object.values(newSelection));
  }


  const editField = edit ? (
    <TextField
      fullWidth
      variant="outlined"
      label={intl.formatMessage({ id: "sitelocale.label.table.editLocaleValue" })}
      value={edit.value}
      onChange={(e) => setEdit({ locale: edit.locale, value: e.target.value })}
      onKeyDown={(event) => {
        if (event.key === 'Enter') {
          handleEditEnd();
        }
      }}
    />
  ) : null;

  const labelSection = (
    <>
      <Grid2 container alignItems='center' px={theme.spacing(1)} sx={{ backgroundColor: theme.palette.secondary.main }}>
        <Grid2 size={{ md: 3, lg: 3, xl: 3 }}>
          <Typography variant='subtitle2' fontWeight='bold'><FormattedMessage id="locales.label.table.locale" /></Typography>
        </Grid2>
        <Grid2 size={{ md: 5, lg: 5, xl: 5 }}>
          <Typography variant='subtitle2' fontWeight='bold'><FormattedMessage id="locales.label.table.value" /></Typography>
        </Grid2>

        <Box flexGrow={1} />

        <Grid2 size={{ md: 1, lg: 1, xl: 1 }} display='flex' justifyContent='flex-end'>
          <IconButton
            disabled={(edit ? true : false)}
            onClick={(event) => setAnchorEl(event.currentTarget)}>
            <AddCircleOutlineIcon color='primary' />
          </IconButton>
        </Grid2>
      </Grid2>

      <Divider />

      <Box>
        {rows.length === 0 ? <Box width='100%'>
          <Typography variant="body1" m={theme.spacing(1)}><FormattedMessage id="locales.label.table.noneSet" /></Typography>
          <Box display="flex" alignItems="center">
            <WarningAmberRoundedIcon color='warning' />
            <Typography variant="caption" fontWeight='bold'>
              <FormattedMessage id={"locales.label.title.helper"} />
            </Typography>
          </Box>
        </Box> : null}

        {rows.map((row, index) => (
          <Grid2 container alignItems='center' p={theme.spacing(1)} key={index}>
            <Grid2 size={{ md: 3, lg: 3, xl: 3 }} pl={theme.spacing(2)}><Typography fontWeight='bold'>{site.locales[row.locale]?.body.value}</Typography></Grid2>
            <Grid2 size={{ md: 5, lg: 5, xl: 5 }} pb={theme.spacing(0.5)}
              onClick={() => {
                if (!edit) {
                  setEdit(row);
                  props.onChangeStart();
                }
              }}>
              {edit?.locale === row.locale ? editField : row.value}
            </Grid2>

            <Box flexGrow={1} />

            <Grid2 size={{ md: 1, lg: 1, xl: 1 }} display='flex' justifyContent='flex-end'>
              <IconButton
                disabled={(edit && edit.locale !== row.locale || edit && edit.value.length === 0) ? true : false}
                onClick={() => {
                  if (edit) {
                    handleEditEnd()
                  } else {
                    setEdit(row);
                    props.onChangeStart();
                  }
                }}>
                {edit?.locale === row.locale ? <CheckIcon color='primary' /> : <EditIcon color='primary' />}
              </IconButton>
              <IconButton onClick={() => handleRemoveLabel(row.locale)}>
                <DeleteOutlineIcon color='error' />
              </IconButton>
            </Grid2>
          </Grid2>
        ))}
      </Box>
    </>
  );

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
              <ListItemIcon sx={{ color: 'primary.main' }}>
                <AddCircleOutlineIcon />
              </ListItemIcon>
              <ListItemText primary={`${item.value} ${item.added ? " - " + alreadyDefinedLabel : ""}`} />
            </ListItemButton>
          </ListItem>))}
        </List>
      </Popover>
      <>
        <Typography variant="body2" fontWeight='bold' marginLeft={theme.spacing(2)}>
          <FormattedMessage id={"locales.label.table.title"} />
        </Typography>

        <Box sx={{ marginTop: theme.spacing(1), border: `1px solid ${theme.palette.divider}` }}>
          {labelSection}
        </Box>
      </>
    </>
  );
}

export type { LocaleLabelsProps, SelectedValue }
export { LocaleLabels }

