import React from 'react';

import { Box, List, Drawer, ListItemIcon, ListItemText, Divider, ListItemButton, Button } from '@mui/material';
import { SxProps } from '@mui/system';

import EditIcon from '@mui/icons-material/Edit';
import DoubleArrowRoundedIcon from '@mui/icons-material/DoubleArrowRounded';
import CompareArrowsRoundedIcon from '@mui/icons-material/CompareArrowsRounded';
import FileDownloadDoneIcon from '@mui/icons-material/FileDownloadDone';
import UploadIcon from '@mui/icons-material/Upload';

import { FormattedMessage, useIntl } from 'react-intl';

import { WrenchComposerApi as Composer } from '../wrench-setup';
import { HdesApi } from '@/api-wrench';
import { CellEdit, NameDescHitPolicyEdit, UploadCSV, OrderEdit, HeaderEdit } from './editors';
import fileDownload from 'js-file-download'


import Decision from './table';
import { useFetch } from '@dxs-ts/eveli-fetch';


interface EditMode {
  cell?: HdesApi.AstDecisionCell,
  header?: HdesApi.TypeDef,
  meta?: boolean,
  upload?: boolean,
  rowsColumns?: boolean,
  options?: boolean
}

function quotation(input: any) {
  if(input === null || input === undefined) {
    return null;
  }

  return "\"" + input.replaceAll('"', '\\"') + "\""  
}

const saveCsv = (decision: HdesApi.AstDecision) => {
  const accepts: HdesApi.TypeDef[] = [...decision.headers.acceptDefs].sort((a, b) => a.order - b.order);
  const returns: HdesApi.TypeDef[] = [...decision.headers.returnDefs].sort((a, b) => a.order - b.order);
  const rows = decision.rows.sort((a, b) => a.order - b.order);
  const headers: HdesApi.TypeDef[] = [...accepts, ...returns];

  const line0 = headers.map(h => h.name).join(";");
  const lines = rows.map(row => {
    const cells: Record<string, HdesApi.AstDecisionCell> = {};
    row.cells.forEach(e => cells[e.header] = e);
    return headers
      .map(header => cells[header.id])
      .map(c => `${c.value ? c.value : ''}`)
      .join(";")

  }).join("\r\n");
  fileDownload(line0 + "\r\n" + lines, decision.name + '.csv')
}

const DrawerOption: React.FC<{
  onClick: () => void;
  label: string;
  icon: React.ReactElement;
}> = ({ icon, onClick, label }) => {
  const itemSx: SxProps = { color: "text.primary" }
  return (<ListItemButton onClick={onClick}>
    <ListItemIcon sx={itemSx}>{icon}</ListItemIcon>
    <ListItemText sx={itemSx}>
      <Box component="span" sx={itemSx}>
        <FormattedMessage id={label} />
      </Box>
    </ListItemText>
  </ListItemButton>);
}


const DrawerSection: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  return <>
    <Box sx={{ width: "350px" }}><List>{children}</List></Box>
    <Divider orientation="vertical" flexItem color="secondary.dark" />
  </>
}

const DecisionEdit: React.FC<{ decision: HdesApi.Entity<HdesApi.AstDecision> }> = ({ decision }) => {
  const [draggedIndex, setDraggedIndex] = React.useState<number | null>(null);
  const { service, actions, session } = Composer.useComposer();
  const update = session.pages[decision.id];

  const commands = React.useMemo(() => update ? update.value : decision.source.commands, [decision, update]);
  const [ast, setAst] = React.useState<HdesApi.AstDecision | undefined>();
  const [edit, setEdit] = React.useState<EditMode | undefined>();
  const intl = useIntl(); 
  const { getCommands } = useFetch('worker/rest/api/assets/wrench/commands/$id.GET', {});


  const onChange = (newCommands: HdesApi.AstCommand[]) => {
    actions.handlePageUpdate(decision.id, [...commands, ...newCommands])
  }

  const decisionId = decision.id;

  React.useEffect(() => {
    service.ast(decisionId, commands).then(data => {
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
    {edit?.cell ? <CellEdit dt={ast} cell={edit?.cell} onClose={() => setEdit(undefined)} onChange={(command) => onChange([command])} /> : null}
    {edit?.header ? <HeaderEdit dt={ast} header={edit.header} onChange={onChange} onClose={() => setEdit(undefined)} /> : null}

    <Drawer anchor="top" open={edit?.options} onClose={() => setEdit(undefined)} sx={{ zIndex: "10000" }}>
      <Box sx={{ display: "flex", backgroundColor: "secondary.main", color: "primary.contrastText" }}>
        <DrawerSection>
          <DrawerOption label='decisions.toolbar.addInputColumn' icon={<DoubleArrowRoundedIcon sx={{ transform: "rotate(-180deg)" }} />} onClick={() => onChange([{ type: 'ADD_HEADER_IN', id: "in-" + ast.headers.acceptDefs.length + 1 }])} />
          <DrawerOption label='decisions.toolbar.addOutputColumn' icon={<DoubleArrowRoundedIcon />} onClick={() => onChange([{ type: 'ADD_HEADER_OUT', id: "out-" + ast.headers.returnDefs.length + 1 }])} />
          <DrawerOption label='decisions.toolbar.addRow' icon={<DoubleArrowRoundedIcon sx={{ transform: "rotate(90deg)" }} />} onClick={() => onChange([{ type: 'ADD_ROW', id: "" }])} />
        </DrawerSection>
        <DrawerSection>
          <DrawerOption label='decisions.toolbar.csvDownload' icon={<FileDownloadDoneIcon />} onClick={() => saveCsv(ast)} />
          <DrawerOption label='decisions.toolbar.csvUpload' icon={<UploadIcon />} onClick={() => setEdit({ upload: true })} />
          <DrawerOption label='decisions.toolbar.copyCommands' icon={<FileDownloadDoneIcon/>} onClick={async function() {
              const commands = await getCommands(decision.id, service.branchName);


              const javaCommands = commands.map(command => 
                (`ImmutableAstCommand.builder().type(AstCommandValue.${command.type}).value(${quotation(command.value)}).id(${quotation(command.id)}).build()`)
              ).join(",\r\n");

              const text = JSON.stringify(commands, null, 2) + "\r\n"+ "\r\n" + javaCommands;
              setTimeout(() => {
                navigator.clipboard.writeText(text);
                console.log(text);
              })
          }} />
        </DrawerSection>
        <DrawerSection>
          <DrawerOption label="decisions.toolbar.nameAndHitpolicy" icon={<EditIcon />} onClick={() => setEdit({ meta: true })} />
          <DrawerOption label="decisions.toolbar.organize.rows.columns" icon={<CompareArrowsRoundedIcon />} onClick={() => setEdit({ rowsColumns: true })} />
        </DrawerSection>
      </Box>
    </Drawer>


    <Decision.Table ast={ast}
      renderHeader={headerProps => (
        <Decision.Header {...headerProps} onClick={(header) => setEdit({ header })}>
          <Button variant='text' onClick={() => setEdit({ options: true })}>
            {`${ast.name} - ${intl.formatMessage({id: "decisions.table.hitpolicy"})}: ${ast.hitPolicy}`}
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
            onDelete={(id) => onChange([{ type: 'DELETE_ROW', id }])}
          />
        );
      }}
      
      renderCell={cellProps => <Decision.Cell onChange={onChange} {...cellProps} dt={ast} errors={decision.errors} onClick={() => setEdit({ cell: cellProps.cell })} />}
      
      onAddRow={() => onChange([{ type: 'ADD_ROW', id: "" }])}
    />
  </Box >);
}

export type { };
export { DecisionEdit };
