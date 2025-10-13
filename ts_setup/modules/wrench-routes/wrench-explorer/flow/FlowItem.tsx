import React from "react";
import { Box, Typography, useTheme } from "@mui/material";

import { AccountTreeOutlined as AccountTreeOutlinedIcon } from '@mui/icons-material';
import { ArticleOutlined as ArticleOutlinedIcon } from '@mui/icons-material';
import { Link as LinkIcon } from '@mui/icons-material';
import { Construction as ConstructionIcon } from '@mui/icons-material';
import { PriorityHigh as PriorityHighIcon } from '@mui/icons-material';
import { LowPriority as LowPriorityIcon } from '@mui/icons-material';

import { FormattedMessage } from 'react-intl';

import {EveliPermissions, TreeItemRoot, TreeItem} from '@dxs-ts/eveli-primitives';

import { HdesApi, WrenchComposerApi as Composer } from '@dxs-ts/wrench-api';
import FlowOptions from './FlowOptions';
import MsgTreeItem from '../MsgTreeItem';
import { useWrenchNav } from '../../wrench-nav';



function DecisionItem(props: {
  labelText: string;
  nodeId: string;
  children?: React.ReactChild;
  onClick: () => void;
}) {
  return (
    <TreeItemRoot
      itemId={props.nodeId}
      onClick={props.onClick}
      label={
        <Box sx={{ display: "flex", alignItems: "center", p: 0.5, pr: 0 }}>
          <Box component={AccountTreeOutlinedIcon} color="secondary.contrastText" sx={{ pl: 1, mr: 1 }} />
          <Typography noWrap={true} maxWidth="300px" variant="body2"
            sx={{ fontWeight: "inherit", flexGrow: 1 }}
          >
            {props.labelText}
          </Typography>
        </Box>
      }
    />
  );
}


const ServiceItem: React.FC<{
  labelText: string;
  nodeId: string;
  children?: React.ReactChild;
  onClick: () => void;
}> = (props) => {
  const theme = useTheme();
  return (
    <TreeItemRoot
      itemId={props.nodeId}
      onClick={props.onClick}
      label={
        <Box sx={{ display: "flex", alignItems: "center", p: 0.5, pr: 0 }}>
          <Box component={LinkIcon} color={theme.palette.primary.light} sx={{ pl: 1, mr: 1 }} />
          <Typography align="left" maxWidth="300px" noWrap={true} variant="body2"
            sx={{ fontWeight: "inherit", flexGrow: 1 }}
          >
            {props.labelText}
          </Typography>
        </Box>
      }
    />
  );
}

const ErrorItem: React.FC<{
  msg: HdesApi.ProgramMessage;
  nodeId: string;
}> = (props) => {
  return (
    <MsgTreeItem error msg={props.msg} nodeId={props.nodeId}>
      <Box sx={{ display: "flex", alignItems: "center", p: 0.5, pr: 0 }}>
        <Box component={PriorityHighIcon} color="error.main" sx={{ pl: 1, mr: 1 }} />
        <Typography align="left" maxWidth="300px" sx={{ fontWeight: "inherit", flexGrow: 1 }} noWrap>
          <b>{props.msg.id}</b><br />
          {props.msg.msg}
        </Typography>
      </Box>
    </MsgTreeItem>
  );
}

const WarningItem: React.FC<{
  msg: HdesApi.ProgramMessage;
  nodeId: string;
}> = (props) => {
  return (
    <MsgTreeItem error msg={props.msg} nodeId={props.nodeId}>
      <Box sx={{ display: "flex", alignItems: "center", p: 0.5, pr: 0 }}>
        <Box component={LowPriorityIcon} color="warning.main" sx={{ pl: 1, mr: 1 }} />
        <Typography align="left" maxWidth="300px" sx={{ fontWeight: "inherit", flexGrow: 1 }} noWrap>
          <b>{props.msg.id}</b><br />
          {props.msg.msg}
        </Typography>
      </Box>
    </MsgTreeItem>
  );
}



