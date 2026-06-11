import React from 'react';
import { Typography, Box } from '@mui/material';
import { useIntl } from 'react-intl';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { useUtilityClasses } from './useUtilityClasses';


export interface FsPropertiesLanguageProps {
  direntId: string;
}

export const FsPropertiesLanguage: React.FC<FsPropertiesLanguageProps> = ({ direntId }) => {
  const intl = useIntl();
  const classes = useUtilityClasses();
  const { getDirent } = useFsDirent();

  const dirent = getDirent(direntId);

  if (!dirent) {
    return;
  }

  if (dirent.type !== 'LOCALE') {
    return undefined;
  }

  const configOptionsEnabled = dirent.props?.configOptions ?? [];

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
        <Typography className={classes.propertyLabel}>{intl.formatMessage({ id: 'fs.properties.propertyLabel.localeCode' })}</Typography>
        <Typography className={classes.propertyValue}>{(dirent.props as Fs.LanguageProps)?.localeCode}</Typography>
      </div>
    </>
  );
};
