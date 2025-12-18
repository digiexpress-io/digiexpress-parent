import React from "react";
import { Box, Typography } from "@mui/material";
import { FormattedMessage } from 'react-intl';
import { TreeItemRoot, TreeItem } from "@dxs-ts/eveli-primitives";
import { DateTime } from "luxon";

interface InfoTreeItemProps {
  nodeId: string;
  lastUpdated: string;
}

const InfoTreeItem: React.FC<InfoTreeItemProps> = ({ nodeId, lastUpdated }) => {
  const formattedDate = DateTime.fromISO(lastUpdated, { zone: 'UTC' }).setZone('local').setLocale('fi').toFormat('d.M.yyyy HH:mm');

  return (
    <TreeItem 
      itemId={nodeId + 'info-nested'}
      labelText={<FormattedMessage id="wrench.explorer.info" />}
      labelcolor="text.secondary">
      
      {formattedDate && (
        <TreeItemRoot
          itemId={nodeId + 'info-last-modified'}
          label={
            <Box sx={{ display: "flex", alignItems: "center", p: 0.5, pr: 0 }}>
              <Typography variant="body2" color="text.secondary" sx={{ pl: 1 }}>
                <FormattedMessage id="wrench.explorer.info.lastModified" values={{ value: formattedDate }} />
              </Typography>
            </Box>
          }
        />
      )}
    </TreeItem>
  );
};

export default InfoTreeItem;