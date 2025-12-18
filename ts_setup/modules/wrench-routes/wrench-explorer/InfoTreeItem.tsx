import React from "react";
import { Box, Typography } from "@mui/material";
import { FormattedMessage } from 'react-intl';
import { TreeItemRoot } from "@dxs-ts/eveli-primitives";
import { DateTime } from "luxon";

interface InfoTreeItemProps {
  nodeId: string;
  lastUpdated: string;
}

const InfoTreeItem: React.FC<InfoTreeItemProps> = ({ nodeId, lastUpdated }) => {
  const formattedDate = DateTime.fromISO(lastUpdated, { zone: 'UTC' }).setZone('local').setLocale('fi').toFormat('d.M.yyyy HH:mm');

  return (
    <TreeItemRoot
      itemId={nodeId + 'info-last-modified'}
      label={
        <Box sx={{ p: 0.5, pr: 0 }}>
          <Typography>
            <FormattedMessage id="wrench.explorer.info.lastModified" values={{ value: formattedDate }} />
          </Typography>
        </Box>
      }
    />
  );
};

export default InfoTreeItem;