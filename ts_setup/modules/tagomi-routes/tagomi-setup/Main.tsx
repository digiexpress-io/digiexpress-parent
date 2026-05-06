import React from 'react';
import { Box } from '@mui/material';
import { TagomiComposerApi } from '@dxs-ts/tagomi-api';
import { ExplorerItem, useTagomiNav } from '../tagomi-nav';
import { ServicesView } from '../tagomi-service';
import { TemplateEditor } from '../tagomi-template';
import { ImagesView } from '../tagomi-images';
import { ScriptsView } from '../tagomi-scripts';
import { EveliSpinner } from '@dxs-ts/eveli-primitives';

const root = { height: `100%`, padding: 1, backgroundColor: "primary.contrastText" };

const Main: React.FC<{}> = () => {
  const site = TagomiComposerApi.useSite();
  const { activeItem } = useTagomiNav();

  return React.useMemo(() => {
    if (!site.commitId) {
      return (<Box><EveliSpinner message={'No commits found'} /></Box>);
    }
    if (!activeItem) {
      return (<Box sx={root}></Box>)
    }
    const explorer: ExplorerItem | undefined = activeItem;
    if (explorer?.type === 'SERVICE_TEMPLATES') {

      const serviceId = explorer.service;
      const localeId = explorer.locale1;

      const template = Object.values(site.templates)
        .filter(template => template.serviceId === serviceId)
        .filter(template => template.localeId === localeId)
      if (template.length === 0) {
        return (<>Template not found!!!</>)
      }

      return <TemplateEditor serviceId={serviceId} templateId={template[0].id} />
    }

    if (explorer?.type === 'IMAGES') {
      return (<Box sx={root}><ImagesView /></Box>)
    }

    if (explorer?.type === 'SCRIPTS') {
      return (<Box sx={root}><ScriptsView /></Box>)
    }

    return (<Box sx={root}><ServicesView /></Box>)
  }, [activeItem, site]);
}
export { Main }


