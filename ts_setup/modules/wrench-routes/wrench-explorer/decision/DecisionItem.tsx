import React from "react";
import { Box, Typography } from "@mui/material";
import { DateTime } from 'luxon';
import { TableChartOutlined as TableChartOutlinedIcon } from '@mui/icons-material';
import { AccountTreeOutlined as AccountTreeOutlinedIcon } from '@mui/icons-material';

import { FormattedMessage } from 'react-intl';

import { EveliPermissions, TreeItemRoot, TreeItem } from "@dxs-ts/eveli-primitives";

import { HdesApi, WrenchComposerApi } from '@dxs-ts/wrench-api';
import DecisionOptions from './DecisionOptions';
import { useWrenchNav } from "../../wrench-nav";
import InfoTreeItem from '../InfoTreeItem';



function FlowItem(props: {
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
  const lastUpdated = session.getLastUpdated(decisionId);
  const formattedDate = lastUpdated ? DateTime.fromISO(lastUpdated, { zone: 'UTC' }).setLocale('fi').toFormat('d.M.yyyy HH:mm') : '';

  const assocs = decision.associations.filter(a => a.refType === 'FLOW');
  const flows: HdesApi.Entity<HdesApi.AstFlow>[] = assocs.map(a =>  a.id ? session.site.flows[a.id] : session.getFlow(a.ref)).filter(a => !!a);

  return (
    <TreeItem itemId={decision.id} labelText={decisionName} labelIcon={TableChartOutlinedIcon} labelcolor={saved ? "explorerItem" : "secondary.light"}>

      {/** Decision options */}
      <EveliPermissions id='EDIT_WRENCH_ASSET'>
        <TreeItem itemId={decision.id + 'options-nested'}
          labelText={<FormattedMessage id="options" />}
        >
          <DecisionOptions decision={decision} />
        </TreeItem>
      </EveliPermissions>

      {/** Decision info */}
      {lastUpdated && <InfoTreeItem nodeId={decision.id} lastUpdated={lastUpdated} />}

      {/** Decision flows */}
      <TreeItem itemId={decision.id + 'flows-nested'}
        labelText={<FormattedMessage id="flows" />}
        labelInfo={`${flows.length}`}
        labelcolor="primary">

        {flows.map(view => (<FlowItem key={view.id} nodeId={view.id}
          labelText={view.ast ? view.ast.name : view.id}
          onClick={() => onNav({ type: 'ENTITY_EDITOR', id: view.id })}
        />)
        )}
      </TreeItem>
    </TreeItem>)
}
export default DecisionItem;
