import React from 'react';
import { Typography, Divider, Collapse, Switch } from '@mui/material';
import { useIntl } from 'react-intl';
import { collectArticles, mockFsData } from '@dxs-ts/fs-api';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonCreate } from '../fs-dirent-button-create';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { FsDirentMultiSelect } from '../fs-dirent-multi-select';
import { FsDirentCreatePhoneProps } from './FsDirentCreatePhoneProps';
import { useUtilityClasses, FsDirentCreatePhoneRoot } from './useUtilityClasses';
import { useOwnerState } from './useOwnerState';

const articles = collectArticles(mockFsData);

export const FsDirentCreatePhone: React.FC<FsDirentCreatePhoneProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();
  const [selectedArticles, setSelectedArticles] = React.useState<string[]>([]);

  return (
    <FsDirentCreatePhoneRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.direntCreate.phone.sectionTitle.createNew' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntCreate.phone.phoneValueField.label' })}</Typography>
        <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.direntCreate.phone.phoneValueField.placeholder' })} />

        <Divider />

        <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.direntCreate.phone.sectionTitle.createLocaleLabels' })}</Typography>

        {ownerState.locales.map((locale) => (
          <div key={locale} className={classes.localeRow}>
            <Typography className={classes.localeLabel}>{intl.formatMessage({ id: `fs.direntCreate.phone.labelField.${locale}.label` })}</Typography>
            <FsDirentTextField placeholder={intl.formatMessage({ id: 'fs.direntCreate.phone.labelField.placeholder' })} />
          </div>
        ))}

        <div className={classes.expandToggle} onClick={ownerState.onToggleExpanded}>
          {intl.formatMessage({ id: ownerState.isExpanded ? 'fs.direntCreate.phone.expandToggle.hide' : 'fs.direntCreate.phone.expandToggle.show' })}
          <FsIcon icon={FsIcons.ExpandMore} small className={ownerState.isExpanded ? classes.expandToggleIconOpen : classes.expandToggleIcon} />
        </div>

        <Collapse in={ownerState.isExpanded}>
          <div className={classes.optionalFields}>
            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntCreate.phone.articlesField.label' })}</Typography>
            <FsDirentMultiSelect options={articles} value={selectedArticles} onChange={setSelectedArticles} />

            <div className={classes.configRow}>
              <Typography className={classes.configLabel}>{intl.formatMessage({ id: 'fs.direntCreate.phone.devModeOption.label' })}</Typography>
              <Switch className={classes.switchRoot} size='small' />
            </div>
          </div>
        </Collapse>

        <div className={classes.buttonContainer}>
          <FsDirentButtonCancel />
          <FsDirentButtonCreate />
        </div>

      </div>
    </FsDirentCreatePhoneRoot>
  );
};
