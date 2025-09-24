import { DialobRestApi } from "../types-rest-api";



export namespace Visitor_UploadCsvForm {
  export interface Input {
    file: File;
  }

  export interface Result {
    success: boolean;
    formId?: string;
    message: string;
    error?: string;
    metadata?: {
      fileName: string;
      fileSize: number;
      uploadTime: Date;
    };
  }
}

// The main visitor - handles CSV form upload
export class Visitor_UploadCsvForm {
  async accept(backend: DialobRestApi.Backend, context: Visitor_UploadCsvForm.Input): Promise<Visitor_UploadCsvForm.Result> {
    try {
      // Validate file
      this.validateCsvFile(context.file);
      
      // Read CSV content
      const csvContent = await this.readCsvFile(context.file);
      
      // Upload via backend
      const response = await this.uploadCsvToBackend(backend, context, csvContent);
      
      return this.buildSuccessResult(context.file, response);
      
    } catch (error) {
      return this.buildFailureResult(context.file, error);
    }
  }

  private validateCsvFile(file: File): void {
    if (!file) {
      throw new Error('No file selected');
    }
    
    if (!file.name.toLowerCase().endsWith('.csv')) {
      throw new Error('File must be a CSV file');
    }
    
    if (file.size === 0) {
      throw new Error('File is empty');
    }
    
    // Optional: Add file size limit
    const maxSize = 10 * 1024 * 1024; // 10MB
    if (file.size > maxSize) {
      throw new Error('File is too large (max 10MB)');
    }
  }

  private async readCsvFile(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      
      reader.onload = (event) => {
        const result = event.target?.result;
        if (typeof result === 'string') {
          resolve(result);
        } else {
          reject(new Error('Failed to read file as text'));
        }
      };
      
      reader.onerror = () => {
        reject(new Error('Error reading file'));
      };
      
      reader.readAsText(file);
    });
  }

  private async uploadCsvToBackend(
    backend: DialobRestApi.Backend,
    context: Visitor_UploadCsvForm.Input, 
    csvContent: string
  ): Promise<DialobRestApi.ApiResponse> {
    try {
      return await backend.createFormFromCsv(csvContent);
    } catch (error) {
      // Re-throw with more context
      throw new Error(`CSV upload failed: ${error instanceof Error ? error.message : 'Unknown error'}`);
    }
  }

  private buildSuccessResult(file: File, response: DialobRestApi.ApiResponse): Visitor_UploadCsvForm.Result {
    const formId = response.id;
    const message = formId 
      ? `Uploaded CSV form successfully. ID: ${formId}`
      : 'Uploaded CSV form successfully';

    return {
      success: true,
      formId,
      message,
      metadata: {
        fileName: file.name,
        fileSize: file.size,
        uploadTime: new Date(),
      },
    };
  }

  private buildFailureResult(file: File, error: unknown): Visitor_UploadCsvForm.Result {
    const errorMessage = error instanceof Error ? error.message : 'Unknown error occurred';
    
    return {
      success: false,
      message: `Error while uploading CSV form: ${errorMessage}`,
      error: errorMessage,
      metadata: {
        fileName: file.name,
        fileSize: file.size,
        uploadTime: new Date(),
      },
    };
  }
}
