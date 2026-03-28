import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { mockFsData, mockFsDirentProperties, FsDirentData } from '@dxs-ts/fs-api';
import { FsDirentSelectSingle } from '../fs-dirent-select-single';
import { FsDirentSelectMulti } from '../fs-dirent-select-multi';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonCreate } from '../fs-dirent-button-create';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { useUtilityClasses, FsDirentPageRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentPageUpdateProps } from './FsDirentPageProps';


const configOptions = FsDirentData.getConfigOptionsForType('page');

export const FsDirentPageUpdate: React.FC<FsDirentPageUpdateProps> = (props) => {
  const intl = useIntl();
  const ownerState = useUpdateOwnerState(props);
  const classes = useUtilityClasses();
  const data = new FsDirentData(mockFsData, mockFsDirentProperties);
  const articleOptions = data.articles;

  return (
    <FsDirentPageRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.page.sectionTitle.edit' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.page.localeCodeField.label' })}</Typography>
        <FsDirentTextField
          value={ownerState.localeCode}
          placeholder={intl.formatMessage({ id: 'fs.dirent.page.localeCodeField.placeholder' })}
          onChange={ownerState.onChangeLocaleCode}
          required
        />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.page.articleField.label' })}</Typography>
        <FsDirentSelectSingle options={articleOptions} value={ownerState.articleId} onChange={ownerState.onChangeArticleId} />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.page.descriptionField.label' })}</Typography>
        <FsDirentTextField
          value={ownerState.description}
          placeholder={intl.formatMessage({ id: 'fs.dirent.page.descriptionField.placeholder' })}
          onChange={ownerState.onChangeDescription}
          multiline minRows={2} maxRows={4}
        />


        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.page.configOptionsField.label' })}</Typography>
        <FsDirentSelectMulti options={configOptions} value={ownerState.configOptions} onChange={ownerState.onChangeConfigOptions} />

        <div className={classes.buttonContainer}>
          <FsDirentButtonCancel />
          <FsDirentButtonCreate />
        </div>

      </div>
    </FsDirentPageRoot>
  );
};
