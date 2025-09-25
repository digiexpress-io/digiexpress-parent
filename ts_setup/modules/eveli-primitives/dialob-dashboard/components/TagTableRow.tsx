import React from 'react'
import { Box, IconButton, Link, TableCell, TableRow, Tooltip } from '@mui/material';
import type { FormConfiguration, FormConfigurationFilters, DialobAdminConfig } from '../types';
import { useIntl } from 'react-intl';
import { useFormTagOptions } from '../util';
import { LabelChips } from './LabelChips';
import ContentCopyIcon from '@mui/icons-material/ContentCopy';
import CloseIcon from '@mui/icons-material/Close';
import DownloadIcon from '@mui/icons-material/Download';
import { DateTimeFormatter } from '@dxs-ts/xui-datetime';

export interface TagTableRowProps {
  filters: FormConfigurationFilters;
  formConfiguration: FormConfiguration;
  deleteFormConfiguration: (formConfiguration: FormConfiguration) => void;
  copyFormConfiguration: (formConfiguration: FormConfiguration) => void;
  config: DialobAdminConfig;
  getDialobForm: (formName: string) => Promise<any>;
  setFetchAgain: React.Dispatch<React.SetStateAction<boolean>>;
  onOpenForm?: (formId: string) => void;
}

export const TagTableRow: React.FC<TagTableRowProps> = ({
  filters,
  formConfiguration,
  copyFormConfiguration,
  deleteFormConfiguration,
  config,
  getDialobForm,
  setFetchAgain,
  onOpenForm
}) => {

  const intl = useIntl();
  const tenantParam = config.tenantId ? `?tenantId=${config.tenantId}` : "";

  const { filteredRow, downloadFormConfiguration, updateLabels } = useFormTagOptions({
    formConfiguration,
    filters,
    config,
    getDialobForm,
    setFetchAgain
  });

  return (
    <>
      {filteredRow && (
        <TableRow>
          <TableCell>
            <Link
              target="_blank"
              rel="noopener noreferrer"
              href={`${config.dialobApiUrl}/composer/${formConfiguration.id}${tenantParam}`}
              onClick={function (e) {
                e.preventDefault();
                if (onOpenForm) {
                  onOpenForm(formConfiguration.id);
                } else {
                  window.location.replace(`${config.dialobApiUrl}/composer/${formConfiguration.id}${tenantParam}`);
                }
              }}
            >
              {formConfiguration.metadata.label || intl.formatMessage({ id: "adminUI.dialog.emptyTitle" })}
            </Link>
          </TableCell>
          <TableCell>{formConfiguration?.latestTagName}</TableCell>
          <TableCell>
            <DateTimeFormatter value={formConfiguration?.latestTagDate} />
          </TableCell>
          <TableCell>
            <DateTimeFormatter value={formConfiguration.metadata.lastSaved} />
          </TableCell>
          <TableCell>
            <LabelChips labels={formConfiguration.metadata.labels} onUpdate={updateLabels} />
          </TableCell>
          <TableCell sx={{ textAlign: "center" }}>
            <Box display="flex">
              <Tooltip title={intl.formatMessage({ id: "adminUI.table.tooltip.copy" })} placement='top-end' arrow>
                <IconButton
                  onClick={function (e) {
                    e.preventDefault();
                    copyFormConfiguration(formConfiguration)
                  }}
                >
                  <ContentCopyIcon fontSize="small" />
                </IconButton>
              </Tooltip>
              <Tooltip title={intl.formatMessage({ id: "adminUI.table.tooltip.delete" })} placement='top-end' arrow>
                <IconButton
                  onClick={function (e) {
                    e.preventDefault();
                    deleteFormConfiguration(formConfiguration);
                  }}
                  color='error'
                >
                  <CloseIcon fontSize="small" />
                </IconButton>
              </Tooltip>
              <Tooltip title={intl.formatMessage({ id: "download" })} placement='top-end' arrow>
                <IconButton
                  onClick={function (e) {
                    e.preventDefault();
                    downloadFormConfiguration();
                  }}
                >
                  <DownloadIcon fontSize="small" />
                </IconButton>
              </Tooltip>
            </Box>
          </TableCell>
        </TableRow>
      )}
    </>
  );
}
