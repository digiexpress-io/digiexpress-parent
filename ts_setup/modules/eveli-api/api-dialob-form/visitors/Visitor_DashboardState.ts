import { DashboardState, DashboardItem } from "../types-dashboard";
import { DialobRestApi } from "../types-rest-api";




export namespace Visitor_DashboardState {
  
}

// The main visitor - orchestrates the entire pipeline
export class Visitor_DashboardState {
  async accept(backend: DialobRestApi.Backend): Promise<DashboardState> {
    
   try {
      // Get forms and tags in parallel
      const [forms, tags] = await Promise.all([
        backend.findAllForms(),
        backend.findAllTags()
      ]);


      // Enrich forms with latest tag information
      const items: DashboardItem[] = forms.map(form => {
        const latestTag = this.findLatestTag(tags, form);
        
        if (latestTag) {
          const result: DashboardItem = {
            ...form,
            latestTagName: latestTag.name,
            latestTagDate: latestTag.created,
          };
          return result;
        }
        return form;
      });

      return {
        forms,
        tags,
        items,
        loadedAt: new Date(),
      };

    } catch (error) {
      console.error(error)
      throw new Error(`Failed to load dashboard state: ${error instanceof Error ? error.message : 'Unknown error'}`);
    }
  }

  private findLatestTag(
    tags: DialobRestApi.FormTag[], 
    form: DialobRestApi.FormListItem
  ): DialobRestApi.FormTag | undefined {
    if (!form.id) {
      return undefined;
    }

    const formTags = tags.filter(tag => tag.formName === form.id);
    
    if (formTags.length === 0) return undefined;

    // Sort by creation date descending and return the latest
    return formTags.sort((a, b) => {
      if (!a.created && !b.created) return 0;
      if (!a.created) return 1;
      if (!b.created) return -1;
      return new Date(b.created).getTime() - new Date(a.created).getTime();
    })[0];
  }
}