import React from 'react';
import { useThemeProps, TextField, Typography, Chip, Grid2, Link, Divider, Alert, Box } from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import CircleIcon from '@mui/icons-material/Circle';
import CloseIcon from '@mui/icons-material/Close';

import { FormattedMessage, useIntl } from 'react-intl';

import { SiteApi, useSite } from '../api-site';
import { useAnchor } from './useAnchor';
import { GLinkPhone, GLogo, GPopoverButton } from '../';
import { useUtilityClasses, GPopoverSearchRoot, GSearchMuiPopover, MUI_NAME } from './useUtilityClasses';
import { GOverridableComponent } from '../g-override';
import { GLinkHyper } from '../';
import { SearchApi } from '../api-search';

import { GLinkFormUnlockedSearchResults as GLinkFormUnlockedSearchResultsUnsecured } from '../g-link/GLinkFormUnlockedSearchResultsUnsecured';
import { useIam } from '../api-iam/IamBackendProvider';


export interface GPopoverSearchProps {
  itemsInColumn?: number | undefined;
  pageId: SiteApi.TopicId;
  slots?: { topicLink?: React.ElementType<GSearchResultProps> }
  component?: GOverridableComponent<GPopoverSearchProps>

  getEnabledOptions?: () => SearchApi.FilterMode[];
  onFormLink: (target: { pageId: string, productId: string }) => void;
  onTopic: (topic: SiteApi.TopicView, event: React.MouseEvent) => void;
}

export interface GSearchResultProps {
  children: SiteApi.TopicView;
  onClick: (
    event: React.MouseEvent<HTMLAnchorElement, MouseEvent> | React.MouseEvent<HTMLSpanElement, MouseEvent>,
    topic: SiteApi.TopicView
  ) => void;
  onClose: () => void;
  
}

interface ResultsDividerProps {
  searchState: SearchApi.SearchState,
  title: string,
  isHidden: boolean,
  className?: string
}

const ResultsDivider: React.FC<ResultsDividerProps> = ({ searchState, title, isHidden, className }) => {
  const intl = useIntl();

  if (isHidden) {
    return (<></>);
  }

  return (
    <>
      <Divider className={className} />
      <Typography className={className}>{intl.formatMessage({ id: title })}</Typography>
    </>
  )
}

const DefaultLinkSlot: React.FC<GSearchResultProps> = (props) => {
  const { children: topic } = props;
  const classes = useUtilityClasses();
  const isChild: boolean = !!topic.parent;

  function handleOnTopic(
    topic: SiteApi.TopicView,
    event: React.MouseEvent<HTMLAnchorElement, MouseEvent> | React.MouseEvent<HTMLSpanElement, MouseEvent>
  ) {
    props.onClick(event, topic);
    props.onClose();
  }

  return (
    <Box className={isChild ? classes.childTopic : undefined}>
      {isChild && <CircleIcon fontSize="small" />}
      <Link onClick={(event) => handleOnTopic(topic, event)}>{topic.name}</Link>
    </Box>
  );
};



