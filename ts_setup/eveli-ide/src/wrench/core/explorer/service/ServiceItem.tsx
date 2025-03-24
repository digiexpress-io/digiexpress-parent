import React from "react";
import { Box, Typography, useTheme } from "@mui/material";
import CodeOutlinedIcon from '@mui/icons-material/CodeOutlined';

import ConstructionIcon from '@mui/icons-material/Construction';
import AccountTreeOutlinedIcon from '@mui/icons-material/AccountTreeOutlined';
import ArticleOutlinedIcon from '@mui/icons-material/ArticleOutlined';
import PriorityHighIcon from '@mui/icons-material/PriorityHigh';
import LowPriorityIcon from '@mui/icons-material/LowPriority';

import { FormattedMessage } from 'react-intl';

import * as Burger from '@/burger';

import MsgTreeItem from '../MsgTreeItem';
import { Composer } from '../../context';
import { HdesApi } from '@/burger';
import ServiceOptions from './ServiceOptions';
import { useWrenchNav } from '../../nav';


const ErrorItem: React.FC<{
  msg:HdesApi.ProgramMessage;
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
  msg:HdesApi.ProgramMessage;
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
  onClick: () => void;
}) {
  return (
    <Burger.TreeItemRoot
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


function FlowItem(props: {
  labelText: string;
  nodeId: string;
  children?: React.ReactChild;
  onClick: () => void;
}) {
  return (
    <Burger.TreeItemRoot
      itemId={props.nodeId}
      onClick={props.onClick}
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
  entity?:HdesApi.Entity<HdesApi.AstDecision>;
  ref:HdesApi.ProgramAssociation;
}
interface RefFlow {
  entity?:HdesApi.Entity<HdesApi.AstFlow>;
  ref:HdesApi.ProgramAssociation;
}

const ServiceItem: React.FC<{ serviceId:HdesApi.ServiceId }> = ({ serviceId }) => {
  const theme = useTheme();
  const { session, isArticleSaved } = Composer.useComposer();
  const { onNav } = useWrenchNav();

  const service = session.site.services[serviceId];

  const saved = isArticleSaved(service);
  const serviceName = service.ast ? service.ast.name : service.id;

  
  const decisions: RefDecision[] = service.associations
    .filter(a => a.refType === "DT")
    .map(a => ({ entity: session.getDecision(a.ref), ref: a }));
  const flows: RefFlow[] = service.associations
    .filter(a => a.owner && a.refType === "FLOW")
    .map(a => ({ entity: session.getFlow(a.ref), ref: a }));

  
  return (
    <Burger.TreeItem itemId={service.id} labelText={serviceName}
      labelIcon={CodeOutlinedIcon}
      labelInfo={service.status === "UP" ? undefined : <ConstructionIcon color='error' />}
      labelcolor={saved ? "explorerItem" : "secondary.light"}
    >

      {/** Service options */}
      <Burger.TreeItem itemId={service.id + 'options-nested'}
        labelText={<FormattedMessage id="options" />}
      >
        <ServiceOptions service={service} />
      </Burger.TreeItem>

      {/** Service status */}
      <Burger.TreeItem itemId={service.id + 'status-nested'}
        labelText={<FormattedMessage id={`program.status.${service.status}`} />}
        labelInfo={`${service.errors.length + service.warnings.length}`}
        labelcolor={theme.palette.primary.dark}
      >

        {service.errors.map((view, index) => (<ErrorItem key={index} msg={view} nodeId={`${view.id}-error-${index}`} />))}
        {service.warnings.map((view, index) => (<WarningItem key={index} msg={view} nodeId={`${view.id}-warning-${index}`} />))}
      </Burger.TreeItem>


      {/** Flow options */}
      <Burger.TreeItem itemId={service.id + 'flows-nested'}
        labelText={<FormattedMessage id="flows" />}
        labelInfo={`${flows.length}`}
        labelcolor="primary">

        {flows.map(view => (<FlowItem key={view.ref.ref} nodeId={`${service.id}-fl-${view.ref.ref}`}
          labelText={view.ref.ref}
          onClick={() => view.entity ? onNav({ type: 'ENTITY_EDITOR', id: view.entity.id }) : undefined}
        />)
        )}
      </Burger.TreeItem>

      {/** Internal decision options */}
      <Burger.TreeItem itemId={service.id + 'internal-decisions-nested'}
        labelText={<FormattedMessage id="internal-decisions" />}
        labelInfo={`${decisions.length}`}
        labelcolor="page">

        {decisions.map(view => (<DecisionItem key={view.ref.ref} nodeId={`${service.id}-dt-${view.ref.ref}`}
          labelText={view.ref.ref}
          onClick={() => view.entity ? onNav({ type: 'DECISIONS' }) : undefined}
        />))}

      </Burger.TreeItem>
    </Burger.TreeItem>)
}
export default ServiceItem;
