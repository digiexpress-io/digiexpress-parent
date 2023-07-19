import React from 'react';
import { FormattedMessage } from 'react-intl';
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutline';
import DeleteOutlineOutlinedIcon from '@mui/icons-material/DeleteOutlineOutlined';
import EditIcon from '@mui/icons-material/ModeEdit';

import { LinkComposer } from '../../link';
import { WorkflowComposer } from '../../workflow';
import { ArticleEdit, ArticleDelete } from '../../article';
import { NewPage, PageEdit, PageDelete, PageEditDevMode } from '../../page';
import { Composer, StencilClient } from '../../context';
import Burger from '@the-wrench-io/react-burger';

interface ArticleOptionsProps {
  article: StencilClient.Article,

}
const ArticleOptions: React.FC<ArticleOptionsProps> = ({ article }) => {

  const [dialogOpen, setDialogOpen] = React.useState<undefined | 'ArticleEdit' | 'NewPage' | 'PageEdit' | 'PageEditDev' | 'PageDelete' | 'ArticleDelete' | 'LinkComposer' | 'WorkflowComposer'>(undefined);

  const { site } = Composer.useComposer();
  const handleDialogClose = () => setDialogOpen(undefined);
  const { handleInTab } = Composer.useNav();

  return (
    <>
      { dialogOpen === 'ArticleEdit' ? <ArticleEdit articleId={article.id} onClose={handleDialogClose} /> : null}
      { dialogOpen === 'NewPage' ? <NewPage articleId={article.id} onClose={handleDialogClose} /> : null}
      { dialogOpen === 'PageEdit' ? <PageEdit articleId={article.id} onClose={handleDialogClose} /> : null}
      { dialogOpen === 'PageEditDev' ? <PageEditDevMode articleId={article.id} onClose={handleDialogClose} /> : null}
      { dialogOpen === 'PageDelete' ? <PageDelete articleId={article.id} onClose={handleDialogClose} /> : null}
      { dialogOpen === 'ArticleDelete' ? <ArticleDelete articleId={article.id} onClose={handleDialogClose} /> : null}
      { dialogOpen === 'LinkComposer' ? <LinkComposer onClose={handleDialogClose} /> : null}
      { dialogOpen === 'WorkflowComposer' ? <WorkflowComposer onClose={handleDialogClose} /> : null}

      {/** Article options */}
      <Burger.TreeItemOption nodeId={article.id + 'edit-nested'}
        color='article'
        icon={EditIcon}
        onClick={() => setDialogOpen('ArticleEdit')}
        labelText={<FormattedMessage id="article.edit.title" />}>
      </Burger.TreeItemOption>
      <Burger.TreeItemOption nodeId={article.id + 'delete-nested'}
        color='article'
        icon={DeleteOutlineOutlinedIcon}
        onClick={() => setDialogOpen('ArticleDelete')}
        labelText={<FormattedMessage id="article.delete.title" />}>
      </Burger.TreeItemOption>

      {/** Page options */}
      <Burger.TreeItemOption nodeId={article.id + 'pages.add'}
        color='page'
        icon={AddCircleOutlineIcon}
        onClick={() => setDialogOpen('NewPage')}
        labelText={<FormattedMessage id="pages.add" />}>
      </Burger.TreeItemOption>
      <Burger.TreeItemOption nodeId={article.id + 'pages.change'}
        color='page'
        icon={EditIcon}
        onClick={() => setDialogOpen('PageEdit')}
        labelText={<FormattedMessage id="pages.change" />}>
      </Burger.TreeItemOption>
      {Object.values(site.pages).filter(p => p.body.article === article.id).length > 0 && <Burger.TreeItemOption nodeId={article.id + 'pages.change.devmode'}
        color='page'
        icon={EditIcon}
        onClick={() => setDialogOpen('PageEditDev')}
        labelText={<FormattedMessage id="pages.change.devmode" />}>
      </Burger.TreeItemOption>}
      <Burger.TreeItemOption nodeId={article.id + 'pages.delete'}
        color='page'
        icon={DeleteOutlineOutlinedIcon}
        onClick={() => setDialogOpen('PageDelete')}
        labelText={<FormattedMessage id="pages.delete" />}>
      </Burger.TreeItemOption>
      <Burger.TreeItemOption nodeId={article.id + 'resource.create.workflows'}
        color='workflow'
        icon={AddCircleOutlineIcon}
        onClick={() => setDialogOpen('WorkflowComposer')}
        labelText={<FormattedMessage id="services.add" />}>
      </Burger.TreeItemOption>

      <Burger.TreeItemOption nodeId={article.id + 'resource.edit.workflows'}
        color='workflow'
        icon={EditIcon}
        onClick={() => handleInTab({ article, type: "ARTICLE_WORKFLOWS" })}
        labelText={<FormattedMessage id="services.change" />}>
      </Burger.TreeItemOption>

      <Burger.TreeItemOption nodeId={article.id + 'resource.create.links'}
        color='link'
        icon={AddCircleOutlineIcon}
        onClick={() => setDialogOpen('LinkComposer')}
        labelText={<FormattedMessage id="link.create" />}>
      </Burger.TreeItemOption>

      <Burger.TreeItemOption nodeId={article.id + 'resource.edit.links'}
        color='link'
        icon={EditIcon}
        onClick={() => handleInTab({ article, type: "ARTICLE_LINKS" })}
        labelText={<FormattedMessage id="links.change" />}>
      </Burger.TreeItemOption>

    </>
  );
}

export { ArticleOptions }
