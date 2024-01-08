import React from 'react';

import { Box, List, Drawer, ListItem, ListItemIcon, ListItemText, Divider } from '@mui/material';
import { SxProps } from '@mui/system';

import EditIcon from '@mui/icons-material/Edit';
import DoubleArrowRoundedIcon from '@mui/icons-material/DoubleArrowRounded';
import CompareArrowsRoundedIcon from '@mui/icons-material/CompareArrowsRounded';
import FileDownloadDoneIcon from '@mui/icons-material/FileDownloadDone';
import UploadIcon from '@mui/icons-material/Upload';

import { FormattedMessage, useIntl } from 'react-intl';

import Burger from 'components-burger';
import { Client, Composer } from '../context';
import { CellEdit, NameDescHitPolicyEdit, UploadCSV, OrderEdit, HeaderEdit } from './editors';
import fileDownload from 'js-file-download'


import Decision from './table';
import { blueberry_whip, sambucus } from 'components-colors';


interface EditMode {
  cell?: Client.AstDecisionCell,
  header?: Client.TypeDef,
  meta?: boolean,
  upload?: boolean,
  rowsColumns?: boolean,
  options?: boolean
}

const saveCsv = (decision: Client.AstDecision) => {
  const accepts: Client.TypeDef[] = [...decision.headers.acceptDefs].sort((a, b) => a.order - b.order);
  const returns: Client.TypeDef[] = [...decision.headers.returnDefs].sort((a, b) => a.order - b.order);
  const rows = decision.rows.sort((a, b) => a.order - b.order);
  const headers: Client.TypeDef[] = [...accepts, ...returns];

  const line0 = headers.map(h => h.name).join(";");
  const lines = rows.map(row => {
    const cells: Record<string, Client.AstDecisionCell> = {};
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
  const itemSx: SxProps = { color: blueberry_whip }
  return (<ListItem button onClick={onClick}>
    <ListItemIcon sx={itemSx}>{icon}</ListItemIcon>
    <ListItemText sx={itemSx}>
      <Box component="span" sx={itemSx}>
        <FormattedMessage id={label} />
      </Box>
    </ListItemText>
  </ListItem>);
}


const DrawerSection: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  return <>
    <Box sx={{ width: "350px" }}><List>{children}</List></Box>
    <Divider orientation="vertical" flexItem color="explorerItem.contrastColor" />
  </>
}

const DecisionEdit: React.FC<{ decision: Client.Entity<Client.AstDecision> }> = ({ decision }) => {
  const { service, actions, session } = Composer.useComposer();
  const update = session.pages[decision.id];

  const commands = React.useMemo(() => update ? update.value : decision.source.commands, [decision, update]);
  const [ast, setAst] = React.useState<Client.AstDecision | undefined>();
  const [edit, setEdit] = React.useState<EditMode | undefined>();
  const intl = useIntl(); 


  const onChange = (newCommands: Client.AstCommand[]) => {
    actions.handlePageUpdate(decision.id, [...commands, ...newCommands])
  }

  const decisionId = decision.id;

  React.useEffect(() => {
    service.ast(decisionId, commands).then(data => {
      console.log("new commands applied");
      setAst(data.ast);
    });

  }, [commands, decisionId, service])

  if (!ast) {
    return <span>loading ...</span>
  }


  return (<Box sx={{ width: '100%', overflow: 'hidden', padding: 1 }}>
    {edit?.meta ? <NameDescHitPolicyEdit decision={ast} onChange={onChange} onClose={() => setEdit(undefined)} /> : null}
    {edit?.rowsColumns ? <OrderEdit decision={ast} onChange={onChange} onClose={() => setEdit(undefined)} /> : null}
    {edit?.upload ? <UploadCSV onChange={onChange} onClose={() => setEdit(undefined)} /> : null}
    {edit?.cell ? <CellEdit dt={ast} cell={edit?.cell} onClose={() => setEdit(undefined)} onChange={(command) => onChange([command])} /> : null}
    {edit?.header ? <HeaderEdit dt={ast} header={edit.header} onChange={onChange} onClose={() => setEdit(undefined)} /> : null}

    <Drawer anchor="top" open={edit?.options} onClose={() => setEdit(undefined)} sx={{ zIndex: "10000" }}>
      <Box sx={{ display: "flex", backgroundColor: sambucus, color: "primary.contrastText" }}>
        <DrawerSection>
          <DrawerOption label='decisions.toolbar.addInputColumn' icon={<DoubleArrowRoundedIcon sx={{ transform: "rotate(-180deg)" }} />} onClick={() => onChange([{ type: 'ADD_HEADER_IN', id: "in-" + ast.headers.acceptDefs.length + 1 }])} />
          <DrawerOption label='decisions.toolbar.addOutputColumn' icon={<DoubleArrowRoundedIcon />} onClick={() => onChange([{ type: 'ADD_HEADER_OUT', id: "out-" + ast.headers.returnDefs.length + 1 }])} />
          <DrawerOption label='decisions.toolbar.addRow' icon={<DoubleArrowRoundedIcon sx={{ transform: "rotate(90deg)" }} />} onClick={() => onChange([{ type: 'ADD_ROW', id: "" }])} />
        </DrawerSection>
        <DrawerSection>
          <DrawerOption label='decisions.toolbar.csvDownload' icon={<FileDownloadDoneIcon />} onClick={() => saveCsv(ast)} />
          <DrawerOption label='decisions.toolbar.csvUpload' icon={<UploadIcon />} onClick={() => setEdit({ upload: true })} />
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
          <Burger.SecondaryButton label={`${ast.name} - ${intl.formatMessage({id: "decisions.table.hitpolicy"})}: ${ast.hitPolicy}`} onClick={() => setEdit({ options: true })} />
          <Burger.SecondaryButton label="decisions.toolbar.addInputColumn" onClick={() => onChange([{ type: 'ADD_HEADER_IN', id: "in-" + ast.headers.acceptDefs.length + 1 }])} />
          <Burger.SecondaryButton label="decisions.toolbar.addOutputColumn" onClick={() => onChange([{ type: 'ADD_HEADER_OUT', id: "out-" + ast.headers.returnDefs.length + 1 }])} />
          <Burger.SecondaryButton label="decisions.toolbar.addRow" onClick={() => onChange([{ type: 'ADD_ROW', id: "" }])} />
          <Burger.SecondaryButton label="decisions.toolbar.organize.rows.columns" onClick={() => setEdit({ rowsColumns: true })} />
        </Decision.Header>
      )}
      renderRow={rowProps => <Decision.Row {...rowProps} />}
      renderCell={cellProps => <Decision.Cell {...cellProps} onClick={() => setEdit({ cell: cellProps.cell })} />}
    />
  </Box >);
}

export type { };
export { DecisionEdit };
