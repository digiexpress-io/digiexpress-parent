import React from "react";
import { Box, Typography } from "@mui/material";

import FolderOutlinedIcon from '@mui/icons-material/FolderOutlined';
import ConstructionIcon from '@mui/icons-material/Construction';
import AccountTreeOutlinedIcon from '@mui/icons-material/AccountTreeOutlined';
import ArticleOutlinedIcon from '@mui/icons-material/ArticleOutlined';
import EditIcon from '@mui/icons-material/ModeEdit';
import PriorityHighIcon from '@mui/icons-material/PriorityHigh';
import LowPriorityIcon from '@mui/icons-material/LowPriority';

import { FormattedMessage } from 'react-intl';

import Burger from 'components-burger';

import MsgTreeItem from '../MsgTreeItem';
import { Composer, Client } from '../../context';
import ServiceOptions from './ServiceOptions';


const ErrorItem: React.FC<{
  msg: Client.ProgramMessage;
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
  msg: Client.ProgramMessage;
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
      nodeId={props.nodeId}
      onClick={props.onClick}
      label={
        <Box sx={{ display: "flex", alignItems: "center", p: 0.5, pr: 0 }}>
          <Box component={AccountTreeOutlinedIcon} color="page.main" sx={{ pl: 1, mr: 1 }} />
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
      nodeId={props.nodeId}
      onClick={props.onClick}
      label={
        <Box sx={{ display: "flex", alignItems: "center", p: 0.5, pr: 0 }}>
          <Box component={AccountTreeOutlinedIcon} color="article.main" sx={{ pl: 1, mr: 1 }} />
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
  entity?: Client.Entity<Client.AstDecision>;
  ref: Client.ProgramAssociation;
}
interface RefFlow {
  entity?: Client.Entity<Client.AstFlow>;
  ref: Client.ProgramAssociation;
}

const ServiceItem: React.FC<{ serviceId: Client.ServiceId }> = ({ serviceId }) => {

  const { session, isArticleSaved } = Composer.useComposer();
  const nav = Composer.useNav();


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
    <Burger.TreeItem nodeId={service.id} labelText={serviceName}
      labelIcon={ArticleOutlinedIcon}
      labelInfo={service.status === "UP" ? undefined : <ConstructionIcon color="error" />}
      labelcolor={saved ? "explorerItem" : "explorerItem.contrastText"}>

      {/** Service options */}
      <Burger.TreeItem nodeId={service.id + 'options-nested'}
        labelText={<FormattedMessage id="options" />}
        labelIcon={EditIcon}>
        <ServiceOptions service={service} />
      </Burger.TreeItem>

      {/** Service status */}
      <Burger.TreeItem nodeId={service.id + 'status-nested'}
        labelText={<FormattedMessage id={`program.status.${service.status}`} />}
        labelIcon={FolderOutlinedIcon}
        labelInfo={`${service.errors.length + service.warnings.length}`}
        labelcolor="workflow">

        {service.errors.map((view, index) => (<ErrorItem key={index} msg={view} nodeId={`${view.id}-error-${index}`} />))}
        {service.warnings.map((view, index) => (<WarningItem key={index} msg={view} nodeId={`${view.id}-warning-${index}`} />))}
      </Burger.TreeItem>


      {/** Flow options */}
      <Burger.TreeItem nodeId={service.id + 'flows-nested'}
        labelText={<FormattedMessage id="flows" />}
        labelIcon={FolderOutlinedIcon}
        labelInfo={`${flows.length}`}
        labelcolor="article">

        {flows.map(view => (<FlowItem key={view.ref.ref} nodeId={`${service.id}-fl-${view.ref.ref}`}
          labelText={view.ref.ref}
          onClick={() => view.entity ? nav.handleInTab({ article: view.entity }) : undefined}
        />)
        )}
      </Burger.TreeItem>

      {/** Internal decision options */}
      <Burger.TreeItem nodeId={service.id + 'internal-decisions-nested'}
        labelText={<FormattedMessage id="internal-decisions" />}
        labelIcon={FolderOutlinedIcon}
        labelInfo={`${decisions.length}`}
        labelcolor="page">

        {decisions.map(view => (<DecisionItem key={view.ref.ref} nodeId={`${service.id}-dt-${view.ref.ref}`}
          labelText={view.ref.ref}
          onClick={() => view.entity ? nav.handleInTab({ article: view.entity }) : undefined}
        />))}

      </Burger.TreeItem>
    </Burger.TreeItem>)
}
export default ServiceItem;
