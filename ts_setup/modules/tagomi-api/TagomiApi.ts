
export namespace TagomiApi {

  export interface StoreError extends Error {
    text: string;
    status: number;
    errors: TagomiApi.ErrorMsg[];
  }
  
  
  export class StoreErrorImpl extends Error {
    private _props: TagomiApi.ErrorProps;
    constructor(props: TagomiApi.ErrorProps) {
      super(props.text);
      this._props = {
        text: props.text,
        status: props.status,
        errors: parseErrors(props.errors)
      };
    }
    get name() {
      return this._props.text;
    }
    get status() {
      return this._props.status;
    }
    get errors() {
      return this._props.errors;
    }
  }

  const getErrorMsg = (error: any) => {
    if (error.msg) {
      return error.msg;
    }
    if (error.value) {
      return error.value
    }
    if (error.message) {
      return error.message;
    }
  }
  const getErrorId = (error: any) => {
    if (error.id) {
      return error.id;
    }
    if (error.code) {
      return error.code
    }
    return "";
  }
  export const parseErrors = (props: any[]): TagomiApi.ErrorMsg[] => {
    if (!props) {
      return []
    }
  
    const result: TagomiApi.ErrorMsg[] = props.map(error => ({
      id: getErrorId(error),
      value: getErrorMsg(error)
    }));
  
    return result;
  }  
}


export declare namespace TagomiApi {

  export type LocalisedContent = string;
  export type TemplateId = string;
  export type ServiceId = string;
  export type LocaleId = string;
  export type ResourceId = string;

  /**
   * Tagomi API Type Definitions
   * 
   * This file contains TypeScript interfaces converted from Java POJOs.
   * Represents the data structures for a PDF generation system with:
   * - Services: Final products that link templates and data sources
   * - Templates: Markdown-based PDF layouts for specific locales
   * - Resources: Static assets (images, etc.) embedded in printouts
   * - Locales: Language configurations
   * - Tags: Version control metadata
   * 
   * Commands are mutation operations following CQRS pattern.
   */

  export type TagomiDocType = 
    | 'TEMPLATE'
    | 'RESOURCE'
    | 'SERVICE'
    | 'LOCALE'
    | 'TAG';

  export interface TagomiContainer {
    tagName: string;
    commitId?: string;
    commitAt?: string; // ISO 8601 datetime string
    tags: Record<string, Tag>;
    locales: Record<string, Locale>;
    templates: Record<string, Template>;
    resources: Record<string, Resource>;
    services: Record<string, Service>;
  }

  export interface Service {  // container for locales/localised content
    id: string;
    docType: 'SERVICE';
    serviceName: string;
    orchestratorName: string; // flow name where content data comes from
    labels: LocaleAndLabel[];
  }

  export interface Template {
    id: string;
    docType: 'TEMPLATE';
    content: string;
    localeId: string;
    serviceId: string;
  }

  export interface Resource { // aka image to include in content
    id: string;
    docType: 'RESOURCE';
    externalLocation: string;
    resourceName: string;
    contentType: string;
    templateIds: string[];
  }

  export interface Locale {
    id: string;
    docType: 'LOCALE';
    localeCode: string;
    enabled: boolean;
    default: boolean;
  }

  export interface Tag {
    id: string;
    docType: 'TAG';
    commitId: string;
    name: string;
    note: string;
  }

  export interface LocaleAndLabel {
    locale: string;
    labelValue: string;
  }

  // ============================================================================
  // Commands - Write Operations
  // ============================================================================

  export interface CreateLocale {
    id?: string;
    localeCode: string;
  }

  export interface CreateTemplate {
    id?: string;
    serviceId: string;
    locale: string;
    content?: string;
  }

  export interface CreateResource {
    id?: string;
    resourceName: string;
    contentType: string;
    uploadBody: string; // Base64 encoded bytes
    templateIds: string[];
  }

  export interface CreateService {
    id?: string;
    labels: LocaleAndLabel[];
    serviceName: string;
    orchestratorName: string;
  }

  export interface CreateTag {
    id?: string;
    tagName: string;
    note: string;
    commitId?: string;
  }

  // ============================================================================
  // Mutators - Update Operations
  // ============================================================================

  export interface LocaleMutator {
    localeId: string;
    value: string;
    enabled: boolean;
  }

  export interface TemplateMutator {
    templateId: string;
    content: string;
    locale?: string;
    resourceIds?: string[];
  }

  export interface ResourceMutator {
    resourceId: string;
    contentType?: string;
    resourceName?: string;
    uploadBody?: string; // Base64 encoded bytes
    templateIds?: string[];
  }

  export interface ServiceMutator {
    serviceId: string;
    serviceName: string;
    orchestratorName: string;
    labels?: LocaleAndLabel[];
  }

  // ============================================================================
  // REST API Interface
  // ============================================================================

  export interface Backend {
    // Query operations
    getSites(): Promise<TagomiContainer>;
    
    // Resource operations
    createResource(body: CreateResource): Promise<Resource>;
    updateResource(body: ResourceMutator): Promise<Resource>;
    deleteResource(linkId: string, articleId?: string): Promise<Resource>;
    
    // Service operations
    createService(body: CreateService): Promise<Service>;
    updateService(body: ServiceMutator): Promise<Service>;
    deleteService(serviceId: string): Promise<Service>;
    
    // Locale operations
    createLocale(body: CreateLocale): Promise<Locale>;
    updateLocale(body: LocaleMutator): Promise<Locale>;
    deleteLocale(id: string): Promise<Locale>;
    
    // Template operations
    createTemplate(body: CreateTemplate): Promise<Template>;
    updateTemplate(body: TemplateMutator[]): Promise<Template[]>;
    deleteTemplate(id: string): Promise<Template>;
    
    // Tag operations
    createTag(body: CreateTag): Promise<Tag>;
    deleteTag(id: string): Promise<Tag>;
  }
  
  export interface ErrorMsg {
    id: string;
    value: string;
  }

  export interface ErrorProps {
    text: string;
    status: number;
    errors: any[];
  }
}