import React from 'react';
import { Chip, Box, Typography } from '@mui/material';
import { useIntl } from 'react-intl';

import DeClient from '@declient';

import { useTable } from './descriptor-table-ctx';
import Info from './Info/CellInfoPopper';
import CellInfoWorkflow from './Info/CellInfoWorkflow';
import CellInfoFlow from './Info/CellInfoFlow';
import CellInfoDialob from './Info/CellInfoDialob';

interface CellProps {
  row: DeClient.ServiceDescriptor,
  assocs: DeClient.DefStateAssocs,
  def: DeClient.DefinitionState,
  width: string,
}

const NameAndTag: React.FC<{ id: string, name?: string, tag?: string, width: string, info?: React.ReactNode }> = ({ id, name, tag, width, info }) => {
  const { setState } = useTable();
  const handleClick = React.useCallback((event: React.MouseEvent<HTMLElement>) => {
    setState(prev => prev.withPopperOpen(id, !prev.popperOpen, event.currentTarget))
  }, [setState, id]);
  
  if (!name) {
    return <>-</>
  }

  return (<Box display='flex'>
    <Info id={id} content={info}/>
    {tag && <Box sx={{ mr: 0, minWidth: '50px', alignSelf: "center" }}>
      <Chip label={tag} color="primary" variant="outlined" size="small" onClick={handleClick}/>
    </Box>}
    <Box alignSelf="center"><Typography noWrap={true} fontSize="13px" fontWeight="400" width={width}>{name}</Typography></Box>
  </Box>);
}

const WorkflowName: React.FC<CellProps> = ({ row, def, assocs, width }) => {
  const intl = useIntl();
  const workflow = assocs.getWorkflow(row.name);
  const stencil = def.definition.refs.find(ref => ref.type === 'STENCIL')?.tagName;
  const id = row.id + "/wk";
    
  if (!workflow) {
    return <NameAndTag id={id} tag={stencil} width={width} />
  }
  const locales = workflow.locales.length;
  const pages = workflow.values.length;

  const name = intl.formatMessage({ id: "descriptorTable.row.articles" }, { locales, pages });
  const info = (<CellInfoWorkflow row={row} def={def} assocs={assocs} id={id} />);
  return <NameAndTag id={id} name={name} tag={stencil} width={width} info={info} />
}

const FlowName: React.FC<CellProps> = ({ row, def, assocs, width }) => {
  const flow = assocs.getFlow(row.flowId);
  const hdes = def.definition.refs.find(ref => ref.type === 'HDES')?.tagName;
  const id = row.id + "/fl";
  const info = (<CellInfoFlow row={row} def={def} assocs={assocs} id={id} />);
  return (<NameAndTag id={id} width={width} name={flow?.flow?.ast?.name} tag={hdes} info={info} />);
}

const DialobName: React.FC<CellProps> = ({ row, width, assocs, def }) => {
  const dialob = assocs.getDialob(row.formId);
  const id = row.id + "/dl";
  const info = (<CellInfoDialob row={row} def={def} assocs={assocs} id={id} />);

  return (<NameAndTag id={id} width={width} name={dialob?.rev.name} tag={dialob?.entry.revisionName} info={info} />);
}

const DescriptorName: React.FC<CellProps> = ({ row, width }) => {
  return (<NameAndTag id={row.id + "/name"} width={width} name={row.name} />);
}

export type { CellProps }
export { WorkflowName, DialobName, FlowName, DescriptorName };

