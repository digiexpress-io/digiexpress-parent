import React from 'react';
import { Box, Typography } from '@mui/material';
import { SimpleTreeView } from "@mui/x-tree-view";
import ArrowDropDownIcon from "@mui/icons-material/ArrowDropDown";

import { Composer, StencilApi } from '../../context';
import { WorkflowEdit } from '../../workflow/WorkflowEdit';
import WorkflowItem from './WorkflowItem';

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

const WorkflowExplorer: React.FC<{ searchString: string }> = ({ searchString }) => {
  const { session } = Composer.useComposer();
  const [expanded, setExpanded] = React.useState<string[]>([]);
  const [editWorkflow, setEditWorkflow] = React.useState<undefined | StencilApi.WorkflowId>(undefined);

  const workflows: Composer.WorkflowView[] = React.useMemo(() => {
    if (searchString) {
      return session.search.filterWorkflows(searchString).map(searchResult => session.getWorkflowView(searchResult.source.id))
    }
    return session.workflows;
  }, [searchString, session]);

  return (
    <Box>
      {editWorkflow ? <WorkflowEdit workflowId={editWorkflow} onClose={() => setEditWorkflow(undefined)} /> : undefined}

      <Typography align="left"
        sx={{
          fontVariant: 'all-petite-caps',
          fontWeight: 'bold',
          color: 'explorerItem.main',
          ml: 1, mr: 1, mb: 1,
          borderBottom: '1px solid'
        }}>
      </Typography>

      <SimpleTreeView expandedItems={expanded}
        slots={{ collapseIcon: ArrowDropDownIcon, expandIcon: ArrowDropDownIcon, endIcon: EndIcon }}

        onExpandedItemsChange={(_event: React.SyntheticEvent, nodeIds: string[]) => {
          const active = findMainId(expanded);
          const newId = findMainId(nodeIds.filter(n => n !== active));
          if (active !== newId && active && newId) {
            nodeIds.splice(nodeIds.indexOf(active), 1);
          }
          setExpanded(nodeIds);
        }}>
        {workflows
          .map((w) => ({w, name: session.getWorkflowName(w.workflow.id)?.name}))
          .sort((a, b) => a.name.localeCompare(b.name))
          .map((w) => (w.w))
          .map((view, index) => (
          <WorkflowItem key={index} workflowId={view.workflow.id} />
        ))}
      </SimpleTreeView>
    </Box>
  );
}

export { WorkflowExplorer }

