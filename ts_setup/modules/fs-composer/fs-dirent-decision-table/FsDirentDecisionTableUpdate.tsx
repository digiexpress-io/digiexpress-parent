import React from 'react';
import { Button, Menu, MenuItem, Divider, SvgIconProps, Box, Stack, Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsIcon, FsIcons } from '../fs-theme';

import { Fs } from '@dxs-ts/fs-api';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonSave } from '../fs-dirent-button-save';
import { FsDirentButtonDelete } from '../fs-dirent-button-delete';
import { useUtilityClasses, FsDirentDecisionTableRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentDecisionTableUpdateProps } from './FsDirentDecisionTableProps';
import { DecisionTable, DecisionTableHeader, DecisionTableRow, DecisionTableCell } from './table';
import { NameDescHitPolicyEdit, OrderEdit, HeaderEdit, UploadCSV, DownloadCSV } from './editors';
import { useFsu } from '@dxs-ts/fs-api';


interface EditMode {
  header?: Fs.DecisionTypeDef;
  meta?: boolean;
  upload?: boolean;
  download?: boolean;
  rowsColumns?: boolean;
}

interface DecisionTableToolbarProps {
  decision: Fs.DecisionAst;
  edit: EditMode | undefined;
  setEdit: (mode: EditMode | undefined) => void;
  onChange: (commands: Fs.AstCommand[]) => void;
}


export const FsDirentDecisionTableUpdate: React.FC<FsDirentDecisionTableUpdateProps> = (props) => {
  const ownerState = useUpdateOwnerState(props);
  const classes = useUtilityClasses();
  const { push } = useFsu();
  const [edit, setEdit] = React.useState<EditMode | undefined>();

  const onChange = ownerState.onChangeCommands;

  return (
    <FsDirentDecisionTableRoot className={classes.root} ownerState={ownerState}>
      {ownerState.decision && (
        <>
          <DecisionTableToolbar
            decision={ownerState.decision}
            edit={edit}
            setEdit={setEdit}
            onChange={onChange}
          />

          <DecisionTable
            ast={ownerState.decision}
            renderHeader={({ ast, headers }) => (
              <DecisionTableHeader ast={ast} headers={headers}>
                <span />
              </DecisionTableHeader>
            )}
            renderRow={({ row, headers, renderCell }) => (
              <DecisionTableRow row={row} headers={headers} renderCell={renderCell} />
            )}
            renderCell={({ row, header, cell }) => (
              <DecisionTableCell row={row} header={header} cell={cell} />
            )}
          />
        </>
      )}

      <div className={classes.buttonContainer}>
        <FsDirentButtonDelete assetId={props.direntId} />
        <FsDirentButtonCancel onClick={ownerState.onCancel} />
        <FsDirentButtonSave onClick={() => push(ownerState.id)} />
      </div>
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

const DecisionTableToolbar: React.FC<DecisionTableToolbarProps> = ({ decision, edit, setEdit, onChange }) => {
  const intl = useIntl();
  const [menuAnchorEl, setMenuAnchorEl] = React.useState<null | HTMLElement>(null);

  const closeMenu = () => setMenuAnchorEl(null);

  return (
    <>
      {edit?.meta && <NameDescHitPolicyEdit decision={decision as Fs.AstDecision} onChange={onChange} onClose={() => setEdit(undefined)} />}
      {edit?.rowsColumns && <OrderEdit decision={decision} onChange={onChange} onClose={() => setEdit(undefined)} />}
      {edit?.upload && <UploadCSV onChange={onChange} onClose={() => setEdit(undefined)} />}
      {edit?.download && <DownloadCSV decision={decision} onClose={() => setEdit(undefined)} />}
      {edit?.header && <HeaderEdit dt={decision} header={edit.header} onChange={onChange} onClose={() => setEdit(undefined)} />}


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
};
