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

import Burger from '@the-wrench-io/react-burger';

import MsgTreeItem from '../MsgTreeItem';
import { Composer, Client } from '../../context';
import { ProcessValueOptions } from './ServicesOptions';


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


const ProcessValue: React.FC<{ value: Client.ProcessValue }> = ({ value }) => {
  const { session } = Composer.useComposer();
  const nav = Composer.useNav();
  const saved = true;
  const ok = true;

  return (
    <Burger.TreeItem nodeId={value.id} labelText={value.name}
      labelIcon={ArticleOutlinedIcon}
      labelInfo={ok ? undefined : <ConstructionIcon color="error" />}
      labelcolor={saved ? "explorerItem" : "explorerItem.contrastText"}>

      {/** Service options */}
      <Burger.TreeItem nodeId={value.id + 'options-nested'}
        labelText={<FormattedMessage id="processValue.options" />}
        labelIcon={EditIcon}>
        <ProcessValueOptions value={value} />
      </Burger.TreeItem>

      {/** Dialob options */}
      <Burger.TreeItem nodeId={value.id + 'processvalues' + value.formId}
        labelText={<FormattedMessage id="processValue.dialob" />}
        labelIcon={FolderOutlinedIcon}
        labelInfo={``}
        labelcolor="page">

        <DecisionItem key={value.formId} nodeId={`${value.id}-form`}
          labelText={value.formId}
          onClick={() => {}}
        />

      </Burger.TreeItem>

      {/** Flow options */}
      <Burger.TreeItem nodeId={value.id + 'processvalues-' + value.flowId}
        labelText={<FormattedMessage id="processValue.hdes" />}
        labelIcon={FolderOutlinedIcon}
        labelInfo={``}
        labelcolor="page">

        <DecisionItem key={value.flowId} nodeId={`${value.id}-flow`}
          labelText={value.flowId}
          onClick={() => {}}
        />

      </Burger.TreeItem>

      {/** Stencil options */}
      <Burger.TreeItem nodeId={value.id + 'processvalues-stencil'}
        labelText={<FormattedMessage id="processValue.stencil" />}
        labelIcon={FolderOutlinedIcon}
        labelInfo={``}
        labelcolor="page">

        <DecisionItem key={value.flowId} nodeId={`${value.id}-flow`}
          labelText={value.flowId}
          onClick={() => {}}
        />

      </Burger.TreeItem>


    </Burger.TreeItem>)
}

export { ProcessValue };
