import React from 'react';
import { useTheme } from '@mui/material';
import { FormattedMessage } from 'react-intl';
import { AddCircleOutline as AddCircleOutlineIcon } from '@mui/icons-material';
import { DeleteOutlineOutlined as DeleteOutlineOutlinedIcon } from '@mui/icons-material';
import { ModeEdit as EditIcon } from '@mui/icons-material';

import * as Burger from '@dxs-ts/eveli-primitives';

import { TagomiComposerApi as Composer, TagomiApi } from '@dxs-ts/tagomi-api';
import { useTagomiNav } from '../tagomi-nav';
import { ServiceEdit } from './ServiceEdit';
import { ServiceDelete } from './ServiceDelete';
import { NewTagomiTemplate, TagomiTemplateEdit } from '../tagomi-template';

interface ServiceOptionsProps {
  service: TagomiApi.Service,
}

/** TODO
      { dialogOpen === 'PageDelete' ? <PageDelete articleId={article.id} onClose={handleDialogClose} /> : null}
 */

export const ServiceOptions: React.FC<ServiceOptionsProps> = ({ service }) => {
  const theme = useTheme();
  const [dialogOpen, setDialogOpen] = React.useState<undefined | 'ServiceEdit' | 'NewTemplate' | 'TemplateEdit' | 'PageEditDev' | 'PageDelete' | 'ServiceDelete' | 'LinkComposer' | 'WorkflowComposer'>(undefined);

  const { site, backend } = Composer.useComposer();
  const handleDialogClose = () => setDialogOpen(undefined);
  const { activeItem, onNav } = useTagomiNav();



  return (
    <>
      {dialogOpen === 'ServiceEdit' ? <ServiceEdit serviceId={service.id} onClose={handleDialogClose} /> : null}
      {dialogOpen === 'ServiceDelete' ? <ServiceDelete serviceId={service.id} onClose={handleDialogClose} /> : null}
      {dialogOpen === 'NewTemplate' ? <NewTagomiTemplate serviceId={service.id} onClose={handleDialogClose} /> : null}
      {dialogOpen === 'TemplateEdit' ? <TagomiTemplateEdit serviceId={service.id} onClose={handleDialogClose} /> : null}


      {/** Article options */}
      <Burger.TreeItemOption nodeId={service.id + 'edit-nested'}
        color='primary'
        icon={EditIcon}
        onClick={() => setDialogOpen('ServiceEdit')}
        labelText={<FormattedMessage id="tagomi.service.edit.title" />}>
      </Burger.TreeItemOption>
      <Burger.TreeItemOption nodeId={service.id + 'delete-nested'}
        color='primary'
        icon={DeleteOutlineOutlinedIcon}
        onClick={() => setDialogOpen('ServiceDelete')}
        labelText={<FormattedMessage id="tagomi.service.delete.title" />}>
      </Burger.TreeItemOption>

      {/** Template options */}
      <Burger.TreeItemOption nodeId={service.id + 'pages.add'}
        color='page'
        icon={AddCircleOutlineIcon}
        onClick={() => setDialogOpen('NewTemplate')}
        labelText={<FormattedMessage id="tagomi.service.templates.add" />}>
      </Burger.TreeItemOption>
      <Burger.TreeItemOption nodeId={service.id + 'pages.change'}
        color='page'
        icon={EditIcon}
        onClick={() => setDialogOpen('TemplateEdit')}
        labelText={<FormattedMessage id="tagomi.service.template.change" />}>
      </Burger.TreeItemOption>

      <Burger.TreeItemOption nodeId={service.id + 'pages.delete'}
        color='page'
        icon={DeleteOutlineOutlinedIcon}
        onClick={() => setDialogOpen('PageDelete')}
        labelText={<FormattedMessage id="pages.delete" />}>
      </Burger.TreeItemOption>
      <Burger.TreeItemOption nodeId={service.id + 'resource.create.workflows'}
        color={theme.palette.primary.dark}
        icon={AddCircleOutlineIcon}
        onClick={() => setDialogOpen('WorkflowComposer')}
        labelText={<FormattedMessage id="services.add" />}>
      </Burger.TreeItemOption>
    </>
  );
}
