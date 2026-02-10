import React from 'react';
import { Box } from '@mui/material';
import { TreeColors } from '../tree-theme';

interface HighlightProps {
  text: string;
  searchTerm: string;
  isDarkMode: boolean
}

export const SearchResultHighlight: React.FC<HighlightProps> = ({ text, searchTerm, isDarkMode }) => {

  if (!searchTerm.trim()) {
    return <>{text}</>;
  }

  const sanitizedText = text.toLowerCase();
  const sanitizedSearchTerm = searchTerm.toLowerCase();
  const parts: string[] = [];
  let lastIndex = 0;
  let index = sanitizedText.indexOf(sanitizedSearchTerm);

  while (index !== -1) {
    if (index > lastIndex) {
      parts.push(text.substring(lastIndex, index));
    }
    parts.push(text.substring(index, index + searchTerm.length));
    lastIndex = index + searchTerm.length;
    index = sanitizedText.indexOf(sanitizedSearchTerm, lastIndex);
  }

  if (lastIndex < text.length) {
    parts.push(text.substring(lastIndex));
  }



  return (
    <>
      {parts.map((part, index) => {
        const isMatch = part.toLowerCase() === searchTerm.toLowerCase();
        return isMatch ? (
          <Box key={index} component='span'
            sx={{
              backgroundColor: isDarkMode ? TreeColors.semantic.highlightDark : TreeColors.semantic.highlightLight,
              borderRadius: '2px',
              fontWeight: 'bold'
            }}
          >
            {part}
          </Box>
        ) : (
          <span key={index}>{part}</span>
        );
      })}
    </>
  );
};