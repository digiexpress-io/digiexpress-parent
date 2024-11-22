import React from 'react';


export interface ItemProps {
  item: any,
  answerId?: string | null,
  answer?: any
}

export interface ItemConfigEntry {
  matcher: (item: any, isMainGroupItem: boolean) => boolean;
  component: React.FC<ItemProps>;
  answerRequired: boolean;
  childrenRequired: boolean;
}

export interface ItemconfigType {
  items: ItemConfigEntry[]
}


export class GFormReviewContextType {
  private questionnaire: {
    context: { id: string, value: any }[];
    variableValues: { id: string, value: any }[];
    answers: { id: string, value: any }[];
  };
  private form: {
    valueSets: { id: string, entries: { id: string, label?: Record<string, string> }[] }[];
    data: Record<string, { id: string, items?: string[] }>;
  };
  private language: string;
  private config: ItemconfigType;

  constructor(questionnaire: any, form: any, language: string, config: ItemconfigType) {
    this.questionnaire = questionnaire;
    this.form = form;
    this.language = language;
    this.config = config;
  }

  private trim(string: string) {
    if (!string) {
      return '-';
    }
    return string;
  }

  private replaceVariable(varName: string) {
    let questionnaireValue = this.questionnaire.context.find(context => context.id === varName);
    if (questionnaireValue) {
      return questionnaireValue.value;
    }
    questionnaireValue = this.questionnaire.variableValues.find(context => context.id === varName);
    if (questionnaireValue) {
      return questionnaireValue.value;
    }
    questionnaireValue = this.questionnaire.answers.find(context => context.id === varName);
    if (questionnaireValue) {
      return questionnaireValue.value;
    }
  }

  public substituteVariables(string: string) {
    return string.replace(/\{(.+?)\}/g, (_match, p1) => { return this.trim(this.replaceVariable(p1)) });
  }

  public getAnswer(itemId: string, answerId: string | null = null): any | null {
    let aID = answerId ? answerId : itemId;
    let answer = this.questionnaire.answers.find(e => e.id === aID);
    if (answer) {
      return answer.value;
    }
    return null;
  }

  public getTranslated(value?: Record<string, string> | any): any {
    return (value && value[this.language]) || '';
  }

  public createItem(itemId: string, answerId: string | null = null, isMainGroupItem: boolean = false): JSX.Element | null {
    const item = this.form.data[itemId];
    let configItem: ItemConfigEntry | undefined = this.config.items.find(c => c.matcher(item, isMainGroupItem));
    if (!configItem) {
      return null;
    }
    const ComponentType: React.FC<ItemProps> = configItem.component;
    if (!ComponentType) {
      console.warn(`Component type not defined for ${itemId}`);
      return null;
    }
    if (configItem.answerRequired) {
      const answer = this.getAnswer(item.id, answerId);
      if (answer === null) {
        // skip unanswered
        return null;
      }
      return (<ComponentType key={item.id} item={item} answerId={answerId} answer={answer} />);
    }
    else {
      if (configItem.childrenRequired) {
        const items = item.items ? item.items.map(item => this.createItem(item)).filter(item => item) : null;
        if (!items || items.length === 0) {
          // skip empty
          return null;
        }
        // TODO: Give child items as props
      }
      return (<ComponentType key={item.id} item={item} />);
    }
  }

  public findValueSet(valueSetId: string) {
    return this.form.valueSets.find(vs => vs.id === valueSetId);
  }

  public getItem(itemId: string) {
    return this.form.data[itemId];
  }

  public getLanguage() {
    return this.language;
  }

}

const defaultContext: GFormReviewContextType = (undefined as unknown) as GFormReviewContextType;

const GFormReviewContext = React.createContext<GFormReviewContextType>(defaultContext);

export { GFormReviewContext }