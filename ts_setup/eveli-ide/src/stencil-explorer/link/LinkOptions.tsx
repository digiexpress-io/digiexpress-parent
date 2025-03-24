import React from 'react';
import { useTheme } from '@mui/material';
import { FormattedMessage } from 'react-intl';

import { LinkDelete } from '../../stencil-link/LinkDelete';
import { LinkEdit } from '../../stencil-link';
import { StencilApi } from '@/api-stencil'
import * as Burger from '@/eveli-styles';

const LinkOptions: React.FC<{ link: StencilApi.Link }> = ({ link }) => {
  const theme = useTheme();
  const [dialogOpen, setDialogOpen] = React.useState<undefined | 'LinkEdit' | 'LinkDelete'>(undefined);
  const handleDialogClose = () => setDialogOpen(undefined);

  return (
    <>
      { dialogOpen === 'LinkEdit' ? <LinkEdit linkId={link.id} onClose={handleDialogClose} /> : null}
      { dialogOpen === 'LinkDelete' ? <LinkDelete linkId={link.id} onClose={handleDialogClose} /> : null}

      <Burger.TreeItemOption nodeId={link.id + 'link.edit'}
        color={theme.palette.primary.light}
        onClick={() => setDialogOpen('LinkEdit')}
        labelText={<FormattedMessage id="link.edit.title" />}>
      </Burger.TreeItemOption>


      <Burger.TreeItemOption nodeId={link.id + 'link.delete'}
        color={theme.palette.primary.light}
        onClick={() => setDialogOpen('LinkDelete')}
        labelText={<FormattedMessage id="link.delete.title" />}>
      </Burger.TreeItemOption>
    </>
  );
}

export { LinkOptions }
