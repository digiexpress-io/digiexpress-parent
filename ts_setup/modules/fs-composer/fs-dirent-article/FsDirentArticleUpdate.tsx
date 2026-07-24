import React from 'react';
import { Typography, Button, Stack, Box } from '@mui/material';
import { useIntl } from 'react-intl';
import { createWidget } from '../fs-factory';
import { useUtilityClasses, FsDirentArticleRoot } from './useUtilityClasses';
import { useUpdateOwnerState } from './useUpdateOwnerState';
import { FsDirentArticleProps } from './FsDirentArticleProps';
import { FsDirentSelectMulti, FsDirentTextField, FsDirentFormField, FsDirentSelectGrouped, FsDirentSelectGroup } from '../fs-utilities';
import { Fs, useFsDirent } from '@dxs-ts/fs-api';
import { FsColors, FsIcon, FsIcons } from '../fs-theme';


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

  const savedLinks = selectOptions.links
    .filter(l => ((getDirent(l.value)?.props as Fs.LinkProps | undefined)?.articles ?? []).includes(props.direntId))
    .map(l => l.value);

  const groups: FsDirentSelectGroup[] = selectOptions.languages.map(locale => ({
    localeId: locale.value,
    localeLabel: locale.label,
    items: selectOptions.links
      .filter(l => {
        const linkProps = getDirent(l.value)?.props as Fs.LinkProps | undefined;
        return !!linkProps?.intlValues?.[locale.value];
      })
      .map(l => {
        const linkProps = getDirent(l.value)?.props as Fs.LinkProps | undefined;
        return {
          id: l.value,
          label: linkProps?.intlValues?.[locale.value] ?? l.label,
          desc: linkProps?.urlValue,
        };
      }),
  }));

  return (
    <FsDirentArticleRoot className={classes.root}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.dirent.article.sectionTitle.edit' }, { name: ownerState.assetPath })}</Typography>

      <FsDirentSelectGrouped
        open={dialogOpen}
        onClose={() => setDialogOpen(false)}
        title="Article links"
        value={ownerState.links}
        onChange={ownerState.onChangeLinks}
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

        <FsDirentFormField label={intl.formatMessage({ id: 'fs.dirent.article.linksField.label' })}>
          <Stack direction="column" spacing={0.5}>
            {savedLinks.map(linkId => {
              const linkLabel = selectOptions.links.find(l => l.value === linkId)?.label ?? linkId;
              return (
                <Stack key={linkId} direction="row" spacing={1} alignItems="center">
                  <FsIcon icon={FsIcons.Link} color={FsColors.direntTypes.link} />
                  <Typography variant="subtitle2">{linkLabel}</Typography>
                </Stack>
              );
            })}
          </Stack>
          <Box sx={{ mt: 1 }}>
            <Button onClick={() => setDialogOpen(true)}>
              {intl.formatMessage({ id: 'fs.dirent.article.linksEdit.button.label' })}
            </Button>
          </Box>
        </FsDirentFormField>


      </div>
    </FsDirentArticleRoot>
  )
};
