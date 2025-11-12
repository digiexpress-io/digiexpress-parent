import React from 'react';
import { Typography, IconButton, Fade, MenuItem, ListItemIcon, Button } from '@mui/material';
import { ExpandMore as ExpandMoreIcon } from '@mui/icons-material';
import { Check as CheckIcon } from '@mui/icons-material';
import { ChevronRight as ChevronRightIcon } from '@mui/icons-material';
import { ChevronLeft as ChevronLeftIcon } from '@mui/icons-material';

import { FormattedMessage } from 'react-intl';

import { GFormStepper } from '../g-form-stepper'
import {
  useThemeInfra,
  GFormPageRoot, GFormPageTitle, GFormPageSubTitle,
  GFormPageBody, GFormPageHeader, GFormPageMenu, GFormPageFooter
} from './useUtilityClasses';

export interface GFormPageClasses {
  root: string;
}
export type GFormPageClassKey = keyof GFormPageClasses;

export interface GFormPageProps {
  id: string;
  proceedAllowed: boolean;
  completeAllowed: boolean;
  disabled: boolean;
  pageNumber: number; // starts from 1..n
  pages: { id: string; title: string | undefined, pageNumber: number }[];
  onChangePage: (pageId: string) => void;
  onNextPage: () => void;
  onPreviousPage: () => void;
  onComplete: () => void;
  onCancel: () => void;
  title: string | undefined;
  subTitle: string | undefined;
  description: string | undefined;
  children: React.ReactNode;
  active: boolean;
  slots?: {
    header: React.ElementType<GFormPageProps>;
    body: React.ElementType<GFormPageProps>;
    footer: React.ElementType<GFormPageProps>;
  };

  component?: React.ElementType<GFormPageProps>;
}

export const GFormPage: React.FC<GFormPageProps> = (initProps) => {
  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);
  const { ownerState, classes, props } = useThemeInfra(initProps);

  function handlePageChange(id: string) {
    setAnchorEl(null);
    props.onChangePage(id);
  }

  function togglePageSelection(event: React.MouseEvent<HTMLElement>) {
    setAnchorEl((prev) => prev ? null : event.currentTarget);
  }

  function handleNextPage() {
    window.scrollTo(0, 0);
    props.onNextPage();
  }

  function handlePreviousPage() {
    window.scrollTo(0, 0);
    props.onPreviousPage();
  }

  function handleComplete() {
    props.onComplete();
  }

  function handleCancel() {
    props.onCancel()
  }

  if (!props.active) {
    return (<></>)
  }


  return (
    <GFormPageRoot ownerState={ownerState} as={ownerState.component} className={classes.root}>
      <GFormPageHeader ownerState={ownerState} className={classes.header} as={ownerState.slots?.header}>
        <div className={classes.titles}>
          <GFormPageTitle ownerState={ownerState} className={classes.title}>
            <Typography>{props.title}</Typography>
          </GFormPageTitle>

          {/** Title to the next page */}
          <GFormPageSubTitle ownerState={ownerState} className={classes.subTitle}>
            <Typography>{props.subTitle}</Typography>

            {/** Page selection */}
            <IconButton onClick={togglePageSelection}><ExpandMoreIcon /></IconButton>

            <GFormPageMenu anchorEl={anchorEl} open={open} onClose={togglePageSelection} TransitionComponent={Fade} ownerState={ownerState}>

              {props.pages.map(({ id: key, title: value, pageNumber }) => {
                const selected = key === value;
                const prefix = selected && <CheckIcon color='primary' fontSize='small' sx={{ mr: 1 }} />;

                return (<MenuItem key={key} value={key} onClick={() => handlePageChange(key)}>
                  <ListItemIcon><FormattedMessage id='gamut.forms.page.selection.key' values={{ stepNumber: pageNumber, totalSteps: props.pages.length }} /></ListItemIcon>
                  <Typography>{value}</Typography>
                </MenuItem>);
              })}
            </GFormPageMenu>

          </GFormPageSubTitle>
        </div>

        <GFormStepper id={props.id} pageNumber={props.pageNumber} totalPages={props.pages.length} />
      </GFormPageHeader>

      <GFormPageBody ownerState={ownerState} className={classes.body} as={ownerState.slots?.body}>
        {props.children}
      </GFormPageBody>

      <GFormPageFooter ownerState={ownerState} className={classes.footer} as={ownerState.slots?.footer}>

        {!props.disabled && (
          <Button onClick={handleCancel} variant='outlined' color='primary'>
            <FormattedMessage id='gamut.forms.page.cancel' />
          </Button>)
        }

        {props.pages.length > 1 &&
          <Button disabled={props.pageNumber === 1} variant='outlined' onClick={handlePreviousPage} startIcon={<ChevronLeftIcon />}>
            <FormattedMessage id='gamut.forms.page.previous' />
          </Button>
        }

        {props.pages.length > 1 &&
          <Button disabled={props.pageNumber === props.pages.length || !props.proceedAllowed} variant='contained' onClick={handleNextPage} endIcon={<ChevronRightIcon />}>
            <FormattedMessage id='gamut.forms.page.next' />
          </Button>
        }

        {!props.disabled && (
          <Button disabled={props.pageNumber !== props.pages.length} onClick={handleComplete} variant='contained' color='primary'>
            <FormattedMessage id='gamut.forms.page.complete' />
          </Button>)
        }

      </GFormPageFooter>
    </GFormPageRoot >
  )
}

