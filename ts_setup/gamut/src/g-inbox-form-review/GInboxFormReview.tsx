import React from 'react';
import { Avatar, Chip, useThemeProps, Dialog, Button, DialogTitle } from '@mui/material';
import DescriptionOutlinedIcon from '@mui/icons-material/DescriptionOutlined';
import { FormattedMessage } from 'react-intl';

import { GInboxFormReviewRoot, MUI_NAME, useUtilityClasses } from './useUtilityClasses';
import { GFormReview } from '../g-form-review';


export interface GInboxFormReviewProps {
  formName: string;
  formId: string;
}


export const GInboxFormReview: React.FC<GInboxFormReviewProps> = (initProps) => {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const { formName, formId } = props;
  const classes = useUtilityClasses();
  const [openReview, setOpenReview] = React.useState(false);

  function handleOpenReview() {
    setOpenReview(true)
  }

  function handleCloseReview() {
    setOpenReview(false)
  }

  return (
    <GInboxFormReviewRoot className={classes.root}>
      <Chip onClick={handleOpenReview}
        className={classes.reviewItem}
        label={formName}
        avatar={
          <Avatar className={classes.reviewAvatar}>
            <DescriptionOutlinedIcon className={classes.reviewIcon} />
          </Avatar>}
      />

      <Dialog
        onClose={handleCloseReview}
        open={openReview}
        sx={{ display: 'flex', flexDirection: 'column', color: 'blue' }}
        maxWidth='lg'
      >
        <DialogTitle>
          <FormattedMessage id='dialob.review.title' />
        </DialogTitle>

        <GFormReview formId={formId} />

        <Button variant='outlined' onClick={handleCloseReview} sx={{ mb: 1 }}>
          <FormattedMessage id='dialob.review.close' />
        </Button>
      </Dialog>
    </GInboxFormReviewRoot>
  )
}



