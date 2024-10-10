import React from 'react';
import { Box, Typography } from '@mui/material';
import TreeView from "@mui/lab/TreeView";
import ArrowDropDownIcon from "@mui/icons-material/ArrowDropDown";
import ArrowRightIcon from "@mui/icons-material/ArrowRight";

import { Composer, StencilClient } from '../../context';
import ArticleItem, { ArticleItemOptions } from './ArticleItem';
import { LinkEdit } from '../../link/LinkEdit';
import { WorkflowEdit } from '../../workflow/WorkflowEdit';


const findMainId = (values: string[]) => {
  const result = values.filter(id => !id.endsWith("-nested"));
  if (result.length) {
    return result[0];
  }
  return undefined;
}

const ArticleExplorer: React.FC<{ searchString: string }> = ({searchString}) => {
  const { session } = Composer.useComposer();
  const [expanded, setExpanded] = React.useState<string[]>([]);

  const [editLink, setEditLink] = React.useState<undefined | StencilClient.LinkId>(undefined);
  const [editWorkflow, setEditWorkflow] = React.useState<undefined | StencilClient.WorkflowId>(undefined);
  const articleOptions: ArticleItemOptions = { setEditLink, setEditWorkflow }
    
  const treeItems: Composer.ArticleView[] = React.useMemo(() => {
    if(searchString) {
      return session.search.filterArticles(searchString).map(searchResult => session.getArticleView(searchResult.source.id))
    }
    return session.articles;
  }, [searchString, session]);

  treeItems.sort((l0, l1) => {
    
    
    return l0.displayOrder - l1.displayOrder;
  });

  return (
    <Box>
      { editLink ? <LinkEdit linkId={editLink} onClose={() => setEditLink(undefined)} /> : undefined}
      { editWorkflow ? <WorkflowEdit workflowId={editWorkflow} onClose={() => setEditWorkflow(undefined)} /> : undefined}

      <Typography align="left"
        sx={{
          fontVariant: 'all-petite-caps',
          fontWeight: 'bold',
          color: 'explorerItem.main',
          ml: 1, mr: 1, mb: 1,
          borderBottom: '1px solid',
        }}>
      </Typography>

      <TreeView expanded={expanded}
        defaultCollapseIcon={<ArrowDropDownIcon />}
        defaultExpandIcon={<ArrowRightIcon />}
        defaultEndIcon={<div style={{ width: 24 }} />}
        onNodeToggle={(_event: React.SyntheticEvent, nodeIds: string[]) => {
          const active = findMainId(expanded);
          const newId = findMainId(nodeIds.filter(n => n !== active));
          if (active !== newId && active && newId) {
            nodeIds.splice(nodeIds.indexOf(active), 1);
          }
          setExpanded(nodeIds);
        }}>
        {treeItems.map((view) => <ArticleItem key={view.article.id} articleId={view.article.id} options={articleOptions} />)}
      </TreeView>
    </Box>
  );
}

export { ArticleExplorer }

