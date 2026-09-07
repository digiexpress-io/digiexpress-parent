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


const faces: { Icon: React.ElementType, labelKey: string }[] = [
  { Icon: SentimentVeryDissatisfiedIcon, labelKey: 'gamut.nps.rating.terrible' },
  { Icon: SentimentDissatisfiedIcon,  labelKey: 'gamut.nps.rating.poor'      },
  { Icon: SentimentNeutralIcon,       labelKey: 'gamut.nps.rating.okay'      },
  { Icon: SentimentSatisfiedIcon,     labelKey: 'gamut.nps.rating.good'      },
  { Icon: SentimentVerySatisfiedIcon, labelKey: 'gamut.nps.rating.excellent' },
];


export const FormFillNpsRating: React.FC = () => {
  const classes = useUtilityClasses();
  const intl = useIntl();

  return (
    <GRouterOfferSummaryRpsRoot className={classes.root}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'gamut.nps.title' })}</Typography>
      <div className={classes.faces}>
        {faces.map(({ Icon, labelKey }) => (
          <ButtonBase key={labelKey} className={classes.faceItem}>
            <Icon />
            <Typography className={classes.faceLabel}>{intl.formatMessage({ id: labelKey })}</Typography>
          </ButtonBase>
        ))}
      </div>
      <GRouterOfferSummaryRpsTextField multiline rows={3} fullWidth placeholder={intl.formatMessage({ id: 'gamut.nps.comment.placeholder' })} />
    </GRouterOfferSummaryRpsRoot>
  );
};
