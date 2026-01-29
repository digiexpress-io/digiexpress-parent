import React from "react";
import { Box } from '@mui/material';
import { ArticleOutlined as ArticleIcon } from '@mui/icons-material';
import { FormattedMessage } from 'react-intl';

import { TreeItem } from "@dxs-ts/eveli-primitives";
import { ScriptOptions } from './ScriptOptions';
import { TagomiComposerApi as Composer } from '@dxs-ts/tagomi-api';


interface ScriptItemProps {
  scriptId: string;
}

const ScriptItem: React.FC<ScriptItemProps> = ({ scriptId }) => {
  const { session } = Composer.useComposer();
  const resourceView = session.resources.find(r => r.resource.id === scriptId);

  if (!resourceView) {
    return null;
  }

  const scriptName = resourceView.resource.resourceName;

  return (
    <>
      <TreeItem
        itemId={scriptId}
        labelcolor="explorerItem"
        labelIcon={ArticleIcon}
        labelText={
          <Box sx={{ display: "flex", alignItems: "center", p: 0.5, pr: 0 }}>
            {scriptName}
          </Box>
        }
      >
        <TreeItem itemId={scriptId + '-options-nested'} labelText={<FormattedMessage id="options" />}>
          <ScriptOptions scriptId={scriptId} />
        </TreeItem>
      </TreeItem>
    </>
  );
}

export default ScriptItem;
