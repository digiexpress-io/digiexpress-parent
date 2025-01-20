import React from 'react';
import { useThemeProps, TextField, Typography, Chip, Grid2, Link, Divider, Alert } from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import { FormattedMessage, useIntl } from 'react-intl';

import { SiteApi, useSite } from '../api-site';
import { useAnchor } from './useAnchor';
import { GLinkFormUnsecured, GLinkPhone, GPopoverButton } from '../';
import { useUtilityClasses, GPopoverSearchRoot, GSearchMuiPopover, MUI_NAME } from './useUtilityClasses';
import { GOverridableComponent } from '../g-override';
import { GLinkHyper } from '../';
import { SearchApi } from '../api-search';


export interface GPopoverSearchProps {
  itemsInColumn?: number | undefined;
  onFormLink: (target: { pageId: string, productId: string }) => void;
  pageId: SiteApi.TopicId;
  onTopic: (topic: SiteApi.TopicView, event: React.MouseEvent) => void;
  slots?: { link?: React.ElementType<GSearchResultProps> }
  component?: GOverridableComponent<GPopoverSearchProps>
}

export interface GSearchResultProps {
  children: SiteApi.TopicView
  onClick?: (
    topic: SiteApi.TopicView,
    event: React.MouseEvent<HTMLAnchorElement, MouseEvent> | React.MouseEvent<HTMLSpanElement, MouseEvent>) => void;
}

interface ResultsDividerProps {
  searchState: SearchApi.SearchState,
  title: string,
  isHidden: boolean,
  className?: string
}

const ResultsDivider: React.FC<ResultsDividerProps> = ({ searchState, title, isHidden, className }) => {
  const intl = useIntl();
  //const classes = useUtilityClasses(ownerState);

  if (isHidden) {
    return (<></>);
  }

  if (searchState.searchOptionType === 'ALL') {
    return (
      <>
        <Divider />
        <Typography className={className}>{intl.formatMessage({ id: title })}</Typography>
      </>
    )
  }
  return (<></>)
}

export const GPopoverSearch: React.FC<GPopoverSearchProps> = (initProps) => {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const intl = useIntl();
  const anchor = useAnchor();
  const classes = useUtilityClasses(props);
  const { views } = useSite();
  const noValueIndicatorColon = intl.formatMessage({ id: 'gamut.noValueIndicatorColon' });
  const [state, setState] = React.useState(SearchApi.getInstance(views, noValueIndicatorColon));

  const hasNoResults = state.topics.length === 0 &&
    state.forms.length === 0 &&
    state.phones.length === 0 &&
    state.internal.length === 0 &&
    state.external.length === 0;

  function handleOnTopic(topic: SiteApi.TopicView, event: React.MouseEvent<HTMLAnchorElement, MouseEvent> | React.MouseEvent<HTMLSpanElement, MouseEvent>) {
    props.onTopic(topic, event);
    anchor.anchorProps.onClose();
  }

  function handleFilterByType(type: SearchApi.FilterMode) {
    setState(prev => prev.filterMode(prev.searchOptionType === type ? 'ALL' : type));
  }


  const Root = props.component ?? GPopoverSearchRoot;

  return (
    <Root className={classes.root} ownerState={props}>
      <GPopoverButton onClick={anchor.onClick} label={<FormattedMessage id='gamut.buttons.search' />} icon={<SearchIcon />} />
      <GSearchMuiPopover {...anchor.anchorProps} open={anchor.anchorProps.open}>
        <div className={classes.layoutContainer}>
          <Grid2>
            <Typography className={classes.title}>{intl.formatMessage({ id: 'gamut.search.popover.title' })}</Typography>
            <Grid2 size={{ lg: 12, xl: 12 }} className={classes.titleContainer}>
              <TextField
                className={classes.inputField}
                placeholder={intl.formatMessage({ id: 'gamut.search.popover.input.placeholder' })}
                onChange={({ currentTarget }) => setState(prev => prev.find(currentTarget.value))} />
            </Grid2>

            <Grid2 size={{ lg: 3, xl: 3 }} />

            <Grid2 size={{ lg: 9, xl: 9 }} className={classes.quickSearch}>
              <Chip
                color={state.searchOptionType === 'ALL' ? 'primary' : undefined}
                label={intl.formatMessage({ id: 'gamut.search.results.allResults' })}
                onClick={() => handleFilterByType('ALL')} className={classes.quickSearchFilterItem} />
              <Chip
                color={state.searchOptionType === 'TOPICS' ? 'primary' : undefined}
                label={intl.formatMessage({ id: 'gamut.search.popover.allServices' })}
                onClick={() => handleFilterByType('TOPICS')} className={classes.quickSearchFilterItem} />
              <Chip
                color={state.searchOptionType === 'FORM_LINKS' ? 'primary' : undefined}
                label={intl.formatMessage({ id: 'gamut.search.popover.allForms' })}
                onClick={() => handleFilterByType('FORM_LINKS')} className={classes.quickSearchFilterItem} />
              <Chip
                color={state.searchOptionType === 'PHONE_LINKS' ? 'primary' : undefined}
                label={intl.formatMessage({ id: 'gamut.search.popover.allPhones' })}
                onClick={() => handleFilterByType('PHONE_LINKS')} className={classes.quickSearchFilterItem} />
              <Chip
                color={state.searchOptionType === 'LINKS' ? 'primary' : undefined}
                label={intl.formatMessage({ id: 'gamut.search.popover.allLinks' })}
                onClick={() => handleFilterByType('LINKS')} className={classes.quickSearchFilterItem} />
            </Grid2>
          </Grid2>

          <Grid2>
            <Grid2 size={{ lg: 3, xl: 3 }} />
            <Grid2 size={{ lg: 9, xl: 9 }} className={classes.resultsContainer}>
              {hasNoResults ? (
                <Alert severity='info' variant='outlined'>
                  {intl.formatMessage({ id: 'gamut.search.results.noResults' })}
                  {intl.formatMessage({ id: 'gamut.noValueIndicatorColon' })} {state.searchString}
                </Alert>
              ) : (
                <>
                  <ResultsDivider searchState={state} title='gamut.search.results.formLinks' isHidden={state.topics.length === 0} className={classes.resultsDividerTitle} />
                  {state.forms.map((form) => <GLinkFormUnsecured key={form.linkToForm.id} label={form.label} value={form.linkToForm.value}
                    onClick={() => { props.onFormLink({ pageId: form.topic.id, productId: form.linkToForm.id }) }} />)}

                  <ResultsDivider searchState={state} title='gamut.search.results.phoneLinks' isHidden={state.topics.length === 0} className={classes.resultsDividerTitle} />
                  {state.phones.map((phone) => <GLinkPhone key={phone.id} label={phone.name} value={phone.value} />)}

                    <ResultsDivider searchState={state} title='gamut.search.results.serviceLinks' isHidden={state.topics.length === 0} className={classes.resultsDividerTitle} />
                    {state.topics.map((topic) => <Link key={topic.id} onClick={(event) => handleOnTopic(topic, event)}>{topic.name}</Link>)}

                    <ResultsDivider searchState={state} title='gamut.search.results.internalExternalLinks' isHidden={state.topics.length === 0} className={classes.resultsDividerTitle} />
                    {...state.internal.map((link) => <GLinkHyper label={link.name} value={link.value} key={link.id} />)}
                    {...state.external.map((link) => <GLinkHyper label={link.name} value={link.value} key={link.id} />)}
                </>
              )}
            </Grid2>

          </Grid2>
        </div>
      </GSearchMuiPopover>

    </Root>
  );
}


