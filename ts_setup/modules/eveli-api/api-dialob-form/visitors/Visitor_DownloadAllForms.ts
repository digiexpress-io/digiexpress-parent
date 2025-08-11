import { DialobRestApi } from "../types-rest-api";


export namespace Visitor_DownloadAllForms {

  export interface Input {
    forms: DialobRestApi.FormListItem[]; // List of form IDs to download
  }

  export interface Result {
    totalCount: number;
    successfulForms: DialobRestApi.Form[];
    failedForms: Array<{
      formId: string;
      error: string;
    }>;
  }
}

// The main visitor - downloads all forms as JSON
export class Visitor_DownloadAllForms {
  async accept(backend: DialobRestApi.Backend, context: Visitor_DownloadAllForms.Input): Promise<{ fileName: string, blob: Blob }> {
    try {

      const formIds: string[] = context.forms.map(({id}) => id);
      const downloadedForms = await this.fetchAllForms(formIds, backend);
      const result = this.buildDownloadResult(downloadedForms);

      const fileName = this.generateFileName(result.successfulForms);
      const jsonData = JSON.stringify(result.successfulForms, null, 2);

      
      const blob = new Blob([jsonData], { type: 'application/json' });

      return { fileName, blob };
    } catch (error) {
      throw new Error(`Failed to download forms: ${error instanceof Error ? error.message : 'Unknown error'}`);
    }
  }

  private async fetchAllForms(
    formIds: string[],
    backend: DialobRestApi.Backend
  ): Promise<Array<{ formId: string; form?: DialobRestApi.Form; error?: string }>> {
    const downloadPromises = formIds.map(async (formId) => {
      try {
        const form = await backend.getForm(formId);
        return { formId, form };
      } catch (error) {
        console.error(error);
        return { 
          formId, 
          error: error instanceof Error ? error.message : 'Unknown error' 
        };
      }
    });

    return Promise.all(downloadPromises);
  }

  private buildDownloadResult(
    downloadedForms: Array<{ formId: string; form?: DialobRestApi.Form; error?: string }>
  ): Visitor_DownloadAllForms.Result {

    const successfulForms: DialobRestApi.Form[] = [];
    const failedForms: Array<{ formId: string; error: string }> = [];

    downloadedForms.forEach(({ formId, form, error }) => {
      if (form) {
        successfulForms.push(form);
      } else if (error) {
        failedForms.push({ formId, error });
      }
    });

    return {
      successfulForms: successfulForms,
      failedForms,
      totalCount: successfulForms.length + successfulForms.length,
    };
  }



  private generateFileName(forms: DialobRestApi.Form[]): string {
    if (forms.length === 0) {
      return 'dialobForms_empty.json';
    }
    
    if (forms.length === 1) {
      const form = forms[0];
      const label = form.metadata?.label;
      return label ? `${this.sanitizeFileName(label)}.json` : 'dialobForm.json';
    }
    
    return `dialobForms_${forms.length}_forms.json`;
  }

  private sanitizeFileName(fileName: string): string {
    // Remove or replace invalid filename characters
    return fileName.replace(/[<>:"/\\|?*]/g, '_').trim();
  }
}