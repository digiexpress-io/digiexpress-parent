import React from 'react';
import { Box, Typography } from '@mui/material';
import { useIntl } from 'react-intl';

import { useStencilNav } from '@/stencil-nav';
import { CancelButton } from '@/eveli-styles';
import { ArticleTimestampsTable } from './ArticleTimestampsTable';

const ArticleTimestampsView: React.FC = () => {
    const { onTabCurrentClose } = useStencilNav();
    const intl = useIntl();
    const title = intl.formatMessage({ id: 'article.timestamps.title' });

    return (
        <Box pl={1} pr={3}>
            <Box sx={{ display: 'inline-block' }}>
                <Box display="flex" alignItems="center" my={1}>
                    <Box>
                        <Typography variant="h1">{title}</Typography>
                    </Box>
                    <Box flexGrow={1} />
                    <Box display="flex" gap={1}>
                        <CancelButton onClick={() => onTabCurrentClose()} />
                    </Box>
                </Box>

                <ArticleTimestampsTable />
            </Box>
        </Box>
    );
};

export { ArticleTimestampsView };
