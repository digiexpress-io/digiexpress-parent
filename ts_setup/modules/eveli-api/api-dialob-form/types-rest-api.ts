export declare namespace DialobRestApi {
  // Core API Response Types
  export type ValidationLevel = 'INFO' | 'WARNING' | 'ERROR' | 'FATAL';

  export type ValidationType = 
    | 'VISIBILITY' 
    | 'VALIDATION' 
    | 'REQUIREMENT' 
    | 'VARIABLE' 
    | 'GENERAL' 
    | 'CLASSNAME' 
    | 'VALUE_ENTRY' 
    | 'VALUESET' 
    | 'VALUESET_ENTRY' 
    | 'CANADDROW' 
    | 'CANREMOVEROW';

  export type FormTagType = 'NORMAL' | 'MUTABLE';

  export interface FormValidationError {
    itemId?: string;
    message?: string;
    level?: ValidationLevel; // Defaults to ERROR
    type?: ValidationType;
    expression?: string;
    startIndex?: number;
    endIndex?: number;
    index?: number;
  }

  export interface RestApiErrorResponse {
    // Top-level error info
    timestamp?: Date;
    status?: number;
    error?: string;
    message?: string;
    trace?: string;
    path?: string;
    // Individual validation/field errors
    errors?: Array<{
      code?: string;
      context?: string;
      rejectedValue?: any;
      error?: string;
    }>;
  }

  export interface ApiResponse {
    ok?: boolean;
    error?: string;
    reason?: string;
    // FormPutResponse specific fields
    id?: string;
    rev?: string;
    errors?: FormValidationError[];
    form?: Form;
  }

  // Form Structure Types
  export interface FormMetadata {
    label: string;
    created?: Date;
    lastSaved?: Date;
    valid?: boolean;
    creator?: string;
    tenantId?: string;
    savedBy?: string;
    labels: string[]; // Set<String> -> string[]
    defaultSubmitUrl?: string;
    languages: string[]; // Set<String> -> string[]
    additionalProperties: Record<string, any>;
  }

  export interface Validation {
    message: Record<string, string>; // Multilingual error messages
    rule?: string; // Validation expression
  }

  export interface FormValueSetEntry {
    id: string;
    label?: Record<string, string>; // Multilingual labels
    when?: string; // Conditional display
    additionalProperties: Record<string, any>;
  }

  export interface FormValueSet {
    id: string;
    entries?: FormValueSetEntry[];
    additionalProperties: Record<string, any>;
  }

  export interface Variable {
    name: string;
    expression?: string;
    defaultValue?: any;
    context?: boolean; // Is this a context variable
    published?: boolean; // Can be sent to client
    contextType?: string;
    description?: string;
  }

  export interface FormItem {
    id: string;
    type: string;
    view?: string;
    label: Record<string, string>; // Multilingual labels
    description: Record<string, string>; // Multilingual descriptions
    required?: string;
    requiredErrorText: Record<string, string>;
    readOnly?: boolean;
    items: string[]; // Child items for groups/containers
    className: string[];
    activeWhen?: string; // Conditional visibility
    canAddRowWhen?: string; // For dynamic lists
    canRemoveRowWhen?: string; // For dynamic lists
    validations: Validation[];
    valueSetId?: string;
    defaultValue?: any;
    props?: Record<string, any>; // Custom properties
    additionalProperties: Record<string, any>;
  }

  export interface Form {
    _id?: string;
    _rev?: string;
    name?: string;
    data: Record<string, FormItem>; // Map<String, FormItem>
    metadata: FormMetadata;
    variables: Variable[];
    namespaces: Record<string, Form>; // Map<String, Form> - nested forms
    valueSets: FormValueSet[];
    requiredErrorText: Record<string, string>; // Map<String, String>
  }

  export interface FormListItem {
    id: string;
    metadata: FormMetadata; // Uses the same metadata as Form
  }

  export interface FormTag {
    formName: string; // grouping column
    name: string;
    refName?: string;
    created?: Date;
    formId?: string;
    description?: string;
    creator?: string;
    type?: FormTagType; // Defaults to NORMAL
  }

  // API Configuration Types
  export interface DialobAdminConfig {
    dialobApiUrl: string;
    tenantId?: string;
  }

  // API Filter Types
  export interface TagFilters {
    formName?: string;
    formId?: string;
    name?: string;
  }

  export interface UpdateFormOptions {
    oldId?: string;
    newId?: string;
    force?: boolean;
    dryRun?: boolean;
  }


  export interface CreateFormRequest {
    name: string,
    data: {
      questionnaire: {
        id: 'questionnaire',
        type: 'questionnaire',
        items: string[]
      }
    },
    metadata: {
      label: string,
      languages: string[]
    }
  }


  // Backend API interface - all the async operations
  export interface Backend {
    // List operations
    findAllForms: () => Promise<FormListItem[]>; // GET /api/forms
    findAllTags: (filters?: TagFilters) => Promise<FormTag[]>; // GET /api/tags?formName=...&formId=...&name=...
    
    // Single operations
    getForm: (formId: string, rev?: string) => Promise<Form>; // GET /api/forms/{formId}?rev=...
    getFormTags: (formId: string) => Promise<FormTag[]>; // GET /api/forms/{formId}/tags
    
    // CRUD operations
    createForm: (form: CreateFormRequest | Form) => Promise<Form>; // POST /api/forms
    updateForm: (formId: string, form: Form, options?: UpdateFormOptions) => Promise<ApiResponse>; // PUT /api/forms/{formId}?force=true&...
    deleteForm: (formId: string) => Promise<ApiResponse>; // DELETE /api/forms/{formId}
    
    // Special operations
    createFormFromCsv: (csvData: string) => Promise<ApiResponse>; // POST /api/forms (Content-Type: text/csv)
  }
}