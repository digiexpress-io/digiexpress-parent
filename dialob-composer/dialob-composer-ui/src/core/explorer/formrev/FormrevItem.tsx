import React from "react";
import { Box, Typography } from "@mui/material";

import FolderOutlinedIcon from '@mui/icons-material/FolderOutlined';
import AccountTreeOutlinedIcon from '@mui/icons-material/AccountTreeOutlined';
import ArticleOutlinedIcon from '@mui/icons-material/ArticleOutlined';
import EditIcon from '@mui/icons-material/ModeEdit';
import ConstructionIcon from '@mui/icons-material/Construction';
import PriorityHighIcon from '@mui/icons-material/PriorityHigh';
import LowPriorityIcon from '@mui/icons-material/LowPriority';

import { FormattedMessage } from 'react-intl';


import Burger from '@the-wrench-io/react-burger';

import { Composer, Client } from '../../context';
import FormrevOptions from './FormrevOptions';
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
  entity?: Client.Form;
  ref: Client.FormRevisionEntryDocument;
}

const FormrevItem: React.FC<{ formrevId: Client.FormrevId }> = ({ formrevId }) => {

  const { session, isArticleSaved } = Composer.useComposer();
  const nav = Composer.useNav();

  const formrev = session.site.revs[formrevId];

  const saved = isArticleSaved(formrev);
  const flowName = formrev.name ? formrev.name : formrev.id;
  
  const decisions: RefDecision[] = formrev.entries
    .map(a => ({ entity: session.getForm(a.formId), ref: a }));

  return (
    <Burger.TreeItem nodeId={formrev.id}
      labelText={flowName}
      labelIcon={ArticleOutlinedIcon}
      labelcolor={saved ? "explorerItem" : "explorerItem.contrastText"}
      labelInfo={undefined}>

      {/** Flow options */}
      <Burger.TreeItem nodeId={formrev.id + 'options-nested'}
        labelText={<FormattedMessage id="options" />}
        labelIcon={EditIcon} >
        <FormrevOptions formrev={formrev} />
      </Burger.TreeItem>


      {/** Decision options */}
      <Burger.TreeItem nodeId={formrev.id + 'decisions-nested'}
        labelText={<FormattedMessage id="formrev-tags" />}
        labelIcon={FolderOutlinedIcon}
        labelInfo={`${decisions.length}`}
        labelcolor="page">

        {decisions.map(view => (<DecisionItem key={view.ref.id} nodeId={`${formrev.id}-dt-${view.ref.id}`}
          labelText={view.ref.revisionName}
          onClick={() => view.entity ? nav.handleInTab({ article: view.entity }) : undefined}
        />))}
      </Burger.TreeItem>

    </Burger.TreeItem>)
}
export default FormrevItem;
