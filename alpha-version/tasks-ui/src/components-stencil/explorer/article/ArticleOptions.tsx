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
import Burger from 'components-burger';
import { bullfighters_red, cyan, purple_zergling, turquoise } from 'components-colors';

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
        color={cyan}
        icon={EditIcon}
        onClick={() => setDialogOpen('ArticleEdit')}
        labelText={<FormattedMessage id="article.edit.title" />} />
      
      <Burger.TreeItemOption nodeId={article.id + 'delete-nested'}
        color={cyan}
        icon={DeleteOutlineOutlinedIcon}
        onClick={() => setDialogOpen('ArticleDelete')}
        labelText={<FormattedMessage id="article.delete.title" />} />

      {/** Page options */}
      <Burger.TreeItemOption nodeId={article.id + 'pages.add'}
        color={turquoise}
        icon={AddCircleOutlineIcon}
        onClick={() => setDialogOpen('NewPage')}
        labelText={<FormattedMessage id="pages.add" />}/>

      <Burger.TreeItemOption nodeId={article.id + 'pages.change'}
        color={turquoise}
        icon={EditIcon}
        onClick={() => setDialogOpen('PageEdit')}
        labelText={<FormattedMessage id="pages.change" />}/>
            
      {Object.values(site.pages).filter(p => p.body.article === article.id).length > 0 && <Burger.TreeItemOption nodeId={article.id + 'pages.change.devmode'}
        color={turquoise}
        icon={EditIcon}
        onClick={() => setDialogOpen('PageEditDev')}
        labelText={<FormattedMessage id="pages.change.devmode" />}/>}

      <Burger.TreeItemOption nodeId={article.id + 'pages.delete'}
        color={turquoise}
        icon={DeleteOutlineOutlinedIcon}
        onClick={() => setDialogOpen('PageDelete')}
        labelText={<FormattedMessage id="pages.delete" />}/>

      <Burger.TreeItemOption nodeId={article.id + 'resource.create.workflows'}
        color={bullfighters_red}
        icon={AddCircleOutlineIcon}
        onClick={() => setDialogOpen('WorkflowComposer')}
        labelText={<FormattedMessage id="services.add" />}/>

      <Burger.TreeItemOption nodeId={article.id + 'resource.edit.workflows'}
        color={bullfighters_red}
        icon={EditIcon}
        onClick={() => handleInTab({ article, type: "ARTICLE_WORKFLOWS" })}
        labelText={<FormattedMessage id="services.change" />} />

      <Burger.TreeItemOption nodeId={article.id + 'resource.create.links'}
        color={purple_zergling}
        icon={AddCircleOutlineIcon}
        onClick={() => setDialogOpen('LinkComposer')}
        labelText={<FormattedMessage id="link.create" />}/>
      
      <Burger.TreeItemOption nodeId={article.id + 'resource.edit.links'}
        color={purple_zergling}
        icon={EditIcon}
        onClick={() => handleInTab({ article, type: "ARTICLE_LINKS" })}
        labelText={<FormattedMessage id="links.change" />} />
    </>
  );
}

export { ArticleOptions }
