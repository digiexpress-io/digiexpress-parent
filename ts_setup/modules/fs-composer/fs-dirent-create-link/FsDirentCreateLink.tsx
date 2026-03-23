import React from 'react';
import { TextField, Typography, FormControl, Select, MenuItem, Chip, OutlinedInput, Collapse, Divider, Switch } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsDirent, mockFsData } from '@dxs-ts/fs-api';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonCreate } from '../fs-dirent-button-create';
import { FsDirentCreateLinkProps } from './FsDirentCreateLinkProps';
import { useUtilityClasses, FsDirentCreateLinkRoot } from './useUtilityClasses';
import { useOwnerState } from './useOwnerState';

export const FsDirentCreateLink: React.FC<FsDirentCreateLinkProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsDirentCreateLinkRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.direntCreate.link.sectionTitle.createNew' })}</Typography>
      <div className={classes.formContainer}>

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntCreate.link.valueField.label' })}</Typography>
        <TextField className={classes.textField}
          placeholder={intl.formatMessage({ id: 'fs.direntCreate.link.valueField.placeholder' })}
          size='small' fullWidth
        />

        <Divider />

              <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.direntCreate.link.sectionTitle.createLocaleLabels' })}</Typography>


        {ownerState.locales.map((locale) => (
          <div key={locale} className={classes.localeRow}>
            <Typography className={classes.localeLabel}>{intl.formatMessage({ id: `fs.direntCreate.link.labelField.${locale}.label` })}</Typography>
            <TextField className={classes.textField}
              placeholder={intl.formatMessage({ id: 'fs.direntCreate.link.labelField.placeholder' })}
              size='small' fullWidth
            />
          </div>
        ))}

        <div className={classes.expandToggle} onClick={ownerState.onToggleExpanded}>
          {intl.formatMessage({ id: ownerState.isExpanded ? 'fs.direntCreate.link.expandToggle.hide' : 'fs.direntCreate.link.expandToggle.show' })}
          <FsIcon icon={FsIcons.ExpandMore} small className={ownerState.isExpanded ? classes.expandToggleIconOpen : classes.expandToggleIcon} />
        </div>

        <Collapse in={ownerState.isExpanded}>
          <div className={classes.optionalFields}>
            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntCreate.link.articlesField.label' })}</Typography>
            <FormControl className={classes.formControl} fullWidth size='small'>
              <Select className={classes.select} multiple
                value={mockSelectedArticleValues}
                input={<OutlinedInput />}
                renderValue={(selected) => (
                  <div className={classes.chipContainer}>
                    {(selected as string[]).map((value) => {
                      const option = MOCK_ARTICLE_OPTIONS.find(opt => opt.value === value);
                      return (
                        <Chip key={value}
                          className={classes.chip}
                          label={option?.label}
                          size="small"
                        />
                      );
                    })}
                  </div>
                )}
              >
                {MOCK_ARTICLE_OPTIONS.map((option) => (
                  <MenuItem key={option.value} value={option.value} className={classes.menuItem}>
                    {option.label}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            <div className={classes.configRow}>
              <Typography className={classes.configLabel}>{intl.formatMessage({ id: 'fs.direntCreate.link.devModeOption.label' })}</Typography>
              <Switch className={classes.switchRoot} size='small' />
            </div>
          </div>
        </Collapse>

        <div className={classes.buttonContainer}>
          <FsDirentButtonCancel />
          <FsDirentButtonCreate />
        </div>

      </div>
    </FsDirentCreateLinkRoot>
  );
};

function collectArticles(nodes: FsDirent[]): { value: string; label: string }[] {
  const result: { value: string; label: string }[] = [];
  nodes.forEach(node => {
    if (node.type === 'article') {
      result.push({ value: node.id, label: node.name });
    }
    if (node.children && node.children.length > 0) {
      result.push(...collectArticles(node.children));
    }
  });
  return result;
}

const MOCK_ARTICLE_OPTIONS = collectArticles(mockFsData);
const mockSelectedArticleValues: string[] = [];
