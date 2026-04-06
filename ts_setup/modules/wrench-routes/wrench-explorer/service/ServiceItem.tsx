import React from "react";
import { Box, Typography, useTheme } from "@mui/material";
import { CodeOutlined as CodeOutlinedIcon } from '@mui/icons-material';

import { DangerousOutlined as DangerousOutlinedIcon } from '@mui/icons-material';
import { AccountTreeOutlined as AccountTreeOutlinedIcon } from '@mui/icons-material';
import { PriorityHigh as PriorityHighIcon } from '@mui/icons-material';
import { LowPriority as LowPriorityIcon } from '@mui/icons-material';

import { FormattedMessage } from 'react-intl';

import { HdesApi } from '@dxs-ts/wrench-api';
import { EveliPermissions, TreeItemRoot, TreeItem } from "@dxs-ts/eveli-primitives";
import { WrenchComposerApi as Composer } from "@dxs-ts/wrench-api";

import ServiceOptions from './ServiceOptions';
import MsgTreeItem from '../MsgTreeItem';
import { useWrenchNav } from '../../wrench-nav';
import InfoTreeItem from '../InfoTreeItem';




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

function DecisionItem(props: {
  labelText: string;
  nodeId: string;
  children?: React.ReactChild;
  onClick?: () => void;
}) {
  const blockInteractionCapture: React.MouseEventHandler = (e) => {
    e.preventDefault();
    e.stopPropagation();
  };

  return (
    <TreeItemRoot
      itemId={props.nodeId}
      onClick={props.onClick}
      onMouseDownCapture={!props.onClick ? blockInteractionCapture : undefined}
      onClickCapture={!props.onClick ? blockInteractionCapture : undefined}
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


function FlowItem(props: {
  labelText: string;
  nodeId: string;
  children?: React.ReactChild;
  onClick?: () => void;
}) {
  const blockInteractionCapture: React.MouseEventHandler = (e) => {
    e.preventDefault();
    e.stopPropagation();
  };

  return (
    <TreeItemRoot
      itemId={props.nodeId}
      onClick={props.onClick}
      onMouseDownCapture={!props.onClick ? blockInteractionCapture : undefined}
      onClickCapture={!props.onClick ? blockInteractionCapture : undefined}
      label={
        <Box sx={{ display: "flex", alignItems: "center", p: 0.5, pr: 0 }}>
          <Box component={AccountTreeOutlinedIcon} color="primary.main" sx={{ pl: 1, mr: 1 }} />
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

interface RefDecision {
  entity?: HdesApi.Entity<HdesApi.AstDecision>;
  ref: HdesApi.ProgramAssociation;
}
interface RefFlow {
  entity?: HdesApi.Entity<HdesApi.AstFlow>;
  ref: HdesApi.ProgramAssociation;
}

const ServiceItem: React.FC<{ serviceId: HdesApi.ServiceId }> = ({ serviceId }) => {
  const theme = useTheme();
  const { session, isArticleSaved } = Composer.useComposer();
  const { onNav } = useWrenchNav();

  const service = session.site.services[serviceId];

  const saved = isArticleSaved(service);
  const serviceName = service.ast ? service.ast.name : service.id;
  const lastUpdated = session.getLastUpdated(serviceId);

  const decisions: RefDecision[] = service.associations
    .filter(a => a.refType === "DECISION_TABLE")
    .map(a => ({ entity: session.getDecision(a.ref), ref: a }));
  const flows: RefFlow[] = service.associations
    .filter(a => a.refType === "FLOW")
    .map(a => ({ entity: session.getFlow(a.ref), ref: a }));

  return (
    <TreeItem itemId={service.id} labelText={serviceName}
      labelIcon={CodeOutlinedIcon}
      labelInfo={service.status === "UP" ? undefined : <DangerousOutlinedIcon color='error' fontSize='small' />}
      labelcolor={saved ? "explorerItem" : "secondary.light"}
    >

      {/** Service options */}
      <EveliPermissions id='EDIT_WRENCH_ASSET'>
        <TreeItem itemId={service.id + 'options-nested'}
          labelText={<FormattedMessage id="options" />}
        >
          <ServiceOptions service={service} />
        </TreeItem>
      </EveliPermissions>

      {/** Service info */}
      {lastUpdated && <InfoTreeItem nodeId={service.id} lastUpdated={lastUpdated} />}

      {/** Service status */}
      <TreeItem itemId={service.id + 'status-nested'}
        labelText={(() => {
          switch (service.status) {
            case 'DEPENDENCY_ERROR':
              return <FormattedMessage id="program.status.DEPENDENCY_ERROR" />;
            case 'UP':
              return <FormattedMessage id="program.status.UP" />;
            case 'AST_ERROR':
              return <FormattedMessage id="program.status.AST_ERROR" />;
            case 'PROGRAM_ERROR':
              return <FormattedMessage id="program.status.PROGRAM_ERROR" />;
            default:
              return service.status;
          }
        })()}
        labelInfo={`${service.errors.length}`}
        labelcolor={theme.palette.primary.dark}
        interactive={(service.errors.length) > 0}
      >

        {service.errors.map((view, index) => (<ErrorItem key={index} msg={view} nodeId={`${view.id}-error-${index}`} />))}
      </TreeItem>


      {/** Flow options */}
      <TreeItem itemId={service.id + 'flows-nested'}
        labelText={<FormattedMessage id="flows" />}
        labelInfo={`${flows.length}`}
        labelcolor="primary"
        interactive={flows.length > 0}
      >

        {flows.map(view => {
          const entity = view.entity;
          return (
            <FlowItem
              key={view.ref.ref}
              nodeId={`${service.id}-fl-${view.ref.ref}`}
              labelText={view.ref.ref}
              onClick={entity ? () => onNav({ type: 'ENTITY_EDITOR', id: entity.id }) : undefined}
            />
          );
        })}
      </TreeItem>

      {/** Internal decision options */}
      <TreeItem itemId={service.id + 'internal-decisions-nested'}
        labelText={<FormattedMessage id="internal-decisions" />}
        labelInfo={`${decisions.length}`}
        labelcolor="page"
        interactive={decisions.length > 0}
      >

        {decisions.map(view => (<DecisionItem key={view.ref.ref} nodeId={`${service.id}-dt-${view.ref.ref}`}
          labelText={view.ref.ref}
          onClick={view.entity ? () => onNav({ type: 'DECISIONS' }) : undefined}
        />))}

      </TreeItem>
    </TreeItem>)
}
export default ServiceItem;
