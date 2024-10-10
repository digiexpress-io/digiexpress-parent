import * as React from "react";
import { Box, Typography } from "@mui/material";

import FolderOutlinedIcon from '@mui/icons-material/FolderOutlined';
import ArticleOutlinedIcon from '@mui/icons-material/ArticleOutlined';
import LinkIcon from '@mui/icons-material/Link';
import AccountTreeOutlinedIcon from '@mui/icons-material/AccountTreeOutlined';
import EditIcon from '@mui/icons-material/ModeEdit';
import ConstructionIcon from '@mui/icons-material/Construction';
import { FormattedMessage } from 'react-intl';

import Burger from '@the-wrench-io/react-burger';
import { Composer, StencilClient } from '../../context';
import { ArticleOptions } from './ArticleOptions';
import ArticlePageItem from './ArticlePageItem';



function WorkflowItem(props: {
  labelText: string;
  nodeId: string;
  children?: React.ReactChild;
  devMode?: boolean,
  onClick: () => void;
}) {

  return (
    <Burger.TreeItemRoot
      nodeId={props.nodeId}
      onClick={props.onClick}
      label={
        <Box sx={{ display: "flex", alignItems: "center", p: 0.5, pr: 0 }}>
          <Box component={props.devMode ? ConstructionIcon : AccountTreeOutlinedIcon} color="workflow.main" sx={{ pl: 1, mr: 1 }} />
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

interface LinkItemProps {
  labelText: string;
  nodeId: string;
  children?: React.ReactChild;
  onClick: () => void;
  devMode?: boolean;
}

const LinkItem: React.FC<LinkItemProps> = (props) => {
  return (
    <Burger.TreeItemRoot
      nodeId={props.nodeId}
      onClick={props.onClick}
      label={
        <Box sx={{ display: "flex", alignItems: "center", p: 0.5, pr: 0 }}>
          <Box component={props.devMode ? ConstructionIcon : LinkIcon} color="link.main" sx={{ pl: 1, mr: 1 }} />
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


interface ArticleItemOptions {
  setEditWorkflow: (workflowId: StencilClient.WorkflowId) => void,
  setEditLink: (linkId: StencilClient.LinkId) => void
}

const ArticleItem: React.FC<{
  articleId: StencilClient.ArticleId,
  nodeId?: string,
  options?: ArticleItemOptions
}> = ({ articleId, nodeId, options }) => {

  const { session, isArticleSaved } = Composer.useComposer();
  const view = session.getArticleView(articleId);
  const { article, pages, workflows, links } = view;
  const saved = isArticleSaved(article);


  const isPageSaved = (pageView: Composer.PageView) => {
    const update = session.pages[pageView.page.id];
    if (!update) {
      return true;
    }
    return update.saved;
  }

  const articleName = session.getArticleName(view.article.id);
  return (
    <>
      <Burger.TreeItem nodeId={nodeId ? nodeId : article.id} labelText={articleName.name} labelIcon={article.body.devMode ? ConstructionIcon : ArticleOutlinedIcon} labelcolor={saved ? "explorerItem" : "explorerItem.contrastText"}>

        {/** Article options */
          options ? (<Burger.TreeItem nodeId={article.id + 'article-options-nested'}
            labelText={<FormattedMessage id="options" />}
            labelIcon={EditIcon}>
            <ArticleOptions article={article} />
          </Burger.TreeItem>) : null
        }

        {/** Pages */}
        <Burger.TreeItem nodeId={article.id + 'pages-nested'}
          labelText={<FormattedMessage id="pages" />}
          labelIcon={FolderOutlinedIcon}
          labelInfo={`${pages.length}`}
          labelcolor={saved ? "page" : "explorerItem.contrastText"}>
          {pages.map(pageView => (<ArticlePageItem key={pageView.page.id}
            saved={isPageSaved(pageView)}
            article={view}
            page={pageView} />))}
        </Burger.TreeItem>


        {/** Workflows options */
          options ? (<Burger.TreeItem nodeId={article.id + 'workflows-nested'}
            labelText={<FormattedMessage id="services" />}
            labelIcon={FolderOutlinedIcon}
            labelInfo={`${workflows.length}`}
            labelcolor="workflow">

            {workflows
              .map((w) => ({ w, name: session.getWorkflowName(w.workflow.id)?.name }))
              .sort((a, b) => a.name.localeCompare(b.name))
              .map((w) => w.w)
              .map(view => (<WorkflowItem
                key={view.workflow.id}
                labelText={session.getWorkflowName(view.workflow.id).name}
                devMode={view.workflow.body.devMode}
                nodeId={view.workflow.id}

                onClick={() => options.setEditWorkflow(view.workflow.id)} />))}
          </Burger.TreeItem>) : null
        }

        {/** Links options */
          options ? (<Burger.TreeItem nodeId={article.id + 'links-nested'}
            labelText={<FormattedMessage id="links" />}
            labelIcon={FolderOutlinedIcon}
            labelInfo={`${links.length}`}
            labelcolor="link">

            {links
              .map((w) => ({ w, name: session.getLinkName(w.link.id)?.name }))
              .sort((a, b) => a.name.localeCompare(b.name))
              .map((w) => w.w)
              .map(view => (<LinkItem key={view.link.id}
                labelText={session.getLinkName(view.link.id).name}
                nodeId={view.link.id}
                onClick={() => options.setEditLink(view.link.id)} 
                devMode={view.link.body.devMode} />)
              )}
          </Burger.TreeItem>) : null

        }

      </Burger.TreeItem>
    </>)
}

export type { ArticleItemOptions }
export default ArticleItem;
