import React from 'react';
import { useIntl } from 'react-intl';
import { PictureAsPdf as PictureAsPdfIconIcon } from '@mui/icons-material';
import { AddCircleOutline as AddCircleOutlineIcon } from '@mui/icons-material';
import { DeleteOutlineOutlined as DeleteOutlineOutlinedIcon } from '@mui/icons-material';
import { ModeEdit as EditIcon } from '@mui/icons-material';

import * as Burger from '@dxs-ts/eveli-primitives';

import { TagomiComposerApi as Composer, TagomiApi } from '@dxs-ts/tagomi-api';
import { useTagomiNav } from '../../tagomi-nav';
import { ServiceEdit } from '../../tagomi-service/ServiceEdit';
import { ServiceDelete } from '../../tagomi-service/ServiceDelete';
import { NewTemplate, TemplateDelete, TemplateLocaleEdit } from '../../tagomi-template';
import { TemplateDependenciesAdd, TemplateDependenciesEdit } from '../../tagomi-dependencies';
import { DebugLocale } from '../../tagomi-debug';


interface ServiceOptionsProps {
  service: TagomiApi.Service,
}

export const ServiceOptions: React.FC<ServiceOptionsProps> = ({ service }) => {
  const intl = useIntl();
  const { site, backend } = Composer.useComposer();
  
  const serviceTemplates = Object.values(site.templates).filter(t => t.serviceId === service.id);
  const hasTemplates = serviceTemplates.length > 0;
  
  const assignedLocaleIds = new Set(serviceTemplates.map(t => t.localeId).filter(Boolean));
  const availableLocaleIds = Object.keys(site.locales ?? {});
  const selectableLocaleIds = availableLocaleIds.filter(id => !assignedLocaleIds.has(id));
  const canChangeTemplateLocale = hasTemplates && selectableLocaleIds.length > 0;
  
  const dependencyTemplates = Object.values(site.templates).filter(t => t.serviceId !== service.id);
  const hasAddableDependencies = dependencyTemplates.length > 0;

  const { activeItem, onNav } = useTagomiNav();

  const [dialogOpen, setDialogOpen] = React.useState<undefined | 
    'ServiceEdit' | 
    'NewTemplate' | 
    'TemplateEdit' | 
    'TemplateDelete' | 
    'ServiceDelete' |
    'DebugLocale' |
    'DependenciesAdd' |
    'DependenciesEdit'
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
      {dialogOpen === 'DependenciesAdd' ? <TemplateDependenciesAdd serviceId={service.id} onClose={handleDialogClose} /> : null}
      {dialogOpen === 'DependenciesEdit' ? <TemplateDependenciesEdit serviceId={service.id} onClose={handleDialogClose} /> : null}


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
        disabled={!canChangeTemplateLocale}
        onClick={() => setDialogOpen('TemplateEdit')}
        labelText={intl.formatMessage({ id: 'tagomi.service.options.template.changeLocale' })}>
      </Burger.TreeItemOption>

      <Burger.TreeItemOption nodeId={service.id + 'dependencies.add'}
        color='page'
        icon={EditIcon}
        disabled={!hasTemplates || !hasAddableDependencies}
        onClick={() => setDialogOpen('DependenciesAdd')}
        labelText={intl.formatMessage({ id: 'tagomi.service.options.template.addDependencies' })}>
      </Burger.TreeItemOption>

      <Burger.TreeItemOption nodeId={service.id + 'dependencies.edit'}
        color='page'
        icon={EditIcon}
        disabled={!hasTemplates || !hasAddableDependencies}
        onClick={() => setDialogOpen('DependenciesEdit')}
        labelText={intl.formatMessage({ id: 'tagomi.service.options.template.editDependencies' })}>
      </Burger.TreeItemOption>

      <Burger.TreeItemOption nodeId={service.id + 'pages.delete'}
        color='page'
        icon={DeleteOutlineOutlinedIcon}
        disabled={!hasTemplates}
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
