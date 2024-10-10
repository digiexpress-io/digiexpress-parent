import React from "react";
import { Box, Typography } from "@mui/material";

import FolderOutlinedIcon from '@mui/icons-material/FolderOutlined';
import AccountTreeOutlinedIcon from '@mui/icons-material/AccountTreeOutlined';
import ArticleOutlinedIcon from '@mui/icons-material/ArticleOutlined';
import LinkIcon from '@mui/icons-material/Link';
import EditIcon from '@mui/icons-material/ModeEdit';
import ConstructionIcon from '@mui/icons-material/Construction';
import PriorityHighIcon from '@mui/icons-material/PriorityHigh';
import LowPriorityIcon from '@mui/icons-material/LowPriority';

import { FormattedMessage } from 'react-intl';


import Burger from '@the-wrench-io/react-burger';

import { Composer, Client } from '../../context';
import FlowOptions from './FlowOptions';
import MsgTreeItem from '../MsgTreeItem';



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


const ServiceItem: React.FC<{
  labelText: string;
  nodeId: string;
  children?: React.ReactChild;
  onClick: () => void;
}> = (props) => {
  return (
    <Burger.TreeItemRoot
      nodeId={props.nodeId}
      onClick={props.onClick}
      label={
        <Box sx={{ display: "flex", alignItems: "center", p: 0.5, pr: 0 }}>
          <Box component={LinkIcon} color="link.main" sx={{ pl: 1, mr: 1 }} />
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



interface RefDecision {
  entity?: Client.Entity<Client.AstDecision>;
  ref: Client.ProgramAssociation;
}
interface RefService {
  entity?: Client.Entity<Client.AstService>;
  ref: Client.ProgramAssociation;
}

const FlowItem: React.FC<{ flowId: Client.FlowId }> = ({ flowId }) => {

  const { session, isArticleSaved } = Composer.useComposer();
  const nav = Composer.useNav();

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
    <Burger.TreeItem nodeId={flow.id}
      labelText={flowName}
      labelIcon={ArticleOutlinedIcon}
      labelcolor={saved ? "explorerItem" : "explorerItem.contrastText"}
      labelInfo={flow.status === "UP" ? undefined : <ConstructionIcon color="error" />}>

      {/** Flow options */}
      <Burger.TreeItem nodeId={flow.id + 'options-nested'}
        labelText={<FormattedMessage id="options" />}
        labelIcon={EditIcon} >
        <FlowOptions flow={flow} />
      </Burger.TreeItem>

      {/** Flow status */}
      <Burger.TreeItem nodeId={flow.id + 'status-nested'}
        labelText={<FormattedMessage id={`program.status.${flow.status}`} />}
        labelIcon={FolderOutlinedIcon}
        labelInfo={`${flow.errors.length + flow.warnings.length}`}
        labelcolor="workflow">

        {flow.errors.map((view, index) => (<ErrorItem key={index} msg={view} nodeId={`${view.id}-error-${index}`} />))}
        {flow.warnings.map((view, index) => (<WarningItem key={index} msg={view} nodeId={`${view.id}-warning-${index}`} />))}
      </Burger.TreeItem>

      {/** Decision options */}
      <Burger.TreeItem nodeId={flow.id + 'decisions-nested'}
        labelText={<FormattedMessage id="decisions" />}
        labelIcon={FolderOutlinedIcon}
        labelInfo={`${decisions.length}`}
        labelcolor="page">

        {decisions.map(view => (<DecisionItem key={view.ref.ref} nodeId={`${flow.id}-dt-${view.ref.ref}`}
          labelText={view.ref.ref}
          onClick={() => view.entity ? nav.handleInTab({ article: view.entity }) : undefined}
        />))}
      </Burger.TreeItem>


      {/** Service options */}
      <Burger.TreeItem nodeId={flow.id + 'services-nested'}
        labelText={<FormattedMessage id="services" />}
        labelIcon={FolderOutlinedIcon}
        labelInfo={`${services.length}`}
        labelcolor="link">

        {services.map(view => (<ServiceItem key={view.ref.ref} nodeId={`${flow.id}-st-${view.ref.ref}`}
          labelText={view.ref.ref}
          onClick={() => view.entity ? nav.handleInTab({ article: view.entity }) : undefined}
        />)
        )}
      </Burger.TreeItem>
    </Burger.TreeItem>)
}
export default FlowItem;
