import React from 'react';
import { Box, Typography, Divider } from '@mui/material';
import { SimpleTreeView } from "@mui/x-tree-view";
import ArrowDropDownIcon from "@mui/icons-material/ArrowDropDown";

import { useIntl } from 'react-intl';

import { Composer, StencilApi } from '../../context';
import ArticleItem, { ArticleItemOptions } from './ArticleItem';
import { LinkEdit } from '../../link/LinkEdit';
import { WorkflowEdit } from '../../workflow/WorkflowEdit';
import { ArticlesListRoot, useUtilityClasses } from './useUtilityClasses';
import { useStencilNav } from '../../nav';


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

export const ArticleList: React.FC<{ searchString: string }> = ({ searchString }) => {
  const intl = useIntl();
  const { session } = Composer.useComposer();
  const { getArticle, onNav } = useStencilNav();
  const expanded = getArticle().expanded ?? [];

  const [editLink, setEditLink] = React.useState<undefined | StencilApi.LinkId>(undefined);
  const [editWorkflow, setEditWorkflow] = React.useState<undefined | StencilApi.WorkflowId>(undefined);
  const articleOptions: ArticleItemOptions = { setEditLink, setEditWorkflow }

  const classes = useUtilityClasses();

  const treeItems: Composer.ArticleView[] = React.useMemo(() => {
    if (searchString) {
      return session.search.filterArticles(searchString).map(searchResult => session.getArticleView(searchResult.source.id))
    }
    return session.articles;
  }, [searchString, session]);

  treeItems.sort((l0, l1) => {
    return l0.displayOrder - l1.displayOrder;
  });


  function handleExpanded(_event: React.SyntheticEvent, nodeIds: string[]) {
    const active = findMainId(expanded);
    const newId = findMainId(nodeIds.filter(n => n !== active));
    if (active !== newId && active && newId) {
      nodeIds.splice(nodeIds.indexOf(active), 1);
    }
    onNav({ type: 'ARTICLES', article: active, expanded: [...nodeIds]})
  }


  return (
    <>
      {editLink ? <LinkEdit linkId={editLink} onClose={() => setEditLink(undefined)} /> : undefined}
      {editWorkflow ? <WorkflowEdit workflowId={editWorkflow} onClose={() => setEditWorkflow(undefined)} /> : undefined}
      
      <ArticlesListRoot className={classes.root}>
        <Typography className={classes.title}>{intl.formatMessage({ id: 'main.articles.all' })}</Typography>

        <SimpleTreeView expandedItems={expanded}
          slots={{ collapseIcon: ArrowDropDownIcon, expandIcon: ArrowDropDownIcon, endIcon: EndIcon }}
          onExpandedItemsChange={handleExpanded}>

          {treeItems.map((view) => <>
            <ArticleItem key={view.article.id} articleId={view.article.id} options={articleOptions} />
            <Divider />
          </>
          )}
        </SimpleTreeView>
      </ArticlesListRoot>
    </>
  );
}



