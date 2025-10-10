import React from 'react';
import { Box, Typography } from '@mui/material';
import { useIntl } from 'react-intl';

import { ArticleTimestampsTable } from './ArticleTimestampsTable';

const ArticleTimestampsView: React.FC = () => {
  const intl = useIntl();
  const title = intl.formatMessage({ id: 'article.timestamps.title' });

  return (
    <Box pl={1} pr={3}>
      <Box sx={{ display: 'inline-block' }}>
        <Box my={1}>
          <Typography variant="h1">{title}</Typography>
        </Box>

        <ArticleTimestampsTable />
      </Box>
    </Box>
  );
};

export { ArticleTimestampsView };