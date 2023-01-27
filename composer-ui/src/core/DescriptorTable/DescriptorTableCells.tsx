import React from 'react';
import { Chip, Box, Typography } from '@mui/material';
import { useIntl } from 'react-intl';

import DeClient from '@declient';

import Info from './CellInfoPopper';
import CellInfoWorkflow from './CellInfoWorkflow';
import CellInfoFlow from './CellInfoFlow';
import CellInfoDialob from './CellInfoDialob';

interface CellProps {
  row: DeClient.ServiceDescriptor,
  assocs: DeClient.DefStateAssocs,
  def: DeClient.DefinitionState,
  width: string,
}

const NameAndTag: React.FC<{ name?: string, tag?: string, width: string, info?: React.ReactNode }> = ({ name, tag, width, info }) => {
  if (!name) {
    return <>-</>
  }
  return (<Box display='flex'>
    {tag && <Box sx={{ mr: 1, minWidth: '50px', alignSelf: "center" }}><Chip label={tag} color="primary" variant="outlined" size="small" /></Box>}
    {info ? <Box sx={{ mr: 1 }}>{info}</Box> : null}
    <Box alignSelf="center"><Typography noWrap={true} fontSize="13px" fontWeight="400" width={width}>{name}</Typography></Box>
  </Box>);
}

const WorkflowName: React.FC<CellProps> = ({ row, def, assocs, width }) => {

  const intl = useIntl();
  const workflow = assocs.getWorkflow(row.name);
  const stencil = def.definition.refs.find(ref => ref.type === 'STENCIL')?.tagName;
  if (!workflow) {
    return <NameAndTag tag={stencil} width={width} />
  }
  const locales = workflow.locales.length;
  const pages = workflow.values.length;
  const id = row.id + "/wk";
  const name = intl.formatMessage({ id: "descriptorTable.row.articles" }, { locales, pages });
  const info = (<CellInfoWorkflow row={row} def={def} assocs={assocs} id={id} />);
  return <NameAndTag name={name} tag={stencil} width={width} info={<Info content={info} id={id} />} />
}

const FlowName: React.FC<CellProps> = ({ row, def, assocs, width }) => {
  const flow = assocs.getFlow(row.flowId);
  const hdes = def.definition.refs.find(ref => ref.type === 'HDES')?.tagName;
  const id = row.id + "/fl";
  const info = (<CellInfoFlow row={row} def={def} assocs={assocs} id={id} />);
  return (<NameAndTag width={width} name={flow?.flow?.ast?.name} tag={hdes} info={<Info content={info} id={id} />} />);
}

const DialobName: React.FC<CellProps> = ({ row, width, assocs, def }) => {
  const dialob = assocs.getDialob(row.formId);
  const id = row.id + "/dl";
  const info = (<CellInfoDialob row={row} def={def} assocs={assocs} id={id} />);
  return (<NameAndTag width={width} name={dialob?.rev.name} tag={dialob?.entry.revisionName} info={<Info content={info} id={id} />} />);
}


const DescriptorName: React.FC<CellProps> = ({ row, width }) => {
  return <NameAndTag width={width} name={row.name} />;
}

export type { CellProps }
export { WorkflowName, DialobName, FlowName, DescriptorName };

