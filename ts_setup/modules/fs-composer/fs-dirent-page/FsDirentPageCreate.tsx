import React from 'react';
import { Typography } from '@mui/material';
import { useIntl } from 'react-intl';
import { useFsDirent } from '@dxs-ts/fs-api';
import { FsDirentSelectSingle } from '../fs-dirent-select-single';
import { FsDirentSelectMulti } from '../fs-dirent-select-multi';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonCreate } from '../fs-dirent-button-create';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { useUtilityClasses, FsDirentPageRoot } from './useUtilityClasses';
import { useCreateOwnerState } from './useCreateOwnerState';
import { FsDirentPageCreateProps } from './FsDirentPageProps';


export const FsDirentPageCreate: React.FC<FsDirentPageCreateProps> = (props) => {
  const intl = useIntl();
  const ownerState = useCreateOwnerState(props);
  const classes = useUtilityClasses();
  const { selectOptions, getConfigOptionsForType } = useFsDirent();
  const articleOptions = selectOptions.articles;
  const configOptions = getConfigOptionsForType('page');

  const [selectedArticleId, setSelectedArticleId] = React.useState('');
  const [selectedConfigOptions, setSelectedConfigOptions] = React.useState<string[]>([]);

  return (
    <FsDirentPageRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.page.sectionTitle.createNew' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.page.localeCodeField.label' })}</Typography>
        <FsDirentTextField  placeholder={intl.formatMessage({ id: 'fs.dirent.page.localeCodeField.placeholder' })}  required />
        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.page.parentField.label' })}</Typography>
        <FsDirentTextField value={ownerState.pathToTopParent + " /"} disabled />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.page.articleField.label' })}</Typography>
        <FsDirentSelectSingle options={articleOptions} value={selectedArticleId} onChange={setSelectedArticleId} />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.page.descriptionField.label' })}</Typography>
        <FsDirentTextField
          placeholder={intl.formatMessage({ id: 'fs.dirent.page.descriptionField.placeholder' })}
          multiline minRows={2} maxRows={4}
        />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.page.configOptionsField.label' })}</Typography>
        <FsDirentSelectMulti options={configOptions} value={selectedConfigOptions} onChange={setSelectedConfigOptions} />

        <div className={classes.buttonContainer}>
          <FsDirentButtonCancel />
          <FsDirentButtonCreate />
        </div>

      </div>
    </FsDirentPageRoot>
  );
};
