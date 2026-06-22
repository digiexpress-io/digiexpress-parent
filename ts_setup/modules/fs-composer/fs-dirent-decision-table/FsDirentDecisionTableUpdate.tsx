import React from 'react';
import { Button, Menu, MenuItem, Divider, SvgIconProps, Box, Stack, Typography } from '@mui/material';
import { useIntl } from 'react-intl';


import { FsIcon, FsIcons } from '../fs-theme';

import { Fs, FsDirentBodyProvider } from '@dxs-ts/fs-api';
import { useUtilityClasses, FsDirentDecisionTableRoot } from './useUtilityClasses';
import { UpdateOwnerState, useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentDecisionTableProps } from './FsDirentDecisionTableProps';
import { DecisionTable, DecisionTableHeader, DecisionTableRow, DecisionTableCell } from './table';
import { NameDescHitPolicyEdit, OrderEdit, HeaderEdit, UploadCSV, DownloadCSV, CellEdit } from './editors';
import { ConfirmDialog } from '@dxs-ts/eveli-primitives';




export const FsDirentDecisionTableUpdate: React.FC<FsDirentDecisionTableProps> = (props) => {
  return (<FsDirentBodyProvider direntId={props.direntId}>
    <Internal {...props} />
  </FsDirentBodyProvider>);
}

const Internal: React.FC<FsDirentDecisionTableProps> = (props) => {
  const intl = useIntl();
  const ownerState = useUpdateOwnerState(props);
  const classes = useUtilityClasses();

  const onChange = ownerState.onChangeCommands;
  const decision = ownerState.decision;
  const setEdit = ownerState.setEditMode;

  return (
    <FsDirentDecisionTableRoot className={classes.root}>
      <DecisionTableToolbar ownerState={ownerState} />
      <DecisionTable
        ast={decision}
        onAddRow={() => onChange([{ type: 'ADD_ROW', id: "" }])}
        renderHeader={({ ast, headers }) => (
          <DecisionTableHeader ast={ast} headers={headers} onClick={(h) => setEdit({ header: h })}>
            <Typography variant='subtitle2' sx={{ ml: 'auto' }}>{ast.name} - {ast.hitPolicy}</Typography>
          </DecisionTableHeader>
        )}
        renderRow={(rowProps) => {
          const index = decision.rows.findIndex((r) => r.id === rowProps.row.id);

          const dragProps = {
            draggable: true,
            onDragStart: ownerState.onDragStart(index),
            onDragOver: ownerState.onDragOver(index),
            onDrop: ownerState.onDrop(index),
          };

          return (
            <DecisionTableRow
              {...rowProps}
              dragProps={dragProps}
              onDelete={(id) => ownerState.setConfirmDelete({ type: 'ROW', id })}
            />
          );
        }}
        renderCell={({ row, header, cell }) => (
          <DecisionTableCell
            dt={decision}
            row={row}
            header={header}
            cell={cell}
            onChange={onChange}
            onClick={() => cell && setEdit({ cell })}
          />
        )}
      />


      <ConfirmDialog
        open={!!ownerState.confirmDelete}
        title={intl.formatMessage({ id: 'decisions.deleteConfirmTitle' })}
        message={intl.formatMessage(
          { id: 'decisions.deleteConfirmText' },
          {
            type: intl.formatMessage({
              id: ownerState.confirmDelete?.type === 'ROW'
                ? 'decisions.type.row'
                : 'decisions.type.column'
            })
          }
      )}
        confirmLabel={intl.formatMessage({ id: 'button.confirmDelete' })}
        onCancel={() => ownerState.setConfirmDelete(null)}
        onConfirm={() => {
          if (!ownerState.confirmDelete) return;
          const { type, id } = ownerState.confirmDelete;
          const cmd: Fs.AstCommand = type === 'ROW'
            ? { type: 'DELETE_ROW', id }
            : { type: 'DELETE_HEADER', id };
          onChange([cmd]);
          ownerState.setConfirmDelete(null);
        }}
      />
    </FsDirentDecisionTableRoot>
  );
};


const MenuOption: React.FC<{
  onClick: () => void;
  label: string;
  icon: React.ElementType<SvgIconProps>;
}> = ({ icon, onClick, label }) => {
  const intl = useIntl();
  return (
    <MenuItem onClick={onClick}>
      <Stack direction='row' gap={1}>
        <Box><FsIcon icon={icon} small color='inherit' /></Box>
        <Typography variant='subtitle2'>{intl.formatMessage({ id: label })}</Typography>
      </Stack>
    </MenuItem>
  );
};

