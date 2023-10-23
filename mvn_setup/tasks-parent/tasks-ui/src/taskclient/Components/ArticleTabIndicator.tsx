import React from 'react';
import { useTheme } from '@mui/material';
import { Document } from '../composer-types';
import { useComposer } from 'context/hooks';

const ArticleTabIndicator: React.FC<{ entity: Document }> = ({ entity }) => {
  const theme = useTheme();
  const { isDocumentSaved } = useComposer();
  const saved = isDocumentSaved(entity);
  return <span style={{
    paddingLeft: "5px",
    fontSize: '30px',
    color: theme.palette.explorerItem.contrastText,
    display: saved ? "none" : undefined
  }}>*</span>
}


export default ArticleTabIndicator;