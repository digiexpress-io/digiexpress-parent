import React from 'react';
import { Typography, Box } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { useUtilityClasses } from './useUtilityClasses';


export interface FsPropertiesWorkflowProps {
  dirent: Fs.DirentBase;
}

export const FsPropertiesWorkflow: React.FC<FsPropertiesWorkflowProps> = ({ dirent }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const { getArticleName, selectOptions } = useFsDirent();

  if (dirent.type !== 'ARTICLE_WORKFLOW') {
    return undefined;
  }
  const workflowProps = dirent.props as Fs.WorkflowProps | undefined;
  const locales = Object.keys(workflowProps?.intlValues ?? {}).map((localeId) => {
    const lang = selectOptions.languages.find((l) => l.value === localeId);
    return lang?.label ?? localeId;
  });

  return (
    <>
      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.serviceName' })}</Typography>
        <Typography className={classes.propertyValue}>{workflowProps?.serviceName}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.serviceLocales' })}</Typography>
        <div className={classes.propertyList}>
          {locales.map((locale, index) => <Box key={index} className={classes.propertyListItem}>{locale}</Box>)}
        </div>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.serviceValidityStart' })}</Typography>
        <Typography className={classes.propertyValue}>{workflowProps?.validityStart}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.serviceValidityEnd' })}</Typography>
        <Typography className={classes.propertyValue}>{workflowProps?.validityEnd}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.dialobFormName' })}</Typography>
        <Typography className={classes.propertyValue}>{workflowProps?.dialobFormName}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.dialobFormTag' })}</Typography>
        <Typography className={classes.propertyValue}>{workflowProps?.dialobFormTag}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.flowName' })}</Typography>
        <Typography className={classes.propertyValue}>{workflowProps?.flowName}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.selectedArticles' })}</Typography>
        <div className={classes.propertyList}>
          {(workflowProps?.articles ?? []).map((article, index) => <Box key={index} className={classes.propertyListItem}>{getArticleName(article)}</Box>)}
        </div>
      </div>
    </>
  );
};
