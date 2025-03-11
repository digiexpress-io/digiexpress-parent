import React from 'react';
import { Box, Typography } from '@mui/material';
import { SimpleTreeView } from "@mui/x-tree-view";
import ArrowDropDownIcon from "@mui/icons-material/ArrowDropDown";
import { useIntl } from 'react-intl';

import { Composer } from '../../context';
import ServiceItem from './ServiceItem';
import TreeViewToggle from '../TreeViewToggle';
import { ServicesListRoot, useUtilityClasses } from './useUtilityClasses';
import { useWrenchNav } from '../../nav';

const EndIcon: React.FC = () => {
  return <Box style={{ width: 24 }} />;
}

const ServicesList: React.FC<{}> = () => {
  const intl = useIntl();
  const { session } = Composer.useComposer();
  const classes = useUtilityClasses();
  const { onNav, getServices } = useWrenchNav();
  const expanded: string[] = getServices().expanded ?? [];

  function handleExpanded(_event: React.SyntheticEvent, nodeIds: string[]) {
    const nodes = new TreeViewToggle().onNodeToggle(nodeIds);
    onNav({ type: 'SERVICES', id: nodes.main, expanded: [...nodes.expanded] })
  }

  return (
    <ServicesListRoot className={classes.root}>
      <Typography className={classes.title} variant='h1'>{intl.formatMessage({ id: 'main.services.all' })}</Typography>

      <SimpleTreeView expandedItems={expanded}
        slots={{ collapseIcon: ArrowDropDownIcon, expandIcon: ArrowDropDownIcon, endIcon: EndIcon }}
        onExpandedItemsChange={handleExpanded}
        >
        {Object.values(session.site.services)
          .sort((a, b) => (a.ast ? a.ast.name : a.id).localeCompare((b.ast ? b.ast.name : b.id)))
          .map(service => (<ServiceItem key={service.id} serviceId={service.id} />))
        }
      </SimpleTreeView>
    </ServicesListRoot>
  );
}

export { ServicesList };

