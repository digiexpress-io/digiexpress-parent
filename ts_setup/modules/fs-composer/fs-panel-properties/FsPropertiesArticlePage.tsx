import React from 'react';
import { Typography, Box } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { useUtilityClasses } from './useUtilityClasses';


export interface FsPropertiesArticlePageProps {
  direntId: string;
}

export const FsPropertiesArticlePage: React.FC<FsPropertiesArticlePageProps> = ({ direntId }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const { getDirentName, getDirent } = useFsDirent();

  const dirent = getDirent(direntId);

  if (!dirent || dirent.type !== 'ARTICLE_PAGE') {
    return undefined;
  }

  const pageProps = dirent.props as Fs.PageProps;
  const configOptionsEnabled = dirent.props?.configOptions ?? [];
  const articleName = getDirentName(pageProps.articleId) ?? pageProps.articleId;
  const localeName = getDirentName(pageProps.localeCode) ?? pageProps.localeCode;

  return (
    <>
      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.configOptionsEnabled' })}</Typography>
        <div className={classes.propertyList}>
          {configOptionsEnabled.map((option, index) => (
            <Box key={index} className={classes.configOptionsListItem}>
              {intl.formatMessage({ id: `fs.dirent.configOption.${option}` })}
            </Box>
          ))}
        </div>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.article' })}</Typography>
        <Typography className={classes.propertyValue}>{articleName}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.localeCode' })}</Typography>
        <Typography className={classes.propertyValue}>{localeName}</Typography>
      </div>
    </>
  );
};
