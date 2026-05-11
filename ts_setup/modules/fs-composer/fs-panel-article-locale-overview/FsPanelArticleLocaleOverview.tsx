import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsColors, FsIcon, FsIcons } from '../fs-theme';
import { FsPanel } from '../fs-panel';
import { FsPanelArticleLocaleOverviewProps } from './FsPanelArticleLocaleOverviewProps';
import { useOwnerState } from './useOwnerState';
import { FsPanelArticleLocaleOverviewRoot, useUtilityClasses } from './useUtilityClasses';


export const FsPanelArticleLocaleOverview: React.FC<FsPanelArticleLocaleOverviewProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsPanel
      title={intl.formatMessage({ id: 'fs.articleLocaleOverview.title' })}
      icon={<FsIcon icon={FsIcons.Language} large />}
      activeDirent={true}
    >
      <FsPanelArticleLocaleOverviewRoot className={classes.root} ownerState={ownerState}>
        <Typography className={classes.desc}>{intl.formatMessage({ id: 'fs.articleLocaleOverview.col.article.desc' })}</Typography>

        <div className={classes.header}>
          <Typography className={classes.name}>{intl.formatMessage({ id: 'fs.articleLocaleOverview.col.article' })}</Typography>
          {ownerState.locales.map((locale) => (
            <Typography key={locale.value} className={classes.localeCell}>{locale.label}</Typography>
          ))}
        </div>
        <div className={classes.container}>
          {ownerState.articles.map((article) => (
            <div key={article.id} className={classes.row}>
              <Typography className={classes.name}>{ownerState.getArticleName(article.id)}</Typography>
              {ownerState.locales.map((locale) => (
                <div key={locale.value} className={classes.localeCell}>
                  {ownerState.isPageInLocale(article.id, locale.value) ? <FsIcon icon={FsIcons.Checkmark} small color={FsColors.semantic.success} /> : "--"}
                </div>
              ))}
            </div>
          ))}
        </div>
      </FsPanelArticleLocaleOverviewRoot>
    </FsPanel>
  );
};
