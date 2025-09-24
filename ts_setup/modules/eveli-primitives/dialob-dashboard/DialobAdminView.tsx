import React, { useEffect } from 'react'
import {
  Box, TableContainer, Typography, Table, TableRow, TableHead, TableBody,
  Tooltip, IconButton, SvgIcon, OutlinedInput, TableCell, Button
} from '@mui/material';
import { FormattedMessage, useIntl } from 'react-intl';
import type { FormConfiguration } from './types';
import { SortField, CustomDatePicker, Spinner, CreateDialog, DeleteDialog, TagTableRow } from './components';
import AddIcon from '@mui/icons-material/Add';
import DownloadIcon from '@mui/icons-material/Download';
import FileUploadIcon from '@mui/icons-material/FileUpload';
import { useDialobDashboardState } from './context/DialobDashboardStateContext';

export const DialobAdminView: React.FC = () => {
  const intl = useIntl();

  const {
    config,
    formConfigurations,
    selectedFormConfiguration,
    filters,
    sortConfig,
    fetchAgain,
    setFetchAgain,
    createModalOpen,
    deleteModalOpen,
    handleSort,
    downloadAllFormConfigurations,
    uploadJsonDialogForm,
    uploadCsvDialogForm,
    handleJsonUploadClick,
    handleCsvUploadClick,
    handleCreateModalClose,
    handleDeleteModalClose,
    copyFormConfiguration,
    addFormConfiguration,
    deleteFormConfiguration,
    handleChangeInput,
    handleDateChange,
    handleDateClear,
    getDialobForm,
    fileInputRefJson,
    fileInputRefCsv,
    fetchFormConfigurations,
    sortedFormConfigurations,
    onOpenForm 
  } = useDialobDashboardState();

  useEffect(() => {
    fetchFormConfigurations();
  }, [fetchAgain]);

  return (
    <>
      {formConfigurations ? (
        <Box>
          <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", pb: 3 }}>
            <Typography variant='h2'><FormattedMessage id={'adminUI.dialog.heading'} /></Typography>
            <Box display="flex" gap={1}>
              <Button
                onClick={function (e) {
                  e.preventDefault();
                  addFormConfiguration();
                }}
              >
                <SvgIcon fontSize="medium"><AddIcon /></SvgIcon>
              </Button>
              <Button onClick={handleJsonUploadClick}>
                <SvgIcon fontSize="small" >
                  <FileUploadIcon />
                </SvgIcon>
                {intl.formatMessage({ id: "upload.json" })}
              </Button>
              <input
                ref={fileInputRefJson}
                type='file'
                accept='.json'
                hidden
                onChange={(e) => uploadJsonDialogForm(e)}
              />
              <Button onClick={handleCsvUploadClick}>
                <SvgIcon fontSize="small">
                  <FileUploadIcon />
                </SvgIcon>
                {intl.formatMessage({ id: "upload.csv" })}
              </Button>
              <input
                ref={fileInputRefCsv}
                type='file'
                accept='.csv'
                hidden
                onChange={(e) => uploadCsvDialogForm(e)}
              />
            </Box>
          </Box>
          <TableContainer>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell width="22%">
                    <SortField
                      active={sortConfig.field === 'label'}
                      direction={sortConfig.field === 'label' ? sortConfig.direction : 'asc'}
                      name="label"
                      handleSort={handleSort}
                    />
                  </TableCell>
                  <TableCell width="22%">
                    <SortField
                      active={sortConfig.field === 'latestTagName'}
                      direction={sortConfig.field === 'latestTagName' ? sortConfig.direction : 'asc'}
                      name="latestTagName"
                      handleSort={handleSort}
                    />
                  </TableCell>
                  <TableCell width="17%" sx={{ minWidth: "220px" }}>
                    <SortField
                      active={sortConfig.field === 'latestTagDate'}
                      direction={sortConfig.field === 'latestTagDate' ? sortConfig.direction : 'asc'}
                      name="latestTagDate"
                      handleSort={handleSort}
                    />
                  </TableCell>
                  <TableCell width="17%" sx={{ minWidth: "220px" }}>
                    <SortField
                      active={sortConfig.field === 'lastSaved'}
                      direction={sortConfig.field === 'lastSaved' ? sortConfig.direction : 'asc'}
                      name="lastSaved"
                      handleSort={handleSort}
                    />
                  </TableCell>
                  <TableCell width="18%">
                    <FormattedMessage id={"adminUI.formConfiguration.labels"} />
                  </TableCell>
                  <TableCell width="4%" sx={{ textAlign: "right" }}>
                    <Tooltip title={intl.formatMessage({ id: "download.all" })} placement='top-end' arrow>
                      <IconButton
                        onClick={function (e) {
                          e.preventDefault();
                          downloadAllFormConfigurations();
                        }}
                      >
                        <SvgIcon fontSize="small"><DownloadIcon /></SvgIcon>
                      </IconButton>
                    </Tooltip>
                  </TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                <TableRow>
                  <TableCell>
                    <OutlinedInput
                      sx={{ height: '40px' }}
                      name='label'
                      onChange={handleChangeInput}
                      value={filters.label}
                    />
                  </TableCell>
                  <TableCell>
                    <OutlinedInput
                      sx={{ height: '40px' }}
                      name='latestTagName'
                      onChange={handleChangeInput}
                      value={filters.latestTagName}
                    />
                  </TableCell>
                  <TableCell>
                    <CustomDatePicker
                      value={filters.latestTagDate}
                      onChange={(date) => handleDateChange('latestTagDate', date)}
                      handleDateClear={() => handleDateClear('latestTagDate')}
                    />
                  </TableCell>
                  <TableCell>
                    <CustomDatePicker
                      value={filters.lastSaved}
                      onChange={(date) => handleDateChange('lastSaved', date)}
                      handleDateClear={() => handleDateClear('lastSaved')}
                    />
                  </TableCell>
                  <TableCell>
                    <OutlinedInput
                      sx={{ height: '40px' }}
                      name='labels'
                      onChange={handleChangeInput}
                      value={filters.labels}
                    />
                  </TableCell>
                  <TableCell />
                </TableRow>
                {sortedFormConfigurations.map((formConfiguration: FormConfiguration, index: number) =>
                  <TagTableRow
                    key={index}
                    filters={filters}
                    formConfiguration={formConfiguration}
                    deleteFormConfiguration={deleteFormConfiguration}
                    copyFormConfiguration={copyFormConfiguration}
                    getDialobForm={getDialobForm}
                    config={config}
                    setFetchAgain={setFetchAgain}
                    onOpenForm={onOpenForm}
                  />
                )}
              </TableBody>
            </Table>
          </TableContainer>
          <CreateDialog
            createModalOpen={createModalOpen}
            handleCreateModalClose={handleCreateModalClose}
            setFetchAgain={setFetchAgain}
            formConfiguration={selectedFormConfiguration}
            config={config}
          />
          <DeleteDialog
            deleteModalOpen={deleteModalOpen}
            handleDeleteModalClose={handleDeleteModalClose}
            setFetchAgain={setFetchAgain}
            formConfiguration={selectedFormConfiguration}
            config={config}
          />
        </Box>
      ) : (
        <Spinner />
      )}
    </>
  );
}