const DecisionTableToolbar: React.FC<{ ownerState: UpdateOwnerState }> = ({ ownerState }) => {
  const intl = useIntl();
  const [menuAnchorEl, setMenuAnchorEl] = React.useState<null | HTMLElement>(null);
  const closeMenu = () => setMenuAnchorEl(null);
  const onChange = ownerState.onChangeCommands;
  const decision = ownerState.decision;

  const edit = ownerState.editMode;
  const setEdit = ownerState.setEditMode;

  return (
    <>
      {edit?.meta && <NameDescHitPolicyEdit decision={decision} onChange={onChange} onClose={() => setEdit(undefined)} />}
      {edit?.rowsColumns && <OrderEdit decision={decision} onChange={onChange} onClose={() => setEdit(undefined)} />}
      {edit?.upload && <UploadCSV onChange={onChange} onClose={() => setEdit(undefined)} />}
      {edit?.download && <DownloadCSV decision={decision} onClose={() => setEdit(undefined)} />}
      {edit?.header && <HeaderEdit dt={decision} header={edit.header} onChange={onChange} onClose={() => setEdit(undefined)} />}
      {edit?.cell && <CellEdit dt={decision} cell={edit.cell} onClose={() => setEdit(undefined)} onChange={(cmd) => onChange([cmd])} />}


      <div>
        <Button variant='text' onClick={(e) => setMenuAnchorEl(e.currentTarget)}>
          {intl.formatMessage({ id: 'decisions.toolbar.options' })}
        </Button>

        <Menu anchorEl={menuAnchorEl} open={Boolean(menuAnchorEl)} onClose={closeMenu}>
          <MenuOption icon={FsIcons.AddHeaderIn} label='decisions.toolbar.addInputColumn' onClick={() => { onChange([{ type: 'ADD_HEADER_IN', id: 'in-' + decision.headers.acceptDefs.length }]); closeMenu(); }} />
          <MenuOption icon={FsIcons.AddHeaderOut} label='decisions.toolbar.addOutputColumn' onClick={() => { onChange([{ type: 'ADD_HEADER_OUT', id: 'out-' + decision.headers.returnDefs.length }]); closeMenu(); }} />
          <MenuOption icon={FsIcons.AddRow} label='decisions.toolbar.addRow' onClick={() => { onChange([{ type: 'ADD_ROW', id: '' }]); closeMenu(); }} />
          <MenuOption icon={FsIcons.Organize} label='decisions.toolbar.organize.rows.columns' onClick={() => { setEdit({ rowsColumns: true }); closeMenu(); }} />
          <Divider />
          <MenuOption icon={FsIcons.CsvDownload} label='decisions.toolbar.csvDownload' onClick={() => { setEdit({ download: true }); closeMenu(); }} />
          <MenuOption icon={FsIcons.CsvUpload} label='decisions.toolbar.csvUpload' onClick={() => { setEdit({ upload: true }); closeMenu(); }} />
          <Divider />
          <MenuOption icon={FsIcons.Edit} label='decisions.toolbar.nameAndHitpolicy' onClick={() => { setEdit({ meta: true }); closeMenu(); }} />
        </Menu>

        <Button variant='text' onClick={() => onChange([{ type: 'ADD_HEADER_IN', id: 'in-' + decision.headers.acceptDefs.length }])}>
          {intl.formatMessage({ id: 'decisions.toolbar.addInputColumn' })}
        </Button>
        <Button variant='text' onClick={() => onChange([{ type: 'ADD_HEADER_OUT', id: 'out-' + decision.headers.returnDefs.length }])}>
          {intl.formatMessage({ id: 'decisions.toolbar.addOutputColumn' })}
        </Button>
        <Button variant='text' onClick={() => onChange([{ type: 'ADD_ROW', id: '' }])}>
          {intl.formatMessage({ id: 'decisions.toolbar.addRow' })}
        </Button>
        <Button variant='text' onClick={() => setEdit({ rowsColumns: true })}>
          {intl.formatMessage({ id: 'decisions.toolbar.organize.rows.columns' })}
        </Button>
        <Button variant='text' onClick={() => setEdit({ meta: true })}>
          {intl.formatMessage({ id: 'decisions.toolbar.nameAndHitpolicy' })}
        </Button>
        <Button variant='text' onClick={() => setEdit({ upload: true })}>
          {intl.formatMessage({ id: 'decisions.toolbar.csvUpload' })}
        </Button>
        <Button variant='text' onClick={() => setEdit({ download: true })}>
          {intl.formatMessage({ id: 'decisions.toolbar.csvDownload' })}
        </Button>
      </div>
    </>
  );
}