export const GPopoverSearch: React.FC<GPopoverSearchProps> = (initProps) => {
  const props = useThemeProps({
    props: initProps,
    name: MUI_NAME,
  });
  const intl = useIntl();
  const anchor = useAnchor();
  const classes = useUtilityClasses();
  const { views } = useSite();
  const iam = useIam();
  const noValueIndicatorColon = intl.formatMessage({ id: 'gamut.noValueIndicatorColon' });
  const [state, setState] = React.useState<SearchApi.SearchState>();
  

  React.useEffect(() => {
    setState(SearchApi.getInstance(views, noValueIndicatorColon));
  }, [intl.locale]);

  if (!state) {
    return (<></>)
  }

  const noResults = state.topics.length === 0 &&
    state.forms.length === 0 &&
    state.phones.length === 0 &&
    state.internal.length === 0 &&
    state.external.length === 0;

  function handleFilterByType(type: SearchApi.FilterMode) {
    setState(prev => prev!.filterMode(prev!.searchOptionType === type ? 'ALL' : type));
  }

  function isOptionEnabled(option: SearchApi.FilterMode): boolean {
    const enabledOptions = props.getEnabledOptions ? props.getEnabledOptions() : undefined;
    if(enabledOptions) {
      return enabledOptions.includes(option);
    }
    return true;
  }

  const Root = props.component ?? GPopoverSearchRoot;
  const TopicLinkSlot: React.ElementType<GSearchResultProps> = props.slots?.topicLink ? props.slots?.topicLink : DefaultLinkSlot;

  return (
    <Root className={classes.root} ownerState={props}>
      <GPopoverButton onClick={anchor.onClick} label={<FormattedMessage id='gamut.buttons.search' />} icon={<SearchIcon />} />
      <GSearchMuiPopover
        {...anchor.anchorProps}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
        transformOrigin={{ vertical: 'top', horizontal: 'center' }}
        open={anchor.anchorProps.open}
        marginThreshold={0}
      >
        <Box className={classes.logoBox}>
          <GLogo variant='black_sm' />
          <Box onClick={() => anchor.anchorProps.onClose()}><CloseIcon /></Box>
        </Box>

        <div className={classes.layoutContainer}>
          <Grid2>
            <Grid2 className={classes.titleContainer}>
              <Typography className={classes.title}>
                {intl.formatMessage({ id: 'gamut.search.popover.title' })}
              </Typography>
              <TextField
                className={classes.inputField}
                placeholder={intl.formatMessage({ id: 'gamut.search.popover.input.placeholder' })}
                onChange={({ currentTarget }) => setState(prev => prev!.find(currentTarget.value))}
              />
            </Grid2>

            <Grid2 size={{ lg: 3, xl: 3 }} />

            <Grid2 size={{ lg: 9, xl: 9 }} className={classes.quickSearch}>
              { isOptionEnabled('ALL') && <Chip
                color={state.searchOptionType === 'ALL' ? 'primary' : undefined}
                label={intl.formatMessage({ id: 'gamut.search.results.allResults' })}
                onClick={() => handleFilterByType('ALL')} className={classes.quickSearchFilterItem} />
              }
              { isOptionEnabled('FORM_LINKS') && <Chip
                color={state.searchOptionType === 'FORM_LINKS' ? 'primary' : undefined}
                label={intl.formatMessage({ id: 'gamut.search.popover.allForms' })}
                onClick={() => handleFilterByType('FORM_LINKS')} className={classes.quickSearchFilterItem} />
              }
              { isOptionEnabled('TOPICS') && <Chip
                color={state.searchOptionType === 'TOPICS' ? 'primary' : undefined}
                label={intl.formatMessage({ id: 'gamut.search.popover.allServices' })}
                onClick={() => handleFilterByType('TOPICS')} className={classes.quickSearchFilterItem} />
              }
              { isOptionEnabled('PHONE_LINKS') && <Chip
                color={state.searchOptionType === 'PHONE_LINKS' ? 'primary' : undefined}
                label={intl.formatMessage({ id: 'gamut.search.popover.allPhones' })}
                onClick={() => handleFilterByType('PHONE_LINKS')} className={classes.quickSearchFilterItem} />
              }
              { isOptionEnabled('LINKS') && <Chip
                color={state.searchOptionType === 'LINKS' ? 'primary' : undefined}
                label={intl.formatMessage({ id: 'gamut.search.popover.allLinks' })}
                onClick={() => handleFilterByType('LINKS')} className={classes.quickSearchFilterItem} />
              }
            </Grid2>
          </Grid2>

          <Grid2>
            <Grid2 size={{ lg: 3, xl: 3 }} />
            <Grid2 size={{ lg: 9, xl: 9 }} className={classes.resultsContainer}>
              {noResults ? (
                <Alert severity='info' variant='outlined'>
                  {intl.formatMessage({ id: 'gamut.search.results.noResults' })}
                  {intl.formatMessage({ id: 'gamut.noValueIndicatorColon' })} {state.searchString}
                </Alert>
              ) : (
                <>
                    <ResultsDivider searchState={state} title='gamut.search.results.serviceLinks' className={classes.resultsDividerTitle} isHidden={state.topics.length === 0} />
                    {
                      state.topics.map((topic) => (
                        <TopicLinkSlot 
                          key={topic.id}
                          onClose={anchor.anchorProps.onClose} 
                          onClick={(event, topic) => {
                            props.onTopic(topic, event);
                          }}>
                            {topic}
                        </TopicLinkSlot>))
                    }

                    <ResultsDivider searchState={state} title='gamut.search.results.formLinks' className={classes.resultsDividerTitle} isHidden={state.forms.length === 0} />

                    {state.forms.map((form) => {
                      const authType = iam.getFormLinkAuthType(form.linkToForm);

                      return (
                        <GLinkFormUnlockedSearchResultsUnsecured
                          key={form.linkToForm.id}
                          label={form.linkToForm.name}
                          value={form.linkToForm.value}
                          formLinkAuthType={authType}
                          onClick={() => {
                            props.onFormLink({ pageId: form.topic.id, productId: form.linkToForm.id });
                          }}
                        />
                      );
                    })}
                    <ResultsDivider searchState={state} title='gamut.search.results.phoneLinks' className={classes.resultsDividerTitle} isHidden={state.phones.length === 0} />
                    {state.phones.map((phone) => (<GLinkPhone key={phone.id} label={phone.name} value={phone.value} />))}

                    <ResultsDivider searchState={state} title='gamut.search.results.internalExternalLinks' className={classes.resultsDividerTitle}
                      isHidden={state.external.length === 0 && state.internal.length === 0}
                    />
                    {...state.internal.map((link) => (<GLinkHyper label={link.name} value={link.value} key={link.id} />))}
                    {...state.external.map((link) => (<GLinkHyper label={link.name} value={link.value} key={link.id} />))}
                </>
              )}
            </Grid2>

          </Grid2>
        </div>
      </GSearchMuiPopover>

    </Root>
  );
}


