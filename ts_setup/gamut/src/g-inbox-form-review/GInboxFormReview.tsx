import React from 'react';
import { Avatar, Chip, useThemeProps } from '@mui/material';
import DescriptionOutlinedIcon from '@mui/icons-material/DescriptionOutlined';
import { GInboxFormReviewRoot, MUI_NAME, useUtilityClasses } from './useUtilityClasses';


export interface GInboxFormReviewProps {
  name: string;
  subjectId: string;
  onClick: (subjectId: string) => void;
}


export const GInboxFormReview: React.FC<GInboxFormReviewProps> = (initProps) => {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const { name, onClick, subjectId } = props;
  const classes = useUtilityClasses();

  return (
    <GInboxFormReviewRoot className={classes.root}>
      <Chip onClick={() => onClick(subjectId)}
        className={classes.reviewItem}
        label={name}
        avatar={
          <Avatar className={classes.reviewAvatar}>
            <DescriptionOutlinedIcon className={classes.reviewIcon} />
          </Avatar>}
      />
    </GInboxFormReviewRoot>
  )
}



