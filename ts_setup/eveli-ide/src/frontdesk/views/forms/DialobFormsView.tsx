import React, { useRef, useState } from 'react'
import { Box, CircularProgress, IconButton } from '@mui/material';
import { FormattedDate, FormattedTime, useIntl } from 'react-intl';
import { CreateDialog } from './CreateDialog';
import { DeleteDialog } from './DeleteDialog';
import { useConfig } from '../../context/ConfigContext';
import { DialobFormEntry } from '../../types';
import moment from 'moment';
import { useFetch } from '../../hooks/useFetch';
import MaterialTable, { Column } from '@material-table/core';
import { localizeTable } from '../../util/localizeTable';
import Edit from '@mui/icons-material/Edit';

export const DialobFormsView: React.FC = () => {
  const config = useConfig();
  const { response:dialobForms, refresh } = useFetch<DialobFormEntry[]>(`${config.api}/forms`);
  const [selectedForm, setSelectedForm] = useState<DialobFormEntry | undefined>();
  const [createModalOpen, setCreateModalOpen] = useState<boolean>(false);
  const [deleteModalOpen, setDeleteModalOpen] = useState<boolean>(false);
  const intl = useIntl();
  const tableLocalization = localizeTable((id: string) => intl.formatMessage({ id }));
  const tableRef = useRef();
  const handleCreateModalClose = () => {
      setSelectedForm(undefined);
      setCreateModalOpen(false);
  }

  const handleDeleteModalClose = () => {
      setSelectedForm(undefined);
      setDeleteModalOpen(false);
  }

  const copyFormConfiguration = (formConfiguration: DialobFormEntry) => {
      setSelectedForm(formConfiguration);
      setCreateModalOpen(true);
  }

  const addFormConfiguration = () => {
      setCreateModalOpen(true);
  }
  
  const deleteFormConfiguration = (formConfiguration: DialobFormEntry) => {
      setSelectedForm(formConfiguration);
      setDeleteModalOpen(true);
  }

  const formatDateTime = (time:any) => {
    if (time) {
      const localTime = moment.utc(time).local().toDate();
      return (
      <React.Fragment>
          <FormattedDate value={localTime} />&nbsp;<FormattedTime value={localTime} />
      </React.Fragment>
      )
    }
  return "-";
  }

  interface TableState  {
      columns: Array<Column<DialobFormEntry>>;
    }
  const tableState: TableState = {
      columns: [
      {
          title: "",
          render: (rowData) => {
              const button = (
              <IconButton
                  color="inherit"
                  onClick={() => {
                    window.location.replace(`${config.dialobComposerUrl!}/${rowData.id}`)
                  }}
              >
                  <Edit />
              </IconButton>
              );
              return button;
          },
          width: '10%',
          filtering: false,
          sorting: false
          },
        {
          title: intl.formatMessage({id: 'dialobForms.table.label'}),
          field: 'metadata.label',
          defaultSort: 'asc',
          headerStyle: { fontWeight: 'bold' },
          width: '50%'
        },
        {
          title: intl.formatMessage({id: 'dialobForms.table.created'}),
          field: 'metadata.created',
          filtering: false,
          type: 'date',
          render: data => formatDateTime(data.metadata.created),
          headerStyle: { fontWeight: 'bold' },
          width: '20%'
        },
        {
          title: intl.formatMessage({id: 'dialobForms.table.lastSaved'}),
          field: 'metadata.lastSaved',
          filtering: false,
          render: data => formatDateTime(data.metadata.lastSaved),
          headerStyle: { fontWeight: 'bold' },
          width: '20%'
        }
      ]
    };
  return (
      <Box pt={6}>
        {dialobForms ? (
          <Box sx={{padding: "0 50px"}}>
          <MaterialTable
              title = {intl.formatMessage({id: 'dialobForms.dialog.heading'})}
              localization={tableLocalization}
              columns={tableState.columns}
              tableRef={tableRef}
              options={{
                actionsColumnIndex: -1,
                debounceInterval: 500,
                padding: 'dense',
                filtering: false,
                sorting: true,
                search: true,
                paging: false
              }}
              actions={[
              {
                  icon: 'add',
                  tooltip: intl.formatMessage({id: 'dialobForms.table.tooltip.add'}),
                  isFreeAction: true,
                  onClick: () => {addFormConfiguration();}
              },
              {
                  icon: 'library_add',
                  tooltip: intl.formatMessage({id: 'dialobForms.table.tooltip.copy'}),
                  onClick: (event, data) => {!Array.isArray(data) &&  copyFormConfiguration(data)}
              },
              {
                  icon: 'delete_outline',
                  tooltip: intl.formatMessage({id: 'dialobForms.table.tooltip.delete'}),
                  onClick: (event, data) => {!Array.isArray(data) &&  deleteFormConfiguration(data)}
              },
              ]}
          
              isLoading={false}
              data={dialobForms||[]}
          />
          <CreateDialog 
              createModalOpen={createModalOpen}
              handleCreateModalClose={handleCreateModalClose}
              refresh={refresh}
              formConfiguration={selectedForm}
          />
          <DeleteDialog 
              deleteModalOpen={deleteModalOpen}
              handleDeleteModalClose={handleDeleteModalClose}
              refresh={refresh}
              formConfiguration={selectedForm}
          />
          </Box>
      ): (
          <CircularProgress />
      )}
      </Box>
  );
}
