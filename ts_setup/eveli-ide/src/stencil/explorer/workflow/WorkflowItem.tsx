import * as React from "react";

import FolderOutlinedIcon from '@mui/icons-material/FolderOutlined';

import AccountTreeOutlinedIcon from '@mui/icons-material/AccountTreeOutlined';
import EditIcon from '@mui/icons-material/ModeEdit';
import ConstructionIcon from '@mui/icons-material/Construction';
import { FormattedMessage } from 'react-intl';

import * as Burger from '@/burger';
import { Composer, StencilApi } from '../../context';

import { WorkflowOptions } from './WorkflowOptions';
import ArticleItem from '../article/ArticleItem';

const WorkflowItem: React.FC<{ workflowId: StencilApi.WorkflowId }> = ({ workflowId }) => {
  const { session } = Composer.useComposer();
  const view = session.getWorkflowView(workflowId);
  const { workflow } = view;


  const workflowName = session.getWorkflowName(workflow.id);

  return (
    <>
      <Burger.TreeItem
        itemId={workflow.id}
        labelText={workflowName.name}
        labelcolor="explorerItem"
        labelIcon={workflow.body.devMode ? ConstructionIcon : AccountTreeOutlinedIcon}>

        <Burger.TreeItem itemId={workflow.id + 'options-nested'} labelText={<FormattedMessage id="options" />} labelIcon={EditIcon}>
          <WorkflowOptions workflow={workflow} />
        </Burger.TreeItem>


        {/** Article options */}
        <Burger.TreeItem itemId={workflow.id + 'articles-nested'}
          labelText={<FormattedMessage id="articles" />}
          labelIcon={FolderOutlinedIcon}
          labelInfo={`${workflow.body.articles.length}`}
          labelcolor="article">

          {workflow.body.articles.map((id => session.getArticleView(id))).map(view => (
            <ArticleItem key={view.article.id} articleId={view.article.id} nodeId={`${workflow.id}-${view.article.id}-nested`}/>
          ))}
        </Burger.TreeItem>

      </Burger.TreeItem>
    </>)
}

export default WorkflowItem;
