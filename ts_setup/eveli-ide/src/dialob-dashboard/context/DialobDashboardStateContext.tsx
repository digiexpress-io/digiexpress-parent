/* eslint-disable react-refresh/only-export-components */
import React, { createContext, useContext, useState, useMemo, useRef } from 'react';
import type { DialobAdminConfig, FormConfiguration, FormConfigurationFilters, FormTag, Metadata } from '../types';
import { DEFAULT_CONFIGURATION_FILTERS, downloadAsJSON } from '../util';
import { checkHttpResponse, checkSearchHttpResponse, handleRejection } from '../middleware';
import { useAdminBackend } from '../backend';

const CSV_PARSING_ERROR = "CSV_PARSING_ERROR";

interface SortConfig {
  field: string | null;
  direction: 'asc' | 'desc';
}

export interface DashboardStateContextType {
  config: DialobAdminConfig;
  formConfigurations: FormConfiguration[];
  setFormConfigurations: React.Dispatch<React.SetStateAction<FormConfiguration[]>>;
  selectedFormConfiguration?: FormConfiguration;
  setSelectedFormConfiguration: React.Dispatch<React.SetStateAction<FormConfiguration | undefined>>;
  filters: FormConfigurationFilters;
  setFilters: React.Dispatch<React.SetStateAction<FormConfigurationFilters>>;
  sortConfig: SortConfig;
  setSortConfig: React.Dispatch<React.SetStateAction<SortConfig>>;
  fetchAgain: boolean;
  setFetchAgain: React.Dispatch<React.SetStateAction<boolean>>;
  createModalOpen: boolean;
  setCreateModalOpen: React.Dispatch<React.SetStateAction<boolean>>;
  deleteModalOpen: boolean;
  setDeleteModalOpen: React.Dispatch<React.SetStateAction<boolean>>;
  handleSort: (field: string) => void;
  uploadJsonDialogForm: (e: React.ChangeEvent<HTMLInputElement>) => void;
  uploadCsvDialogForm: (e: React.ChangeEvent<HTMLInputElement>) => void;
  handleJsonUploadClick: () => void;
  handleCsvUploadClick: () => void;
  handleCreateModalClose: () => void;
  handleDeleteModalClose: () => void;
  copyFormConfiguration: (formConfiguration: FormConfiguration) => void;
  addFormConfiguration: () => void;
  deleteFormConfiguration: (formConfiguration: FormConfiguration) => void;
  handleChangeInput: (e: any) => void;
  handleDateChange: (name: string, date: Date | null) => void;
  handleDateClear: (name: string) => void;
  getDialobForm: (formName: string) => Promise<any | null>;
  downloadAllFormConfigurations: () => void;
  fileInputRefJson: React.RefObject<HTMLInputElement>;
  fileInputRefCsv: React.RefObject<HTMLInputElement>;
  showNotification?: (message: string, severity: "success" | "error") => void;
  fetchFormConfigurations: () => Promise<void>;
  findLatestTag: (allTags: FormTag[] | undefined, formId: string) => FormTag | undefined;
  sortedFormConfigurations: FormConfiguration[];
  onOpenForm?: (formId: string) => void;
}

const defaultContext: DashboardStateContextType = {
  config: {
    dialobApiUrl: '',
    language: 'en',
    setLoginRequired: () => { },
    setTechnicalError: () => { },
  },
  formConfigurations: [],
  setFormConfigurations: () => { },
  selectedFormConfiguration: undefined,
  setSelectedFormConfiguration: () => { },
  filters: DEFAULT_CONFIGURATION_FILTERS,
  setFilters: () => { },
  sortConfig: { field: 'label', direction: 'asc' },
  setSortConfig: () => { },
  fetchAgain: false,
  setFetchAgain: () => { },
  createModalOpen: false,
  setCreateModalOpen: () => { },
  deleteModalOpen: false,
  setDeleteModalOpen: () => { },
  handleSort: () => { },
  downloadAllFormConfigurations: () => { },
  uploadJsonDialogForm: () => { },
  uploadCsvDialogForm: () => { },
  handleJsonUploadClick: () => { },
  handleCsvUploadClick: () => { },
  handleCreateModalClose: () => { },
  handleDeleteModalClose: () => { },
  copyFormConfiguration: () => { },
  addFormConfiguration: () => { },
  deleteFormConfiguration: () => { },
  handleChangeInput: () => { },
  handleDateChange: () => { },
  handleDateClear: () => { },
  getDialobForm: async () => Promise.resolve(),
  fileInputRefJson: { current: null },
  fileInputRefCsv: { current: null },
  showNotification: undefined,
  fetchFormConfigurations: async () => Promise.resolve(),
  findLatestTag: () => undefined,
  sortedFormConfigurations: [],
  onOpenForm: undefined
};

