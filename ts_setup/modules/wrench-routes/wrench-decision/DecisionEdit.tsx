import React from 'react';

import { Box, Menu, MenuItem, ListItemIcon, ListItemText, Divider, Button, Typography } from '@mui/material';

import { Edit as EditIcon } from '@mui/icons-material';
import { DoubleArrowRounded as DoubleArrowRoundedIcon } from '@mui/icons-material';
import { CompareArrowsRounded as CompareArrowsRoundedIcon } from '@mui/icons-material';
import { FileDownloadDone as FileDownloadDoneIcon } from '@mui/icons-material';
import { Upload as UploadIcon } from '@mui/icons-material';

import { FormattedMessage, useIntl } from 'react-intl';

import { useFetch } from '@dxs-ts/envir-fetch';
import { HdesApi, WrenchComposerApi as Composer } from '@dxs-ts/wrench-api';
import { CellEdit, NameDescHitPolicyEdit, UploadCSV, DownloadCSV, OrderEdit, HeaderEdit } from './editors';


import Decision from './table';


import { ConfirmDialog } from '@dxs-ts/eveli-primitives';


interface EditMode {
  cell?: HdesApi.AstDecisionCell,
  header?: HdesApi.TypeDef,
  meta?: boolean,
  upload?: boolean,
  download?: boolean,
  rowsColumns?: boolean,
}

function quotation(input: any) {
  if(input === null || input === undefined) {
    return null;
  }

  return "\"" + input.replaceAll('"', '\\"') + "\""  
}

const MenuOption: React.FC<{
  onClick: () => void;
  label: string;
  icon: React.ReactElement;
}> = ({ icon, onClick, label }) => {
  return (
    <MenuItem onClick={onClick}>
      <ListItemIcon>{icon}</ListItemIcon>
      <ListItemText>
        <FormattedMessage id={label} />
      </ListItemText>
    </MenuItem>
  );
}

const copyCommandsToClipboard = async (
  decisionId: string,
  branchName: string | undefined,
  getCommands: (id: string, branch: string | undefined) => Promise<HdesApi.AstCommand[]>
) => {
  const commands = await getCommands(decisionId, branchName);

  const javaCommands = commands.map(command =>
    (`ImmutableAstCommand.builder().type(AstCommandValue.${command.type}).value(${quotation(command.value)}).id(${quotation(command.id)}).build()`)
  ).join(",\r\n");

  const text = JSON.stringify(commands, null, 2) + "\r\n"+ "\r\n" + javaCommands;
  setTimeout(() => {
    navigator.clipboard.writeText(text);
    console.log(text);
  })
}

