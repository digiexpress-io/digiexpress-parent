import React from 'react';
import { TextField, Typography, FormControl, Select, MenuItem, Chip, OutlinedInput, Collapse, Box } from '@mui/material';
import { useIntl } from 'react-intl';
import { FsIcon, FsIcons } from '../fs-theme';
import { FsDirentButtonCancel } from '../fs-dirent-button-cancel';
import { FsDirentButtonCreate } from '../fs-dirent-button-create';
import { FsDirentCreateArticleProps } from './FsDirentCreateArticleProps';
import { useUtilityClasses, FsDirentCreateArticleRoot } from './useUtilityClasses';
import { useOwnerState } from './useOwnerState';

export const FsDirentCreateArticle: React.FC<FsDirentCreateArticleProps> = (props) => {
  const intl = useIntl();
  const ownerState = useOwnerState(props);
  const classes = useUtilityClasses();

  return (
    <FsDirentCreateArticleRoot className={classes.root} ownerState={ownerState}>
      <Typography className={classes.title}>{intl.formatMessage({ id: 'fs.direntCreate.article.sectionTitle.createNew' })}</Typography>
      <div className={classes.formContainer}>

        {ownerState.parentArticle && (
          <>
            <Box display='flex' justifyContent='space-between' alignItems='center'>
              <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntCreate.article.parentArticleField.label' })}</Typography>
              <FsIcon icon={FsIcons.Info} small tooltip={intl.formatMessage({ id: 'fs.direntCreate.article.parentArticleField.desc' })} />
            </Box>
            <TextField className={classes.textField}
              value={ownerState.parentArticlePath}
              size='small' fullWidth disabled
            />
          </>
        )}

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntCreate.article.nameField.label' })}</Typography>
        <TextField className={classes.textField}
          placeholder={intl.formatMessage({ id: 'fs.direntCreate.article.nameField.placeholder' })}
          size='small' fullWidth
        />

        <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntCreate.article.orderNumberField.label' })}</Typography>
        <TextField className={classes.textField}
          placeholder={intl.formatMessage({ id: 'fs.direntCreate.article.orderNumberField.placeholder' })}
          size='small' fullWidth
        />

        <div className={classes.expandToggle} onClick={ownerState.onToggleExpanded}>
          {intl.formatMessage({ id: ownerState.isExpanded ? 'fs.direntCreate.article.expandToggle.hide' : 'fs.direntCreate.article.expandToggle.show' })}
          <FsIcon icon={FsIcons.ExpandMore} small className={ownerState.isExpanded ? classes.expandToggleIconOpen : classes.expandToggleIcon} />
        </div>

        <Collapse in={ownerState.isExpanded}>
          <div className={classes.optionalFields}>
            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntCreate.article.descriptionField.label' })}</Typography>
            <TextField className={classes.textField}
              placeholder={intl.formatMessage({ id: 'fs.direntCreate.article.descriptionField.placeholder' })}
              size='small' fullWidth multiline minRows={2} maxRows={5}
            />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntCreate.article.configOptionsField.label' })}</Typography>
            <FormControl className={classes.formControl} fullWidth size='small'>
              <Select className={classes.select} multiple
                value={mockSelectedValues}
                input={<OutlinedInput />}
                renderValue={(selected) => (
                  <div className={classes.chipContainer}>
                    {(selected as string[]).map((value) => {
                      const option = configOptions.find(opt => opt.value === value);
                      const IconComponent = FsIcons[option?.icon as keyof typeof FsIcons];
                      return (
                        <Chip key={value}
                          className={classes.chip}
                          icon={IconComponent ? <FsIcon icon={IconComponent} small /> : undefined}
                          label={option?.label}
                          size="small"
                        />
                      );
                    })}
                  </div>
                )}
              >
                {configOptions.map((option) => {
                  const IconComponent = FsIcons[option.icon as keyof typeof FsIcons];
                  return (
                    <MenuItem key={option.value} value={option.value} className={classes.menuItem}>
                      <div className={classes.menuItemContent}>
                        {IconComponent && <FsIcon icon={IconComponent} small />}
                        {option.label}
                      </div>
                    </MenuItem>
                  );
                })}
              </Select>
            </FormControl>

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntCreate.article.labelsField.label' })}</Typography>
            <TextField className={classes.textField} size='small' fullWidth
              placeholder={intl.formatMessage({ id: 'fs.direntCreate.article.labelsField.placeholder' })}
            />

            <Typography className={classes.label}>{intl.formatMessage({ id: 'fs.direntCreate.article.commentsField.label' })}</Typography>
            <TextField className={classes.textField}
              placeholder={intl.formatMessage({ id: 'fs.direntCreate.article.commentsField.placeholder' })}
              size='small' fullWidth multiline minRows={2} maxRows={5}
            />

            <Typography className={classes.sectionTitle}>{intl.formatMessage({ id: 'fs.direntCreate.article.sectionTitle.sharing' })}</Typography>
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
    </FsDirentCreateArticleRoot>
  );
};

const configOptions = [
  { value: 'devMode', label: 'Development', icon: 'DevMode' },
  { value: 'assignableMode', label: 'Assignable', icon: 'Assignment' },
  { value: 'disabledMode', label: 'Disabled', icon: 'Disabled' },
  { value: 'anonymousMode', label: 'Anonymous', icon: 'Anonymous' },
];

const mockSelectedValues = ['devMode', 'assignableMode', 'disabledMode', 'anonymousMode'];
