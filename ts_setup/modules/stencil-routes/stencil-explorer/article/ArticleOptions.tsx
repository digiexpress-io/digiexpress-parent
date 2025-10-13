import React from 'react';
import { useTheme } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { AddCircleOutline as AddCircleOutlineIcon } from '@mui/icons-material';
import { DeleteOutlineOutlined as DeleteOutlineOutlinedIcon } from '@mui/icons-material';
import { ModeEdit as EditIcon } from '@mui/icons-material';

import { LinkComposer } from '../../stencil-link';
import { WorkflowComposer } from '../../stencil-workflow';
import { ArticleEdit, ArticleDelete } from '../../stencil-article';
import { NewPage, PageEdit, PageDelete, PageEditDevMode } from '../../stencil-page';
import { StencilComposerApi as Composer } from '@dxs-ts/stencil-api';
import { StencilApi } from '@dxs-ts/stencil-api';
import * as Burger from '@dxs-ts/eveli-primitives';
import { useStencilNav } from '../../stencil-nav';

interface ArticleOptionsProps {
  article: StencilApi.Article,

}
const ArticleOptions: React.FC<ArticleOptionsProps> = ({ article }) => {
  const theme = useTheme();
  const [dialogOpen, setDialogOpen] = React.useState<undefined | 'ArticleEdit' | 'NewPage' | 'PageEdit' | 'PageEditDev' | 'PageDelete' | 'ArticleDelete' | 'LinkComposer' | 'WorkflowComposer'>(undefined);

  const { site } = Composer.useComposer();
  const handleDialogClose = () => setDialogOpen(undefined);
  const { activeItem, onNav } = useStencilNav();

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
        color='primary'
        icon={EditIcon}
        onClick={() => setDialogOpen('ArticleEdit')}
        labelText={<FormattedMessage id="article.edit.title" />}>
      </Burger.TreeItemOption>
      <Burger.TreeItemOption nodeId={article.id + 'delete-nested'}
        color='primary'
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
        color={theme.palette.primary.dark}
        icon={AddCircleOutlineIcon}
        onClick={() => setDialogOpen('WorkflowComposer')}
        labelText={<FormattedMessage id="services.add" />}>
      </Burger.TreeItemOption>

      <Burger.TreeItemOption nodeId={article.id + 'resource.edit.workflows'}
        color={theme.palette.primary.dark}
        icon={EditIcon}
        onClick={() => onNav({ article: article.id, type: "ARTICLE_WORKFLOWS" })}
        labelText={<FormattedMessage id="services.change" />}>
      </Burger.TreeItemOption>

      <Burger.TreeItemOption nodeId={article.id + 'resource.create.links'}
        color={theme.palette.primary.light}
        icon={AddCircleOutlineIcon}
        onClick={() => setDialogOpen('LinkComposer')}
        labelText={<FormattedMessage id="link.create" />}>
      </Burger.TreeItemOption>

      <Burger.TreeItemOption nodeId={article.id + 'resource.edit.links'}
        color={theme.palette.primary.light}
        icon={EditIcon}
        onClick={() => onNav({ article: article.id, type: "ARTICLE_LINKS" })}
        labelText={<FormattedMessage id="links.change" />}>
      </Burger.TreeItemOption>

    </>
  );
}

export { ArticleOptions }
