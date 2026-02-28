import React from 'react';
import { Typography } from '@mui/material';
import { OwnerState } from './useOwnerState';

export interface FsMainLeftProps {
  ownerState: OwnerState;
  className: string;
}

export const FsMainLeft: React.FC<FsMainLeftProps> = ({ className, ownerState }) => {
  const activeTab = ownerState.openTabs[ownerState.activeTabIndex];

  return (
    <div className={className}>
      {activeTab ? (
        <Typography>
          {activeTab.node.name}
          At vero eos et accusamus et iusto odio dignissimos ducimus qui blanditiis praesentium voluptatum deleniti atque
          corrupti quos dolores et quas molestias excepturi sint occaecati cupiditate non provident, similique sunt in culpa
          qui officia deserunt mollitia animi, id est laborum et dolorum fuga. Et harum quidem rerum facilis est et expedita distinctio.
          Nam libero tempore, cum soluta nobis est eligendi optio cumque nihil impedit quo minus id quod maxime placeat facere possimus,
          omnis voluptas assumenda est, omnis dolor repellendus. Temporibus autem quibusdam et aut officiis debitis aut rerum necessitatibus saepe
          eveniet ut et voluptates repudiandae sint et molestiae non recusandae. Itaque earum rerum hic tenetur a sapiente delectus, ut aut
          reiciendis voluptatibus maiores alias consequatur aut perferendis doloribus asperiores repellat.
        </Typography>
      ) : (
        'No asset selected'
      )}

    </div>
  );
};

