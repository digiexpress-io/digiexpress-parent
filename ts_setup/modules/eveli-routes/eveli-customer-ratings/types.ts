import React from 'react';
import {
  SentimentVerySatisfied as SentimentVerySatisfiedIcon,
  SentimentSatisfied as SentimentSatisfiedIcon,
  SentimentNeutral as SentimentNeutralIcon,
  SentimentDissatisfied as SentimentDissatisfiedIcon,
  SentimentVeryDissatisfied as SentimentVeryDissatisfiedIcon,
} from '@mui/icons-material';

export interface RpsComment {
  text: string;
  createdAt: string;
  rating: number;
}

export const RATING_ICONS: Record<number, React.ElementType> = {
  5: SentimentVerySatisfiedIcon,
  4: SentimentSatisfiedIcon,
  3: SentimentNeutralIcon,
  2: SentimentDissatisfiedIcon,
  1: SentimentVeryDissatisfiedIcon,
};
