import React from 'react';
import { Typography, Collapse } from '@mui/material';
import { FsDirentLoader } from '../fs-dirent-loader';
import { useIntl } from 'react-intl';
import { useFsDirent } from '@dxs-ts/fs-api';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsDirentSelectMulti } from '../fs-dirent-select-multi';
import { FsDirentSelectSingle } from '../fs-dirent-select-single';
import { FsDirentTextFieldAutocomplete } from '../fs-dirent-textfield-autocomplete';
import { FsDirentTextField } from '../fs-dirent-text-field';
import { useUtilityClasses, FsDirentArticleLinkRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentArticleLinkUpdateProps } from './FsDirentArticleLinkProps';

export const FsDirentArticleLinkUpdate: React.FC<FsDirentArticleLinkUpdateProps> = (props) => {
  const intl = useIntl();
  const ownerState = useUpdateOwnerState(props);
  const classes = useUtilityClasses();
  const { selectOptions, getConfigOptionsForType, getDirentName } = useFsDirent();
  const articles = selectOptions.articles.map(item => ({ value: item.value, label: getDirentName(item.value) ?? item.label }));
  const configOptions = getConfigOptionsForType('ARTICLE_LINK');
  const linkTypeOptions = selectOptions.linkTypes.map(v => ({
    value: v,
    label: intl.formatMessage({ id: `fs.dirent.link.contentType.${v}` }),
  }));

  const [isLoading, setIsLoading] = React.useState(true);
  React.useEffect(() => {
    const timer = setTimeout(() => setIsLoading(false), 300);
    return () => clearTimeout(timer);
  }, []);

  if (ownerState.isLoading || isLoading) {
    return <FsDirentLoader />;
  }

  return (
    <FsDirentArticleLinkRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.link.sectionTitle.edit' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.link.urlValueField.label' })}</Typography>
        <FsDirentTextField
          value={ownerState.urlValue}
          placeholder={intl.formatMessage({ id: 'fs.dirent.link.urlValueField.placeholder' })}
          onChange={ownerState.onChangeUrlValue}
        />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.link.contentTypeField.label' })}</Typography>
        <FsDirentSelectSingle options={linkTypeOptions} value={ownerState.contentType} onChange={ownerState.onChangeContentType} />
        <Typography className={classes.helperText}>{intl.formatMessage({ id: `fs.dirent.link.contentType.${ownerState.contentType}.desc` })}</Typography>


        {ownerState.locales.map((locale) => (
          <div key={locale.value} className={classes.localeRow}>
            <Typography className={classes.localeLabel}>{intl.formatMessage({ id: 'fs.dirent.locales.labelField' }, { localeCode: locale.label })}</Typography>
            <FsDirentTextField value={ownerState.intlValues[locale.value] ?? ''} onChange={(value) => ownerState.onChangeIntlValue(locale.value, value)} />
          </div>
        ))}

        <div className={classes.expandToggle} onClick={ownerState.onToggleExpanded}>
          {intl.formatMessage({ id: ownerState.isExpanded ? 'fs.dirent.expandToggle.hide' : 'fs.dirent.expandToggle.show' })}
          <FsIcon icon={FsIcons.ExpandMore} small className={ownerState.isExpanded ? classes.expandToggleIconOpen : classes.expandToggleIcon} />
        </div>

        <Collapse in={ownerState.isExpanded}>
          <div className={classes.optionalFields}>
            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.descriptionField.label' })}</Typography>
            <FsDirentTextField
              value={ownerState.assetDescription}
              placeholder={intl.formatMessage({ id: 'fs.dirent.descriptionField.placeholder' })}
              onChange={ownerState.onChangeDescription}
              multiline minRows={2} maxRows={5}
            />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.labelsField.label' })}</Typography>
            <FsDirentTextFieldAutocomplete options={selectOptions.labels} value={ownerState.tagLabels} onChange={ownerState.onChangeLabels} placeholder={intl.formatMessage({ id: 'fs.dirent.labelsField.placeholder' })} />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.link.articlesField.label' })}</Typography>
            <FsDirentSelectMulti options={articles} value={ownerState.articles} onChange={ownerState.onChangeArticles} />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.dirent.configOptionsField.label' })}</Typography>
            <FsDirentSelectMulti options={configOptions} value={ownerState.configOptions} onChange={ownerState.onChangeConfigOptions} />
          </div>
        </Collapse>


      </div>
    </FsDirentArticleLinkRoot>
  );
};
