import React from 'react';
import { useTheme } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import AddCircleOutlineIcon from '@mui/icons-material/AddCircleOutline';
import DeleteOutlineOutlinedIcon from '@mui/icons-material/DeleteOutlineOutlined';
import EditIcon from '@mui/icons-material/ModeEdit';

import * as Burger from '@dxs-ts/eveli-primitives';

import { TagomiComposerApi as Composer, TagomiApi } from '@dxs-ts/tagomi-api';
import { useTagomiNav } from '../tagomi-nav';

interface ArticleOptionsProps {
  service: TagomiApi.Service,
}

/** TODO
      { dialogOpen === 'ArticleEdit' ? <ArticleEdit articleId={article.id} onClose={handleDialogClose} /> : null}
      { dialogOpen === 'NewPage' ? <NewPage articleId={article.id} onClose={handleDialogClose} /> : null}
      { dialogOpen === 'PageEdit' ? <PageEdit articleId={article.id} onClose={handleDialogClose} /> : null}
      { dialogOpen === 'PageDelete' ? <PageDelete articleId={article.id} onClose={handleDialogClose} /> : null}
      { dialogOpen === 'ArticleDelete' ? <ArticleDelete articleId={article.id} onClose={handleDialogClose} /> : null}
 */

export const ServiceOptions: React.FC<ArticleOptionsProps> = ({ service: article }) => {
  const theme = useTheme();
  const [dialogOpen, setDialogOpen] = React.useState<undefined | 'ArticleEdit' | 'NewPage' | 'PageEdit' | 'PageEditDev' | 'PageDelete' | 'ArticleDelete' | 'LinkComposer' | 'WorkflowComposer'>(undefined);

  const { site } = Composer.useComposer();
  const handleDialogClose = () => setDialogOpen(undefined);
  const { activeItem, onNav } = useTagomiNav();

  return (
    <>


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
    </>
  );
}
