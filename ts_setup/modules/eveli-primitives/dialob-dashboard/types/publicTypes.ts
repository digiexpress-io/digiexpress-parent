export interface CsrfShape {
  key: string;
  value: string;
}

export interface DialobAdminConfig {
  dialobApiUrl: string;
  setLoginRequired: () => void;
  setTechnicalError: () => void;
  language: string;
  csrf?: CsrfShape;
  tenantId?: string;
}

export interface DialobAdminProps {
  config: DialobAdminConfig;
  showNotification?: (message: string, severity: 'success' | 'error') => void;
  onOpenForm?: (formId: string) => void;
}