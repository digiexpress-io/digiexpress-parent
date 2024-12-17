import React from 'react';
import { Avatar, Chip, useThemeProps, Dialog, Button, DialogTitle, Box, IconButton, DialogContent, Typography, Divider } from '@mui/material';
import DescriptionOutlinedIcon from '@mui/icons-material/DescriptionOutlined';
import CloseIcon from '@mui/icons-material/Close';
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

      <Dialog open={openReview} maxWidth='lg'>
        <DialogTitle>
          <Box display='flex'>
            <Typography alignContent='center'>
              <FormattedMessage id='dialob.review.title' />
              <FormattedMessage id='gamut.textSeparator' />
              {props.formName}
            </Typography>
            <Box flexGrow={1} />
            <IconButton onClick={handleCloseReview}><CloseIcon /></IconButton>
          </Box>
        </DialogTitle>
        <Divider />

        <DialogContent>
          <GFormReview formId={formId} />
        </DialogContent>

        <Button variant='contained' className={classes.closeButton} onClick={handleCloseReview}>
          <FormattedMessage id='dialob.review.button.close' />
        </Button>
      </Dialog>
    </GInboxFormReviewRoot>
  )
}



