import { TagomiApi } from '@dxs-ts/tagomi-api';
import { Position, languages, editor } from 'monaco-editor';
import { Hint_ImportScript } from './Hint_ImportScript';
import { Hint_Image } from './Hint_Image';
import { Hint_IncludeTemplate } from './Hint_IncludeTemplate';

export interface TagomiContainer {
  site: TagomiApi.TagomiContainer;
  templateId: TagomiApi.TemplateId;
  model: editor.ITextModel;
  modelPosition: Position;
}

export class TagomiCompletionBuilder {
  private _site?: TagomiApi.TagomiContainer;
  private _templateId?: TagomiApi.TemplateId;
  private _model?: editor.ITextModel;
  private _modelPosition?: Position;

  withSite(site: TagomiApi.TagomiContainer): TagomiCompletionBuilder {
    this._site = site;
    return this;
  }

  withTemplateId(templateId: TagomiApi.TemplateId): TagomiCompletionBuilder {
    this._templateId = templateId;
    return this;
  }

  withModel(model: editor.ITextModel): TagomiCompletionBuilder {
    this._model = model;
    return this;
  }

  withPosition(modelPosition: Position): TagomiCompletionBuilder {
    this._modelPosition = modelPosition;
    return this;
  }

  build(): languages.CompletionItem[] {
    if (!this._site || !this._templateId || !this._model || !this._modelPosition) {
      return [];
    }

    const container: TagomiContainer = {
      site: this._site,
      templateId: this._templateId,
      model: this._model,
      modelPosition: this._modelPosition
    };

    const result: languages.CompletionItem[] = [];

    result.push(...Hint_Image.accept(container));
    result.push(...Hint_ImportScript.accept(container));
    result.push(...Hint_IncludeTemplate.accept(container));

    return result;
  }
}