interface RefDecision {
  entity?: HdesApi.Entity<HdesApi.AstDecision>;
  ref: HdesApi.ProgramAssociation;
}
interface RefService {
  entity?: HdesApi.Entity<HdesApi.AstService>;
  ref: HdesApi.ProgramAssociation;
}

const FlowItem: React.FC<{ flowId: HdesApi.FlowId }> = ({ flowId }) => {
  const theme = useTheme();
  const { session, isArticleSaved } = Composer.useComposer();
  const { onNav } = useWrenchNav();

  const flow = session.site.flows[flowId];

  const saved = isArticleSaved(flow);
  const flowName = flow.ast ? flow.ast.name : flow.id;

  const decisions: RefDecision[] = flow.associations
    .filter(a => a.refType === "DT")
    .map(a => ({ entity: session.getDecision(a.ref), ref: a }));
  const services: RefService[] = flow.associations
    .filter(a => a.owner && a.refType === "FLOW_TASK")
    .map(a => ({ entity: session.getService(a.ref), ref: a }));


  return (
    <TreeItem itemId={flow.id}
      labelText={flowName}
      labelIcon={ArticleOutlinedIcon}
      labelcolor={saved ? "explorerItem" : "secondary.light"}
      labelInfo={flow.status === "UP" ? undefined : <ConstructionIcon color="error" />}>

      {/** Flow options */}
      <EveliPermissions id='EDIT_WRENCH_ASSET'>
        <TreeItem itemId={flow.id + 'options-nested'}
          labelText={<FormattedMessage id="options" />}>
          <FlowOptions flow={flow} />
        </TreeItem>
      </EveliPermissions>

      {/** Flow status */}
      <TreeItem itemId={flow.id + 'status-nested'}
        labelText={(() => {
          switch (flow.status) {
            case 'DEPENDENCY_ERROR':
              return <FormattedMessage id="program.status.DEPENDENCY_ERROR" />;
            case 'UP':
              return <FormattedMessage id="program.status.UP" />;
            case 'AST_ERROR':
              return <FormattedMessage id="program.status.AST_ERROR" />;
            case 'PROGRAM_ERROR':
              return <FormattedMessage id="program.status.PROGRAM_ERROR" />;
            default:
              return flow.status;
          }
        })()}
        labelInfo={`${flow.errors.length + flow.warnings.length}`}
        labelcolor={theme.palette.primary.dark}>

        {flow.errors.map((view, index) => (<ErrorItem key={index} msg={view} nodeId={`${view.id}-error-${index}`} />))}
        {flow.warnings.map((view, index) => (<WarningItem key={index} msg={view} nodeId={`${view.id}-warning-${index}`} />))}
      </TreeItem>

      {/** Decision options */}
      <TreeItem itemId={flow.id + 'decisions-nested'}
        labelText={<FormattedMessage id="decisions" />}
        labelInfo={`${decisions.length}`}
        labelcolor="page">

        {decisions.map(view => (<DecisionItem key={view.ref.ref} nodeId={`${flow.id}-dt-${view.ref.ref}`}
          labelText={view.ref.ref}
          onClick={() => view.entity ? onNav({ type: 'ENTITY_EDITOR', id: view.entity.id }) : undefined}
        />))}
      </TreeItem>


      {/** Service options */}
      <TreeItem itemId={flow.id + 'services-nested'}
        labelText={<FormattedMessage id="services" />}
        labelInfo={`${services.length}`}
        labelcolor={theme.palette.primary.light}>

        {services.map(view => (<ServiceItem key={view.ref.ref} nodeId={`${flow.id}-st-${view.ref.ref}`}
          labelText={view.ref.ref}
          onClick={() => view.entity ? onNav({ type: 'ENTITY_EDITOR', id: view.entity.id }) : undefined}
        />)
        )}
      </TreeItem>
    </TreeItem>)
}
export default FlowItem;