const DecisionEdit: React.FC<{ decision: HdesApi.Entity<HdesApi.AstDecision> }> = ({ decision }) => {
  const [confirmDelete, setConfirmDelete] = React.useState<{ type: 'ROW' | 'COLUMN', id: string } | null>(null);
  const [draggedIndex, setDraggedIndex] = React.useState<number | null>(null);
  const [menuAnchorEl, setMenuAnchorEl] = React.useState<null | HTMLElement>(null);
  const { service, actions, session } = Composer.useComposer();
  const update = session.pages[decision.id];

  const commands: HdesApi.AstCommand[] = React.useMemo(() => update ? update.value : decision.commands, [decision, update]) as HdesApi.AstCommand[];
  const [ast, setAst] = React.useState<HdesApi.AstDecision | undefined>();
  const [edit, setEdit] = React.useState<EditMode | undefined>();
  const intl = useIntl(); 
  const { getCommands } = useFetch('worker/rest/api/assets/wrench/commands/$id.GET', {});
  const decisionId = decision.id;

  const onChange = (newCommands: HdesApi.AstCommand[]) => {
    const allCommands: HdesApi.AstCommand[] = [...commands, ...newCommands];
    actions.handlePageUpdate(decision.id, allCommands);
  }

  React.useEffect(() => {

    service.ast(decisionId, 'DECISION_TABLE', commands).then(data => {
      setAst(data.ast);
    });

  }, [commands, decisionId, service])

  if (!ast) {
    return <span>loading ...</span>
  }

  const handleDragStart = (index: number) => () => {
    setDraggedIndex(index);
  };

  const handleDragOver = (index: number) => (e: React.DragEvent) => {
    e.preventDefault();
  };

  const handleDrop = (targetIndex: number) => () => {
    if (draggedIndex === null || draggedIndex === targetIndex) return;
  
    const sourceRow = ast.rows[draggedIndex];
    const targetRow = ast.rows[targetIndex];
  
    onChange([
      {
        type: 'MOVE_ROW' as const,
        id: sourceRow.id,
        value: targetRow.id,
      }
    ]);
  
    setDraggedIndex(null);
  };  

  return (<Box sx={{ width: '100%', overflow: 'hidden', padding: 1 }}>
    {edit?.meta ? <NameDescHitPolicyEdit decision={ast} onChange={onChange} onClose={() => setEdit(undefined)} /> : null}
    {edit?.rowsColumns ? <OrderEdit decision={ast} onChange={onChange} onClose={() => setEdit(undefined)} /> : null}
    {edit?.upload ? <UploadCSV onChange={onChange} onClose={() => setEdit(undefined)} /> : null}
    {edit?.download ? <DownloadCSV decision={ast} onClose={() => setEdit(undefined)} /> : null}
    {edit?.cell ? <CellEdit dt={ast} cell={edit?.cell} onClose={() => setEdit(undefined)} onChange={(command) => onChange([command])} /> : null}
    {edit?.header ? <HeaderEdit dt={ast} header={edit.header} onChange={onChange} onClose={() => setEdit(undefined)} /> : null}

    <Menu
      anchorEl={menuAnchorEl}
      open={Boolean(menuAnchorEl)}
      onClose={() => setMenuAnchorEl(null)}
    >
      <MenuOption label='decisions.toolbar.addInputColumn' icon={<DoubleArrowRoundedIcon sx={{ transform: "rotate(-180deg)" }} />} onClick={() => { onChange([{ type: 'ADD_HEADER_IN', id: "in-" + ast.headers.acceptDefs.length + 1 }]); setMenuAnchorEl(null); }} />
      <MenuOption label='decisions.toolbar.addOutputColumn' icon={<DoubleArrowRoundedIcon />} onClick={() => { onChange([{ type: 'ADD_HEADER_OUT', id: "out-" + ast.headers.returnDefs.length + 1 }]); setMenuAnchorEl(null); }} />
      <MenuOption label='decisions.toolbar.addRow' icon={<DoubleArrowRoundedIcon sx={{ transform: "rotate(90deg)" }} />} onClick={() => { onChange([{ type: 'ADD_ROW', id: "" }]); setMenuAnchorEl(null); }} />
      <MenuOption label="decisions.toolbar.organize.rows.columns" icon={<CompareArrowsRoundedIcon />} onClick={() => { setEdit({ rowsColumns: true }); setMenuAnchorEl(null); }} />
      <Divider />
      <MenuOption label='decisions.toolbar.csvDownload' icon={<FileDownloadDoneIcon />} onClick={() => { setEdit({ download: true }); setMenuAnchorEl(null); }} />
      <MenuOption label='decisions.toolbar.csvUpload' icon={<UploadIcon />} onClick={() => { setEdit({ upload: true }); setMenuAnchorEl(null); }} />
      <Divider />
      <MenuOption label="decisions.toolbar.nameAndHitpolicy" icon={<EditIcon />} onClick={() => { setEdit({ meta: true }); setMenuAnchorEl(null); }} />
      <MenuOption label='decisions.toolbar.copyCommands' icon={<FileDownloadDoneIcon/>} onClick={async () => {
        await copyCommandsToClipboard(decision.id, service.branchName, getCommands);
        setMenuAnchorEl(null);
      }} />
    </Menu>


    <Decision.Table ast={ast}
      renderHeader={headerProps => (
        <Decision.Header {...headerProps} onClick={(header) => setEdit({ header })}>
          <Button variant='text' onClick={(e) => setMenuAnchorEl(e.currentTarget)}>
            <FormattedMessage id='decisions.toolbar.options' />
          </Button>
          <Button variant='text' onClick={() => onChange([{ type: 'ADD_HEADER_IN', id: "in-" + ast.headers.acceptDefs.length + 1 }])}>
            <FormattedMessage id='decisions.toolbar.addInputColumn'/>
          </Button>
          <Button variant='text' onClick={() => onChange([{ type: 'ADD_HEADER_OUT', id: "out-" + ast.headers.returnDefs.length + 1 }])}>
            <FormattedMessage id='decisions.toolbar.addOutputColumn'/>
          </Button>
          
          <Button variant='text' onClick={() => onChange([{ type: 'ADD_ROW', id: "" }])}>
            <FormattedMessage id='decisions.toolbar.addRow'/>
          </Button>

          <Button variant='text' onClick={() => setEdit({ rowsColumns: true })}>
            <FormattedMessage id='decisions.toolbar.organize.rows.columns'/>
          </Button>

          <Typography variant='subtitle2' sx={{ ml: 'auto' }}>{ast.name} - {ast.hitPolicy}</Typography>
        </Decision.Header>
      )}

      renderRow={(rowProps) => {
        const index = ast.rows.findIndex((r) => r.id === rowProps.row.id);
      
        const dragProps = {
          draggable: true,
          onDragStart: handleDragStart(index),
          onDragOver: handleDragOver(index),
          onDrop: handleDrop(index),
        };
      
        return (
          <Decision.Row
            {...rowProps}
            dragProps={dragProps}
            onDelete={(id) => setConfirmDelete({ type: 'ROW', id })}
          />
        );
      }}
      
      renderCell={cellProps => <Decision.Cell onChange={onChange} {...cellProps} dt={ast} errors={decision.errors} onClick={() => setEdit({ cell: cellProps.cell })} />}

      onAddRow={() => onChange([{ type: 'ADD_ROW', id: "" }])}
    />

    <ConfirmDialog
      open={!!confirmDelete}
      title={intl.formatMessage({ id: 'decisions.deleteConfirmTitle' })}
      message={intl.formatMessage(
        { id: 'decisions.deleteConfirmText' },
        {
          type: intl.formatMessage({
            id: confirmDelete?.type === 'ROW'
              ? 'decisions.type.row'
              : 'decisions.type.column'
          })
        }
      )}
      confirmLabel={intl.formatMessage({ id: 'button.confirmDelete' })}
      onCancel={() => setConfirmDelete(null)}
      onConfirm={() => {
        if (!confirmDelete) return;
        const { type, id } = confirmDelete;
        const cmd: HdesApi.AstCommand = type === 'ROW'
          ? { type: 'DELETE_ROW', id }
          : { type: 'DELETE_HEADER', id };
        onChange([cmd]);
        setConfirmDelete(null);
      }}
    />

  </Box >);
}

export type { };
export { DecisionEdit };
