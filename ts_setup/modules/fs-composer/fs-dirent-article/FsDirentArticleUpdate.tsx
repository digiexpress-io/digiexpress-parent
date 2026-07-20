import React from 'react';
import { Typography, Button } from '@mui/material';
import { useIntl } from 'react-intl';
import { createWidget } from '../fs-factory';
import { useUtilityClasses, FsDirentArticleRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentArticleProps } from './FsDirentArticleProps';
import { FsDirentSelectMulti, FsDirentTextField, FsDirentFormField, FsDirentSelectGrouped, FsDirentSelectGroup } from '../fs-utilities';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';


export const FsDirentArticleUpdate: React.FC<FsDirentArticleProps> = (props) => {
  const intl = useIntl();
  const ownerState = useUpdateOwnerState(props);
  const classes = useUtilityClasses();
  const { selectOptions, getDirent } = useFsDirent();
  const configOptions = createWidget({ type: 'ARTICLE' }).meta.configOptions.map(opt => ({
    value: opt,
    label: intl.formatMessage({ id: `fs.dirent.configOption.${opt}` }),
  }));

  const [dialogOpen, setDialogOpen] = React.useState(false);

  const selectedLinks = selectOptions.links
    .filter(l => {
      const linkProps = getDirent(l.value)?.props as Fs.LinkProps | undefined;
      return linkProps?.articles?.includes(props.direntId) ?? false;
    })
    .map(l => l.value);

  const groups: FsDirentSelectGroup[] = selectOptions.languages.map(locale => ({
    localeId: locale.value,
    localeLabel: locale.label,
    items: selectOptions.links
      .filter(l => {
        const linkProps = getDirent(l.value)?.props as Fs.LinkProps | undefined;
        return !!linkProps?.intlValues?.[locale.value];
      })
      .map(l => ({
        id: l.value,
        label: (getDirent(l.value)?.props as Fs.LinkProps | undefined)?.intlValues?.[locale.value] ?? l.label,
      })),
  }));

  return (
    <FsDirentArticleRoot className={classes.root}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.article.sectionTitle.edit' }, { name: ownerState.assetPath })}</Typography>

      <FsDirentSelectGrouped
        open={dialogOpen}
        onClose={() => setDialogOpen(false)}
        title="Article links"
        value={selectedLinks}
        onChange={(value) => console.log('selected links:', value)}
        groups={groups}
      />

      <div className={classes.formContainer}>
        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.nameField.label' })}>
          <FsDirentTextField required value={ownerState.name}
            placeholder={intl.formatMessage({ id: 'fs.dirent.article.nameField.placeholder' })}
            onChange={ownerState.onChangeName}
          />
        </FsDirentFormField>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.article.orderNumberField.label' })}>
          <FsDirentTextField required value={ownerState.orderNumber}
            placeholder={intl.formatMessage({ id: 'fs.dirent.article.orderNumberField.placeholder' })}
            onChange={ownerState.onChangeOrderNumber}
          />
        </FsDirentFormField>

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.configOptionsField.label' })}>
          <FsDirentSelectMulti options={configOptions} value={ownerState.configOptions} onChange={ownerState.onChangeConfigOptions} />
        </FsDirentFormField>

        <Button size="small" onClick={() => setDialogOpen(true)}>Article links ({selectedLinks.length})</Button>

      </div>
    </FsDirentArticleRoot>
  )
};
