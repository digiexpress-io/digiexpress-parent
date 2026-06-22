import React from 'react';
import { styled, generateUtilityClass } from '@mui/material';
import composeClasses from '@mui/utils/composeClasses';
import { FsColors } from '../fs-theme';

const MUI_NAME = 'SearchResultHighlight';

interface HighlightProps {
  text: string;
  searchTerm: string;
}

export interface SearchResultHighlightClasses {
  root: string;
  highlight: string;
}

const useUtilityClasses = (_props: HighlightProps) => {
  const slots = {
    root: ['root'],
    highlight: ['highlight'],
  };
  const getUtilityClass = (slot: string) => generateUtilityClass(MUI_NAME, slot);
  return composeClasses(slots, getUtilityClass, {});
};

const SearchResultHighlightRoot = styled('span', {
  name: MUI_NAME,
  slot: 'Root',
})(() => ({
  [`& .${MUI_NAME}-highlight`]: {
    backgroundColor: FsColors.semantic.highlightLight,
    borderRadius: '2px',
    fontWeight: 'bold',
  },
}));

export const SearchResultHighlight: React.FC<HighlightProps> = (props) => {
  const { text, searchTerm } = props;
  const classes = useUtilityClasses(props);

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
    <SearchResultHighlightRoot className={classes.root}>
      {parts.map((part, index) => {
        const isMatch = part.toLowerCase() === searchTerm.toLowerCase();
        return isMatch ? (
          <span key={index} className={classes.highlight}>
            {part}
          </span>
        ) : (
          <span key={index}>{part}</span>
        );
      })}
    </SearchResultHighlightRoot>
  );
};