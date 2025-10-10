import React from 'react';
import { Box, Typography, Divider, List, ListItem, ListItemText } from '@mui/material';
import { SimpleTreeView } from "@mui/x-tree-view";
import ArrowDropDownIcon from "@mui/icons-material/ArrowDropDown";

import { useIntl } from 'react-intl';



//import ArticleItem, { ArticleItemOptions } from './ArticleItem';
//import { LinkEdit } from '../../stencil-link/LinkEdit';
//import { WorkflowEdit } from '../../stencil-workflow/WorkflowEdit';

import { ExplorerItemArticle, useTagomiNav } from '../tagomi-nav';
import { ServicesListRoot, useUtilityClasses } from './useUtilityClasses';
import { TagomiComposerApi as Composer } from '@dxs-ts/tagomi-api';
import { TagomiApi } from '@dxs-ts/tagomi-api';
import ServiceItem from './ServiceItem';


const findMainId = (values: string[]) => {
  const result = values.filter(id => !id.endsWith("-nested"));
  if (result.length) {
    return result[0];
  }
  return undefined;
}


const EndIcon: React.FC = () => {
  return <Box style={{ width: 24 }} />;
}

export const ServicesList: React.FC<{ searchString: string }> = ({ searchString }) => {
  const intl = useIntl();
  const { session, backend } = Composer.useComposer();
  const { getArticle, onNav } = useTagomiNav();
  const expanded = getArticle().expanded ?? [];
   // const [editWorkflow, setEditWorkflow] = React.useState<undefined | StencilApi.WorkflowId>(undefined);
  //const serviceItemOptions: ServiceItemOptions = { setEditWorkflow };

  const classes = useUtilityClasses();


  function handleExpanded(_event: React.SyntheticEvent, nodeIds: string[]) {
    const active = findMainId(expanded);
    const newId = findMainId(nodeIds.filter(n => n !== active));
    if (active !== newId && active && newId) {
      nodeIds.splice(nodeIds.indexOf(active), 1);
    }
    onNav({ type: 'SERVICES', article: active, expanded: [...nodeIds] })
  }


  const treeItems = React.useMemo(() => {
    let items = session.services;

    if (searchString) {
      items = items.filter(item =>
        item.service.serviceName.toLowerCase().includes(searchString.toLowerCase())
      );
    }

    return items.sort((a, b) => a.displayOrder - b.displayOrder);
  }, [session.services, searchString]);

  return (
    <>


      <ServicesListRoot className={classes.root}>
        <Typography className={classes.title}>{intl.formatMessage({ id: 'tagomi.main.services.all' })}</Typography>

        <SimpleTreeView expandedItems={expanded}
          slots={{ collapseIcon: ArrowDropDownIcon, expandIcon: ArrowDropDownIcon, endIcon: EndIcon }}
          onExpandedItemsChange={handleExpanded}>

          {treeItems.map(item => (<ServiceItem serviceId={item.service.id} key={item.service.id} />))}

        </SimpleTreeView>
      </ServicesListRoot>
    </>
  );
}




