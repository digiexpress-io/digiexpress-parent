import { Fs } from '@dxs-ts/fs-api';
import { Position, languages, editor } from 'monaco-editor';
import { Hint_Image } from './Hint_Image';

export interface PrintoutPageContainer {
  pageId: string;
  pageProps: Fs.PrintoutPageProps;
  allProps: Record<string, Fs.Props>;
  model: editor.ITextModel;
  modelPosition: Position;
}

export class PrintoutPageCompletionBuilder {
  private _pageId?: string;
  private _pageProps?: Fs.PrintoutPageProps;
  private _allProps?: Record<string, Fs.Props>;
  private _model?: editor.ITextModel;
  private _modelPosition?: Position;

  withPageId(pageId: string): PrintoutPageCompletionBuilder {
    this._pageId = pageId;
    return this;
  }

  withPageProps(pageProps: Fs.PrintoutPageProps): PrintoutPageCompletionBuilder {
    this._pageProps = pageProps;
    return this;
  }

  withAllProps(allProps: Record<string, Fs.Props>): PrintoutPageCompletionBuilder {
    this._allProps = allProps;
    return this;
  }

  withModel(model: editor.ITextModel): PrintoutPageCompletionBuilder {
    this._model = model;
    return this;
  }

  withPosition(modelPosition: Position): PrintoutPageCompletionBuilder {
    this._modelPosition = modelPosition;
    return this;
  }

  build(): languages.CompletionItem[] {
    if (!this._pageId || !this._pageProps || !this._allProps || !this._model || !this._modelPosition) {
      return [];
    }

    const container: PrintoutPageContainer = {
      pageId: this._pageId,
      pageProps: this._pageProps,
      allProps: this._allProps,
      model: this._model,
      modelPosition: this._modelPosition,
    };

    const result: languages.CompletionItem[] = [];

    result.push(...Hint_Image.accept(container));

    return result;
  }
}