export const DialobDashboardStateContext = createContext<DashboardStateContextType>(defaultContext);

export const useDialobDashboardState = () => useContext(DialobDashboardStateContext);

export interface DialobDashboardStateProviderProps {
  children: React.ReactNode;
  config: DialobAdminConfig;
  showNotification?: (message: string, severity: "success" | "error") => void;
  onOpenForm?: (formId: string) => void;
}

export const DialobDashboardStateProvider: React.FC<DialobDashboardStateProviderProps> = ({ children, config, showNotification, onOpenForm }) => {
  const [formConfigurations, setFormConfigurations] = useState<FormConfiguration[]>([]);
  const [selectedFormConfiguration, setSelectedFormConfiguration] = useState<FormConfiguration | undefined>();
  const [filters, setFilters] = useState<FormConfigurationFilters>(DEFAULT_CONFIGURATION_FILTERS);
  const [createModalOpen, setCreateModalOpen] = useState<boolean>(false);
  const [deleteModalOpen, setDeleteModalOpen] = useState<boolean>(false);
  const [fetchAgain, setFetchAgain] = useState<boolean>(false);
  const [sortConfig, setSortConfig] = useState<SortConfig>({
    field: "label",
    direction: 'asc',
  });

  const fileInputRefJson = useRef<HTMLInputElement | null>(null);
  const fileInputRefCsv = useRef<HTMLInputElement | null>(null);

  const {
    addAdminFormConfiguration, addAdminFormConfigurationFromCsv, editAdminFormConfiguration,
    getAdminFormConfiguration, getAdminFormAllTags, getAdminFormConfigurationList
  } = useAdminBackend(config);

  const fetchFormConfigurations = async () => {
    try {
      const response = await getAdminFormConfigurationList()
        .then((response: Response) => checkHttpResponse(response, config.setLoginRequired));

      const data = await response.json();
      const tagsResponse = await getAdminFormAllTags()
        .then((response: Response) => checkSearchHttpResponse(response, config.setLoginRequired));
      const allTags: FormTag[] | undefined = await tagsResponse.json();

      const enrichedConfigurations =
        data.map((formConfiguration: FormConfiguration) => {
          const latestTag = findLatestTag(allTags, formConfiguration.id);
          if (latestTag) {
            return {
              ...formConfiguration,
              latestTagName: latestTag.name,
              latestTagDate: latestTag.created,
            };
          } else {
            return formConfiguration;
          }
        }
        );

      setFormConfigurations(enrichedConfigurations);
    } catch (error) {
      handleRejection(error, config.setTechnicalError);
    }
  };

  const findLatestTag = (allTags: FormTag[] | undefined, formId: string): FormTag | undefined => {
    let latestTag: FormTag | undefined = undefined;
    allTags?.forEach((current) => {
      if (current.formName == formId) {
        if (!latestTag) {
          latestTag = current;
        }
        else if (current.created > latestTag.created) {
          latestTag = current;
        }
      }
    });
    return latestTag;
  }

  const sortedFormConfigurations = useMemo(() => {
    if (!sortConfig.field) return formConfigurations;
    let aValue: any = undefined;
    let bValue: any = undefined;

    return [...formConfigurations].sort((a, b) => {
      if (sortConfig.field === 'latestTagDate' || sortConfig.field === 'latestTagName') {
        aValue = a[sortConfig.field as keyof FormConfiguration];
        bValue = b[sortConfig.field as keyof FormConfiguration];
      } else {
        aValue = a.metadata[sortConfig.field as keyof Metadata];
        bValue = b.metadata[sortConfig.field as keyof Metadata];
      }

      if (sortConfig.field === 'latestTagDate' || sortConfig.field === 'lastSaved') {
        const aDate = aValue ? new Date(aValue as string).getTime() : 0;
        const bDate = bValue ? new Date(bValue as string).getTime() : 0;
        return sortConfig.direction === 'asc' ? aDate - bDate : bDate - aDate;
      } else {
        const aStr = (aValue || '').toString().toLowerCase();
        const bStr = (bValue || '').toString().toLowerCase();
        return sortConfig.direction === 'asc'
          ? aStr.localeCompare(bStr)
          : bStr.localeCompare(aStr);
      }
    });
  }, [formConfigurations, sortConfig]);


  const handleJsonUploadClick = () => {
    if (fileInputRefJson.current) {
      fileInputRefJson.current.click();
    }
  };

  const handleCsvUploadClick = () => {
    if (fileInputRefCsv.current) {
      fileInputRefCsv.current.click();
    }
  };

  const handleCreateModalClose = () => {
    setSelectedFormConfiguration(undefined);
    setCreateModalOpen(false);
  }

  const handleDeleteModalClose = () => {
    setSelectedFormConfiguration(undefined);
    setDeleteModalOpen(false);
  }

  const copyFormConfiguration = (formConfiguration: FormConfiguration) => {
    setSelectedFormConfiguration(formConfiguration);
    setCreateModalOpen(true);
  }

  const addFormConfiguration = () => {
    setCreateModalOpen(true);
  }

  const deleteFormConfiguration = (formConfiguration: FormConfiguration) => {
    setSelectedFormConfiguration(formConfiguration);
    setDeleteModalOpen(true);
  }

  const handleChangeInput = (e: any) => {
    setFilters(prevFilters => ({
      ...prevFilters,
      [e.target.name]: e.target.value
    }));
  }

  const handleDateChange = (name: string, date: Date | null) => {
    setFilters(prevFilters => ({
      ...prevFilters,
      [name]: date
    }));
  };

  const handleDateClear = (name: string) => {
    setFilters(prevFilters => ({
      ...prevFilters,
      [name]: null
    }));
  };

  const getDialobForm = async (formName: string) => {
    try {
      const response = await getAdminFormConfiguration(formName);
      await checkHttpResponse(response, config.setLoginRequired);
      const form = await response.json();
      return form;
    } catch (ex) {
      handleRejection(ex, config.setTechnicalError);
      return null;
    }
  }

  const downloadAllFormConfigurations = () => {
    const formNamesList = formConfigurations?.map((formConfiguration: FormConfiguration) => formConfiguration.id) || [];

    const fetchForms = async () => {
      try {
        const forms = await Promise.all(
          formNamesList.map(async (formName) => {
            const form = await getDialobForm(formName);
            return form;
          })
        );
        downloadAsJSON(forms);
      } catch (error) {
        console.error("Error fetching forms:", error);
      }
    };

    fetchForms();
  };

  const uploadJsonDialogForm = (e: any) => {
    const file = e.target.files[0];

    const handleFileRead = async (event: ProgressEvent<FileReader>) => {
      const result = event.target?.result;
      if (typeof result === 'string') {
        try {
          const json = JSON.parse(result);
          if (!Array.isArray(json)) {
            const formNamesList = formConfigurations?.map((formConfiguration: FormConfiguration) => formConfiguration.id) || [];
            delete json._id;
            delete json._rev;

            const uploadPromise = formNamesList.includes(json.name)
              ? editAdminFormConfiguration(json)
              : addAdminFormConfiguration(json);

            try {
              const response = await uploadPromise;
              await checkHttpResponse(response, config.setLoginRequired);
              await response.json();
              if (showNotification) {
                showNotification(`Uploaded ${formNamesList.includes(json.name) ? 'an existing' : 'a new'} form successfully.`, 'success');
              }
              setFetchAgain((prevState) => !prevState);
            } catch (ex) {
              if (showNotification) {
                showNotification(`Error while uploading ${formNamesList.includes(json.name) ? 'an existing' : 'a new'} form: ${ex}`, 'error');
              }
              handleRejection(ex, config.setTechnicalError);
            }
          } else {
            if (showNotification) {
              showNotification(`JSON needs to contain an object, not an array.`, 'error');
            }
          }
        } catch (error: any) {
          if (showNotification) {
            showNotification(`Error parsing JSON: ${error.message}`, 'error');
          }
        }
      }
    };

    const handleFileError = () => {
      if (showNotification) {
        showNotification(`Error reading file.`, 'error');
      }
    };

    if (file) {
      const reader = new FileReader();
      reader.onload = handleFileRead;
      reader.onerror = handleFileError;
      reader.readAsText(file);
    }

    if (fileInputRefJson.current) {
      fileInputRefJson.current.value = '';
    }
  };

  const uploadCsvDialogForm = (e: any) => {
    const file = e.target.files[0];

    const handleFileRead = async (event: ProgressEvent<FileReader>) => {
      const result = event.target?.result;
      let response;
      if (typeof result === 'string') {
        try {
          response = await addAdminFormConfigurationFromCsv(result);
          await checkHttpResponse(response, config.setLoginRequired);

          const responseData = await response.json();
          if (showNotification) {
            showNotification(`Uploaded CSV form successfully. ID: ${responseData?.id}`, 'success');
          }
          setFetchAgain((prev) => !prev);
        } catch (ex) {
          if (response) {
            const responseData = await response.json();
            const errorMessage = responseData?.error === CSV_PARSING_ERROR
              ? responseData?.reason
              : responseData?.message;

            if (errorMessage) {
              console.error(`Error while uploading a new form from CSV: ${errorMessage}`);
              if (showNotification) {
                showNotification(`Error while uploading a new form from CSV: ${errorMessage}`, 'error');
              }
            }
          } else {
            console.error(`Error while uploading a new form from CSV: ${ex}`);
            if (showNotification) {
              showNotification(`Error while uploading a new form from CSV: ${ex}`, 'error');
            }
          }
          handleRejection(ex, config.setTechnicalError);
        }
      }
    };

    const handleFileError = () => {
      if (showNotification) {
        showNotification(`Error reading file.`, 'error');
      }
    };

    if (file) {
      const reader = new FileReader();
      reader.onload = handleFileRead;
      reader.onerror = handleFileError;
      reader.readAsText(file);
    }

    if (fileInputRefCsv.current) {
      fileInputRefCsv.current.value = '';
    }
  };

  const handleSort = (field: string) => {
    setSortConfig((prevConfig) => {
      if (prevConfig.field === field) {
        return {
          field,
          direction: prevConfig.direction === 'asc' ? 'desc' : 'asc',
        };
      } else {
        return { field, direction: 'asc' };
      }
    });
  };

  const value = useMemo(() => ({
    config,
    showNotification,
    formConfigurations,
    setFormConfigurations,
    selectedFormConfiguration,
    setSelectedFormConfiguration,
    filters,
    setFilters,
    sortConfig,
    setSortConfig,
    fetchAgain,
    setFetchAgain,
    createModalOpen,
    setCreateModalOpen,
    deleteModalOpen,
    setDeleteModalOpen,
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
    findLatestTag,
    sortedFormConfigurations,
    onOpenForm
  }), [
    formConfigurations, selectedFormConfiguration, filters, sortConfig, sortedFormConfigurations,
    fetchAgain, createModalOpen, deleteModalOpen, config, fileInputRefJson, fileInputRefCsv
  ]);

  return (
    <DialobDashboardStateContext.Provider value={value}>
      {children}
    </DialobDashboardStateContext.Provider>
  );
};