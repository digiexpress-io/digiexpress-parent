import { DialobRestApi } from "../types-rest-api";



export namespace Visitor_UploadFormJson {

  export interface Input {
    file: File;
    allForms: DialobRestApi.FormListItem[];
  }

  export interface Result {
    success: boolean;
    message: string;
    isExisting?: boolean;
    error?: string;
    metadata?: {
      fileName: string;
      fileSize: number;
      uploadTime: Date;
      formName?: string;
    };
  }
}

// The main visitor - orchestrates the entire pipeline
export class Visitor_UploadFormJson {
  private startTime = new Date();

  async accept(backend: DialobRestApi.Backend, context: Visitor_UploadFormJson.Input): Promise<Visitor_UploadFormJson.Result> {
    try {
      // File validation
      this.validateFile(context.file);

      // File reading - sync using FileReader
      const fileContent = this.readFileSync(context.file);

      // JSON parsing
      const parsedJson = this.parseJson(fileContent);

      // Data preparation
      const cleanedData = this.prepareData(parsedJson);

      // Update/Create ?
      const formNamesList = context.allForms.map(form => form.id) || [];
      const isExisting = formNamesList.includes(cleanedData.name);

      // Upload form
      const uploadResponse = this.uploadForm(backend, cleanedData, context, isExisting);

      return this.buildSuccessResult(context, cleanedData, isExisting);

    } catch (error) {
      return this.handleFailure(
        error instanceof Error ? error.message : 'Unknown error',
        context.file?.name || 'unknown'
      );
    }
  }

  private validateFile(file: File): void {
    if (!file) {
      throw new Error('No file selected');
    }
    if (!file.name.endsWith('.json')) {
      throw new Error('File must be a JSON file');
    }
  }

  private readFileSync(file: File): string {
    // Use synchronous approach with FileReaderSync in worker or throw for now
    throw new Error('Synchronous file reading not implemented - use async version');
  }

  private parseJson(content: string): any {
    try {
      const json = JSON.parse(content);
      if (Array.isArray(json)) {
        throw new Error('JSON must contain an object, not an array');
      }
      return json;
    } catch (error) {
      throw new Error(`Invalid JSON: ${error instanceof Error ? error.message : 'Parse error'}`);
    }
  }

  private prepareData(json: any): any {
    if (!json || typeof json !== 'object') {
      throw new Error('Invalid form data structure');
    }

    const cleanedData = { ...json };
    delete cleanedData._id;
    delete cleanedData._rev;

    if (!cleanedData.name) {
      throw new Error('Form must have a name property');
    }

    return cleanedData;
  }


  private async uploadForm(backend: DialobRestApi.Backend, formData: any, context: Visitor_UploadFormJson.Input, isExisting: boolean): Promise<any> {
    try {


      const uploadFn = isExisting
        ? backend.updateForm(formData.name, formData)
        : backend.createForm(formData);

      const response = uploadFn;

      // Handle response directly - no mystery utility functions
      if (!response) {
        throw new Error('Upload failed - no response received');
      }

      return response;
    } catch (error) {
      throw new Error(`Upload failed: ${error instanceof Error ? error.message : 'Unknown error'}`);
    }
  }

  private buildSuccessResult(context: Visitor_UploadFormJson.Input, formData: any, isExisting: boolean): Visitor_UploadFormJson.Result {
    return {
      success: true,
      message: `Successfully uploaded ${isExisting ? 'existing' : 'new'} form: ${formData.name}`,
      isExisting,
      metadata: {
        fileName: context.file.name,
        fileSize: context.file.size,
        uploadTime: this.startTime,
        formName: formData.name
      }
    };
  }

  private handleFailure(error: string, fileName: string): Visitor_UploadFormJson.Result {
    return {
      success: false,
      message: error,
      error,
      metadata: {
        fileName,
        fileSize: 0,
        uploadTime: this.startTime
      }
    };
  }
}