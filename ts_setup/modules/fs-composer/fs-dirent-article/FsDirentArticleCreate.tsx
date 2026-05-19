import React from 'react';
import { Typography, Collapse, Box } from '@mui/material';
import { useIntl } from 'react-intl';
import { useFsDirent } from '@dxs-ts/fs-api';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsDirentSelectMulti } from '../fs-dirent-select-multi';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonCreate } from '../fs-dirent-button-create';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { FsDirentTextFieldAutocomplete } from '../fs-dirent-textfield-autocomplete';
import { useUtilityClasses, FsDirentArticleRoot } from './useUtilityClasses';
import { useCreateOwnerState } from './useCreateOwnerState';
import { FsDirentArticleCreateProps } from './FsDirentArticleProps';


export const FsDirentArticleCreate: React.FC<FsDirentArticleCreateProps> = () => {
  const intl = useIntl();
  const ownerState = useCreateOwnerState();
  const classes = useUtilityClasses();
  const { getConfigOptionsForType, selectOptions } = useFsDirent();
  const configOptions = getConfigOptionsForType('ARTICLE');

  const [name, setName] = React.useState('');
  const [selectedConfigOptions, setSelectedConfigOptions] = React.useState<string[]>([]);
  const [labels, setLabels] = React.useState<string[]>([]);
  const [orderNumber, setOrderNumber] = React.useState('');

  return (
    <FsDirentArticleRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.article.sectionTitle.createNew' })}</Typography>
      <div className={classes.formContainer}>

        {ownerState.parentArticle && (
          <>
            <Box display='flex' justifyContent='space-between' alignItems='center'>
              <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.article.parentArticleField.label' })}</Typography>
              <FsIcon icon={FsIcons.Info} small tooltip={intl.formatMessage({ id: 'fs.dirent.article.parentArticleField.desc' })} />
            </Box>
            <FsDirentTextField value={ownerState.parentArticlePath} disabled />
          </>
        )}

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.nameField.label' })}</Typography>
        <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.dirent.article.nameField.placeholder' })}
          required value={name} onChange={setName}
        />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.article.orderNumberField.label' })}</Typography>
        <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.dirent.article.orderNumberField.placeholder' })}
          required
          value={orderNumber}
          onChange={(value) => setOrderNumber(value)} />

        <div className={classes.expandToggle} onClick={ownerState.onToggleExpanded}>
          {intl.formatMessage({ id: ownerState.isExpanded ? 'fs.dirent.expandToggle.hide' : 'fs.dirent.expandToggle.show' })}
          <FsIcon icon={FsIcons.ExpandMore} small className={ownerState.isExpanded ? classes.expandToggleIconOpen : classes.expandToggleIcon} />
        </div>

        <Collapse in={ownerState.isExpanded}>
          <div className={classes.optionalFields}>
            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.article.descriptionField.label' })}</Typography>
            <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.dirent.article.descriptionField.placeholder' })}
              multiline minRows={2} maxRows={5}
            />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.configOptionsField.label' })}</Typography>
            <FsDirentSelectMulti options={configOptions} value={selectedConfigOptions} onChange={setSelectedConfigOptions} />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.article.labelsField.label' })}</Typography>
            <FsDirentTextFieldAutocomplete options={selectOptions.labels} value={labels} onChange={setLabels} placeholder={intl.formatMessage({ id: 'fs.dirent.article.labelsField.placeholder' })} />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.article.commentsField.label' })}</Typography>
            <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.dirent.article.commentsField.placeholder' })}
              multiline minRows={2} maxRows={5}
            />

            <Typography className={classes.sectionTitle}>{intl.formatMessage({ id: 'fs.dirent.article.sectionTitle.sharing' })}</Typography>
            <div className={classes.sectionBox}>
              <Typography className={classes.sectionContent}>TODO</Typography>
            </div>
          </div>
        </Collapse>

        <div className={classes.buttonContainer}>
          <FsDirentButtonCancel />
          <FsDirentButtonCreate />
        </div>

      </div>
    </FsDirentArticleRoot>
  );
};
