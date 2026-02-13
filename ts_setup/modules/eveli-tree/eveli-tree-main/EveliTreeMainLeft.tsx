import React from 'react';
import { Box, styled, Typography } from '@mui/material';
import { useEveliTree } from '../../eveli-tree-api';
import { TreeColors } from '../tree-theme';

export const EveliTreeMainLeft: React.FC = () => {
  const { isDarkMode, activeTabIndex, openTabs } = useEveliTree();

  const activeTab = openTabs[activeTabIndex];

  return (
    <LeftPanel isDarkMode={isDarkMode}>
      <Box p={2}>
        {activeTab ? (
          <Typography variant='subtitle2' fontWeight={500}>
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
      </Box>
    </LeftPanel>
  );
};

const LeftPanel = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'isDarkMode'
})<{ isDarkMode: boolean }>(({ isDarkMode }) => ({
  flex: 1,
  height: '100%',
  backgroundColor: isDarkMode ? TreeColors.dark.background : TreeColors.light.background,
  overflow: 'auto'
}));