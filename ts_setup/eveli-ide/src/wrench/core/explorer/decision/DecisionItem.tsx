import React from "react";
import { Box, Typography } from "@mui/material";

import FolderOutlinedIcon from '@mui/icons-material/FolderOutlined';
import AccountTreeOutlinedIcon from '@mui/icons-material/AccountTreeOutlined';
import ArticleOutlinedIcon from '@mui/icons-material/ArticleOutlined';
import EditIcon from '@mui/icons-material/ModeEdit';

import { FormattedMessage } from 'react-intl';


import * as Burger from '@/burger';

import { Composer,  } from '../../context';
import { HdesApi } from '../../client';
import DecisionOptions from './DecisionOptions';



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

const DecisionItem: React.FC<{ decisionId: HdesApi.DecisionId }> = ({ decisionId }) => {

  const { session, isArticleSaved } = Composer.useComposer();
  const nav = Composer.useNav();
  
  
  const decision = session.site.decisions[decisionId];
  
  const saved = isArticleSaved(decision);
  const decisionName = decision.ast ? decision.ast.name : decision.id;

  const flows: HdesApi.Entity<HdesApi.AstFlow>[] = [];

  return (
    <Burger.TreeItem nodeId={decision.id} labelText={decisionName} labelIcon={ArticleOutlinedIcon} labelcolor={saved ? "explorerItem" : "explorerItem.contrastText"}>

      {/** Decision options */}
      <Burger.TreeItem nodeId={decision.id + 'options-nested'}
        labelText={<FormattedMessage id="options" />}
        labelIcon={EditIcon}>
        <DecisionOptions decision={decision} />
      </Burger.TreeItem>


      {/** Decision options */}
      <Burger.TreeItem nodeId={decision.id + 'flows-nested'}
        labelText={<FormattedMessage id="flows" />}
        labelIcon={FolderOutlinedIcon}
        labelInfo={`${flows.length}`}
        labelcolor="article">

        {flows.map(view => (<FlowItem key={view.id} nodeId={view.id}
          labelText={view.ast ? view.ast.name : view.id}
          onClick={() => nav.handleInTab({ article: view })}
        />)
        )}
      </Burger.TreeItem>
    </Burger.TreeItem>)
}
export default DecisionItem;
