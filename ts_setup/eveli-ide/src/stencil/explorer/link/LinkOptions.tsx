import React from 'react';
import { FormattedMessage } from 'react-intl';

import { LinkDelete } from '../../link/LinkDelete';
import { LinkEdit } from '../../link';
import { StencilApi } from '../../context';
import * as Burger from '@/burger';

const LinkOptions: React.FC<{ link: StencilApi.Link }> = ({ link }) => {

  const [dialogOpen, setDialogOpen] = React.useState<undefined | 'LinkEdit' | 'LinkDelete'>(undefined);
  const handleDialogClose = () => setDialogOpen(undefined);

  return (
    <>
      { dialogOpen === 'LinkEdit' ? <LinkEdit linkId={link.id} onClose={handleDialogClose} /> : null}
      { dialogOpen === 'LinkDelete' ? <LinkDelete linkId={link.id} onClose={handleDialogClose} /> : null}

      <Burger.TreeItemOption nodeId={link.id + 'link.edit'}
        color={Burger.colors.purple}
        onClick={() => setDialogOpen('LinkEdit')}
        labelText={<FormattedMessage id="link.edit.title" />}>
      </Burger.TreeItemOption>


      <Burger.TreeItemOption nodeId={link.id + 'link.delete'}
        color={Burger.colors.purple}
        onClick={() => setDialogOpen('LinkDelete')}
        labelText={<FormattedMessage id="link.delete.title" />}>
      </Burger.TreeItemOption>
    </>
  );
}

export { LinkOptions }
