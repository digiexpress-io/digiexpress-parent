import React from 'react';
import { Typography } from '@mui/material';
import { SimpleTreeView } from "@mui/x-tree-view";
import { ArrowDropDown as ArrowDropDownIcon } from "@mui/icons-material";
import { useIntl } from 'react-intl';

import { ScriptsListRoot, useUtilityClasses } from './useUtilityClasses';
import ScriptItem from './ScriptItem';
import { TagomiComposerApi as Composer } from '@dxs-ts/tagomi-api';


const EndIcon: React.FC = () => {
  return <div style={{ width: 24 }} />;
}

export const ScriptsList: React.FC<{ searchString: string }> = ({ searchString }) => {
  const intl = useIntl();
  const { session } = Composer.useComposer();

  const scripts = React.useMemo(() => {
    return session.resources.filter(r => r.resource.contentType === 'SCRIPT');
  }, [session.resources]);

  const filteredScripts = React.useMemo(() => {
    if (!searchString) return scripts;
    return scripts.filter((script) =>
      script.resource.resourceName.toLowerCase().includes(searchString.toLowerCase())
    );
  }, [scripts, searchString]);

  const classes = useUtilityClasses();

  return (
    <ScriptsListRoot>
      <Typography className={classes.title}>
        {intl.formatMessage({ id: 'tagomi.main.scripts.all' })}
      </Typography>

      <SimpleTreeView
        slots={{ collapseIcon: ArrowDropDownIcon, expandIcon: ArrowDropDownIcon, endIcon: EndIcon }}
      >
        {filteredScripts.map(script => (<ScriptItem key={script.resource.id} scriptId={script.resource.id} />))}
      </SimpleTreeView>
    </ScriptsListRoot>
  );
}
