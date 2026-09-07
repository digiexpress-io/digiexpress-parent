import React from 'react';
import { ButtonBase, Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import {
  SentimentVerySatisfied as SentimentVerySatisfiedIcon,
  SentimentSatisfied as SentimentSatisfiedIcon,
  SentimentNeutral as SentimentNeutralIcon,
  SentimentDissatisfied as SentimentDissatisfiedIcon,
  SentimentVeryDissatisfied as SentimentVeryDissatisfiedIcon,
} from '@mui/icons-material';
import { GRouterOfferSummaryRpsRoot, GRouterOfferSummaryRpsTextField, useUtilityClasses } from './useUtilityClasses';


export interface FormFillNpsRatingProps {
  rating: number | undefined;
  comment: string;
  onRatingChange: (value: number) => void;
  onCommentChange: (value: string) => void;
}

export const FormFillNpsRating: React.FC<FormFillNpsRatingProps> = ({ rating, onRatingChange, comment, onCommentChange }) => {
  const classes = useUtilityClasses();
  const intl = useIntl();

  const faces = [
    { value: 1, Icon: SentimentVeryDissatisfiedIcon, labelKey: 'gamut.nps.rating.terrible', className: `${classes.faceItem} ${classes.faceItemTerrible}` },
    { value: 2, Icon: SentimentDissatisfiedIcon,     labelKey: 'gamut.nps.rating.poor',     className: `${classes.faceItem} ${classes.faceItemPoor}` },
    { value: 3, Icon: SentimentNeutralIcon,          labelKey: 'gamut.nps.rating.okay',     className: `${classes.faceItem} ${classes.faceItemOkay}` },
    { value: 4, Icon: SentimentSatisfiedIcon,        labelKey: 'gamut.nps.rating.good',     className: `${classes.faceItem} ${classes.faceItemGood}` },
    { value: 5, Icon: SentimentVerySatisfiedIcon,    labelKey: 'gamut.nps.rating.excellent', className: `${classes.faceItem} ${classes.faceItemExcellent}` },
  ];

  return (
    <GRouterOfferSummaryRpsRoot className={classes.root}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'gamut.nps.title' })}</Typography>
      <div className={classes.faces}>
        {faces.map(({ value, Icon, labelKey, className }) => (
          <ButtonBase key={labelKey} className={className} data-selected={rating === value} onClick={() => onRatingChange(value)}>
            <Icon />
            <Typography className={classes.faceLabel}>{intl.formatMessage({ id: labelKey })}</Typography>
          </ButtonBase>
        ))}
      </div>
      <GRouterOfferSummaryRpsTextField
        multiline
        rows={3}
        fullWidth
        value={comment}
        onChange={(event) => onCommentChange(event.target.value)}
        placeholder={intl.formatMessage({ id: 'gamut.nps.comment.placeholder' })}
      />
    </GRouterOfferSummaryRpsRoot>
  );
};
