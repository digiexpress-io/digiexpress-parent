import React from "react";
import { Box } from '@mui/material';
import { Image as ImageIcon } from '@mui/icons-material';
import { FormattedMessage } from 'react-intl';

import { TreeItem } from "@dxs-ts/eveli-primitives";
import { LogoOptions } from './LogoOptions';
import { TagomiComposerApi as Composer } from '@dxs-ts/tagomi-api';


interface LogoItemProps {
  logoId: string;
}

const LogoItem: React.FC<LogoItemProps> = ({ logoId }) => {
  const { site } = Composer.useComposer();
  const logo = Object.values(site.resources).find(r => r.id === logoId);

  if (!logo) {
    return null;
  }

  const logoName = logo.resourceName;

  return (
    <>
      <TreeItem
        itemId={logoId}
        labelcolor="explorerItem"
        labelIcon={ImageIcon}
        labelText={
          <Box sx={{ display: "flex", alignItems: "center", p: 0.5, pr: 0 }}>
            {logoName}
          </Box>
        }
      >
        <TreeItem itemId={logoId + '-options-nested'} labelText={<FormattedMessage id="options" />}>
          <LogoOptions logoId={logoId} />
        </TreeItem>
      </TreeItem>
    </>
  );
}

export default LogoItem;
