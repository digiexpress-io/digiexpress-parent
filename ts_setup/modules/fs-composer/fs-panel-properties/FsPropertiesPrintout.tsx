import React from 'react';
import { Typography, Box } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { FsIcon, FsIcons } from '../fs-theme';
import { useUtilityClasses } from './useUtilityClasses';


export interface FsPropertiesPrintoutProps {
  dirent: Fs.DirentBase;
  children: Fs.DirentBase[];
}

export const FsPropertiesPrintout: React.FC<FsPropertiesPrintoutProps> = ({ dirent, children }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const { selectOptions } = useFsDirent();

  if (dirent.type !== 'PRINTOUT') {
    return undefined;
  }

  const printoutProps = dirent.props as Fs.PrintoutProps;
  const locales = Object.keys(printoutProps.intlValues ?? {}).map((localeId) => {
    const lang = selectOptions.languages.find((l) => l.value === localeId);
    return lang?.label ?? localeId;
  });

  return (
    <>
      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.printoutServiceName' })}</Typography>
        <Typography className={classes.propertyValue}>{printoutProps?.printoutServiceName}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.orchestratorName' })}</Typography>
        <Typography className={classes.propertyValue}>{printoutProps?.orchestratorName}</Typography>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.locales' })}</Typography>
        <div className={classes.propertyList}>
          {locales.map((locale, index) => <Box key={index} className={classes.propertyListItem}>{locale}</Box>)}
        </div>
      </div>

      <div className={classes.propertyRow}>
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.children' })}</Typography>
        <div className={classes.commentList}>
          {children.map((child) => (
            <div key={child.id} className={classes.childRow}>
              <FsIcon small icon={FsIcons.Pdf} />
              <Typography className={classes.propertyValue}>{child.name}</Typography>
            </div>
          ))}
        </div>
      </div>
    </>
  );
};
