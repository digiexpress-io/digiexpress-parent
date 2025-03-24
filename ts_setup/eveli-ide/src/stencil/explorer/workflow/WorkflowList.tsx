import React from 'react';
import { Box, Typography } from '@mui/material';
import { SimpleTreeView } from "@mui/x-tree-view";
import ArrowDropDownIcon from "@mui/icons-material/ArrowDropDown";
import { useIntl } from 'react-intl'

import { Composer } from '../../context';
import { StencilApi } from '@/burger';
import { WorkflowEdit } from '../../workflow/WorkflowEdit';
import WorkflowItem from './WorkflowItem';
import { WorkflowsListRoot, useUtilityClasses } from './useUtilityClasses';

import * as Burger from '@/burger';

const findMainId = (values: string[]) => {
  const result = values.filter(id => !id.endsWith("-nested"));
  if (result.length) {
    return result[0];
  }
  return undefined;
}

const EndIcon: React.FC = () => {
  return <Box style={{ width: 24 }} />;
}

export const WorkflowList: React.FC<{ searchString: string }> = ({ searchString }) => {
  const intl = useIntl();

  const { session } = Composer.useComposer();
  const [expanded, setExpanded] = React.useState<string[]>([]);
  const [editWorkflow, setEditWorkflow] = React.useState<undefined | StencilApi.WorkflowId>(undefined);

  const workflows: Composer.WorkflowView[] = React.useMemo(() => {
    if (searchString) {
      return session.search.filterWorkflows(searchString).map(searchResult => session.getWorkflowView(searchResult.source.id))
    }
    return session.workflows;
  }, [searchString, session]);

  const classes = useUtilityClasses();

  return (
    <>
      {editWorkflow ? <WorkflowEdit workflowId={editWorkflow} onClose={() => setEditWorkflow(undefined)} /> : undefined}

      <WorkflowsListRoot className={classes.root}>
        <Typography className={classes.title}>{intl.formatMessage({ id: 'main.services.all' })}</Typography>

        <SimpleTreeView expandedItems={expanded} slots={{ collapseIcon: ArrowDropDownIcon, expandIcon: ArrowDropDownIcon, endIcon: EndIcon }}

          onExpandedItemsChange={(_event: React.SyntheticEvent, nodeIds: string[]) => {
            const active = findMainId(expanded);
            const newId = findMainId(nodeIds.filter(n => n !== active));
            if (active !== newId && active && newId) {
              nodeIds.splice(nodeIds.indexOf(active), 1);
            }
            setExpanded(nodeIds);
          }}>
          {workflows.length ? workflows
            .map((w) => ({ w, name: session.getWorkflowName(w.workflow.id)?.name }))
            .sort((a, b) => a.name.localeCompare(b.name))
            .map((w) => (w.w))
            .map((view, index) => (
              <WorkflowItem key={index} workflowId={view.workflow.id} />
            )) : <Burger.EveliAlert title={intl.formatMessage({ id: 'stencil.services.found.none' })} />}
        </SimpleTreeView>
      </WorkflowsListRoot>
    </>
  );
}


