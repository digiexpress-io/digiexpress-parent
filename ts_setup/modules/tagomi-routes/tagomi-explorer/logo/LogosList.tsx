import React from 'react';
import { Typography } from '@mui/material';
import { SimpleTreeView } from "@mui/x-tree-view";
import { ArrowDropDown as ArrowDropDownIcon } from "@mui/icons-material";
import { useIntl } from 'react-intl';

import { LogosListRoot, useUtilityClasses } from './useUtilityClasses';
import LogoItem from './LogoItem';
import { TagomiComposerApi as Composer } from '@dxs-ts/tagomi-api';


const EndIcon: React.FC = () => {
  return <div style={{ width: 24 }} />;
}

export const LogosList: React.FC<{ searchString: string }> = ({ searchString }) => {
  const intl = useIntl();
  const { session } = Composer.useComposer();

  const logos = React.useMemo(() => {
    return session.resources.filter(r => r.resource.contentType === 'LOGO');
  }, [session.resources]);

  const filteredLogos = React.useMemo(() => {
    if (!searchString) return logos;
    return logos.filter((logo) =>
      logo.resource.resourceName.toLowerCase().includes(searchString.toLowerCase())
    );
  }, [logos, searchString]);

  const classes = useUtilityClasses();

  return (
    <LogosListRoot>
      <Typography className={classes.title}>
        {intl.formatMessage({ id: 'tagomi.main.logos.all' })}
      </Typography>

      <SimpleTreeView
        slots={{ collapseIcon: ArrowDropDownIcon, expandIcon: ArrowDropDownIcon, endIcon: EndIcon }}
      >
        {filteredLogos.map(logo => (<LogoItem key={logo.resource.id} logoId={logo.resource.id} />))}
      </SimpleTreeView>
    </LogosListRoot>
  );
}
