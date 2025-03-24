import React from "react";
import { Box, Typography } from "@mui/material";
import TableChartOutlinedIcon from '@mui/icons-material/TableChartOutlined';
import AccountTreeOutlinedIcon from '@mui/icons-material/AccountTreeOutlined';

import { FormattedMessage } from 'react-intl';


import * as Burger from '@/eveli-styles';

import { WrenchComposerApi } from '../../wrench-setup';
import { HdesApi } from '@/api-wrench';
import DecisionOptions from './DecisionOptions';
import { useWrenchNav } from "../../wrench-nav";



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

const DecisionItem: React.FC<{ decisionId: HdesApi.DecisionId }> = ({ decisionId }) => {

  const { session, isArticleSaved } = WrenchComposerApi.useComposer();
  const { onNav } = useWrenchNav();
  
  
  const decision = session.site.decisions[decisionId];
  
  const saved = isArticleSaved(decision);
  const decisionName = decision.ast ? decision.ast.name : decision.id;

  const flows: HdesApi.Entity<HdesApi.AstFlow>[] = [];

  return (
    <Burger.TreeItem itemId={decision.id} labelText={decisionName} labelIcon={TableChartOutlinedIcon} labelcolor={saved ? "explorerItem" : "secondary.light"}>

      {/** Decision options */}
      <Burger.TreeItem itemId={decision.id + 'options-nested'}
        labelText={<FormattedMessage id="options" />}
      >
        <DecisionOptions decision={decision} />
      </Burger.TreeItem>


      {/** Decision options */}
      <Burger.TreeItem itemId={decision.id + 'flows-nested'}
        labelText={<FormattedMessage id="flows" />}
        labelInfo={`${flows.length}`}
        labelcolor="primary">

        {flows.map(view => (<FlowItem key={view.id} nodeId={view.id}
          labelText={view.ast ? view.ast.name : view.id}
          onClick={() => onNav({ type: 'ENTITY_EDITOR', id: view.id })}
        />)
        )}
      </Burger.TreeItem>
    </Burger.TreeItem>)
}
export default DecisionItem;
