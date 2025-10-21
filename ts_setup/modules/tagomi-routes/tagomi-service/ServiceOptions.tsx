import React from 'react';
import { useIntl } from 'react-intl';
import { PictureAsPdf as PictureAsPdfIconIcon } from '@mui/icons-material';
import { AddCircleOutline as AddCircleOutlineIcon } from '@mui/icons-material';
import { DeleteOutlineOutlined as DeleteOutlineOutlinedIcon } from '@mui/icons-material';
import { ModeEdit as EditIcon } from '@mui/icons-material';

import * as Burger from '@dxs-ts/eveli-primitives';

import { TagomiComposerApi as Composer, TagomiApi } from '@dxs-ts/tagomi-api';
import { useTagomiNav } from '../tagomi-nav';
import { ServiceEdit } from './ServiceEdit';
import { ServiceDelete } from './ServiceDelete';
import { NewTemplate, TemplateDelete, TemplateLocaleEdit } from '../tagomi-template';
import { DebugLocale } from '../tagomi-debug';


interface ServiceOptionsProps {
  service: TagomiApi.Service,
}

export const ServiceOptions: React.FC<ServiceOptionsProps> = ({ service }) => {
  const intl = useIntl();
  const { site, backend } = Composer.useComposer();
  const { activeItem, onNav } = useTagomiNav();

  const [dialogOpen, setDialogOpen] = React.useState<undefined | 
    'ServiceEdit' | 
    'NewTemplate' | 
    'TemplateEdit' | 
    'TemplateDelete' | 
    'ServiceDelete' |
    'DebugLocale'
  >(undefined);

  const handleDialogClose = () => setDialogOpen(undefined);

  return (
    <>
      {dialogOpen === 'ServiceEdit' ? <ServiceEdit serviceId={service.id} onClose={handleDialogClose} /> : null}
      {dialogOpen === 'ServiceDelete' ? <ServiceDelete serviceId={service.id} onClose={handleDialogClose} /> : null}
      {dialogOpen === 'NewTemplate' ? <NewTemplate serviceId={service.id} onClose={handleDialogClose} /> : null}
      {dialogOpen === 'TemplateEdit' ? <TemplateLocaleEdit serviceId={service.id} onClose={handleDialogClose} /> : null}
      {dialogOpen === 'TemplateDelete' ? <TemplateDelete serviceId={service.id} onClose={handleDialogClose} /> : null}
      {dialogOpen === 'DebugLocale' ? <DebugLocale serviceId={service.id} onClose={handleDialogClose} /> : null}

      {/** Article options */}
      <Burger.TreeItemOption nodeId={service.id + 'edit-nested'}
        color='primary'
        icon={EditIcon}
        onClick={() => setDialogOpen('ServiceEdit')}
        labelText={intl.formatMessage({ id: 'tagomi.service.options.edit' })}>
      </Burger.TreeItemOption>
      <Burger.TreeItemOption nodeId={service.id + 'delete-nested'}
        color='primary'
        icon={DeleteOutlineOutlinedIcon}
        onClick={() => setDialogOpen('ServiceDelete')}
        labelText={intl.formatMessage({ id: 'tagomi.service.options.delete' })
        }>
      </Burger.TreeItemOption>

      {/** Template options */}
      <Burger.TreeItemOption nodeId={service.id + 'pages.add'}
        color='page'
        icon={AddCircleOutlineIcon}
        onClick={() => setDialogOpen('NewTemplate')}
        labelText={intl.formatMessage({ id: 'tagomi.service.options.templates.create' })} >
      </Burger.TreeItemOption>
      <Burger.TreeItemOption nodeId={service.id + 'pages.change'}
        color='page'
        icon={EditIcon}
        onClick={() => setDialogOpen('TemplateEdit')}
        labelText={intl.formatMessage({ id: 'tagomi.service.options.template.changeLocale' })}>
      </Burger.TreeItemOption>

      <Burger.TreeItemOption nodeId={service.id + 'pages.delete'}
        color='page'
        icon={DeleteOutlineOutlinedIcon}
        onClick={() => setDialogOpen('TemplateDelete')}
        labelText={intl.formatMessage({ id: 'tagomi.service.options.template.delete' })}>
      </Burger.TreeItemOption>


      <Burger.TreeItemOption nodeId={service.id + 'pages.debug'}
        color='page'
        icon={PictureAsPdfIconIcon}
        onClick={() => setDialogOpen('DebugLocale')}
        labelText={intl.formatMessage({ id: 'tagomi.service.options.template.debug' })}>
      </Burger.TreeItemOption>
    </>
  );
}
