import { SiteApi } from "../api-site";
import { SearchApi } from "./search-types";


class SearchReducer {
  private _source: readonly SiteApi.TopicView[];
  private _searchString: string | undefined;
  private _searchOptionType: SearchApi.FilterMode;
  private _noValueIndicatorColon: string;

  private _forms: SearchApi.LinkToForm[] = [];
  private _internal: SiteApi.TopicLink[] = [];
  private _external: SiteApi.TopicLink[] = [];
  private _phones: SiteApi.TopicLink[] = [];
  private _topics: SiteApi.TopicView[] = [];

  constructor(
    source: readonly SiteApi.TopicView[],
    searchString: string | undefined,
    searchOptionType: SearchApi.FilterMode,
    noValueIndicatorColon: string
  ) {
    this._source = source;
    this._searchString = searchString?.toLocaleLowerCase();
    this._searchOptionType = searchOptionType;
    this._noValueIndicatorColon = noValueIndicatorColon;
  }

  accept(): {
    forms: SearchApi.LinkToForm[];
    internal: SiteApi.TopicLink[];
    external: SiteApi.TopicLink[];
    phones: SiteApi.TopicLink[];
    topics: SiteApi.TopicView[];
  } {
    this._source.forEach(view => this.visitView(view))
    return {
      external: this._external.sort((a, b) => a.name.localeCompare(b.name)),
      forms: this._forms.sort((a, b) => a.label.localeCompare(b.label)),
      internal: this._internal.sort((a, b) => a.name.localeCompare(b.name)),
      phones: this._phones.sort((a, b) => a.name.localeCompare(b.name)),
      topics: this._topics.sort((a, b) => a.order - b.order),
    };
  }
  private visitView(view: SiteApi.TopicView) {
    this.visitTopic(view);

    for (const link of view.links) {
      if (link.type === 'external') {
        this.visitExternal(link);
      } else if (link.type === 'phone') {
        this.visitPhone(link);
      } else if (link.type === 'workflow') {
        this.visitForm(view, link);
      } else if (link.type === 'internal') {
        this.visitInternal(link);
      }
    }
  }

  // GET ALL TOPICS
  private visitTopic(topic: SiteApi.TopicView) {

    if (this._searchString) {
      const foundTopic = topic.name.toLocaleLowerCase().indexOf(this._searchString) > -1;
      const found = foundTopic;

      if (!found) {
        return;
      }
    }

    // ENABLE/DISABLE
    const enabled = this._searchOptionType === "TOPICS" || this._searchOptionType === "ALL";
    if (!enabled) {
      return;
    }
    this._topics.push(topic);
  }

  private visitForm(topic: SiteApi.TopicView, linkToForm: SiteApi.TopicLink) {

    if (this._searchString) {
      const foundTopic = topic.name.toLocaleLowerCase().indexOf(this._searchString) > -1;
      const foundForm = linkToForm.name.toLocaleLowerCase().indexOf(this._searchString) > -1;
      const found = foundTopic || foundForm;

      if (!found) {
        return;
      }
    }

    // ENABLE/DISABLE
    const enabled = this._searchOptionType === "FORM_LINKS" || this._searchOptionType === "ALL";
    if (!enabled) {
      return;
    }

    const form: SearchApi.LinkToForm = { linkToForm, topic, label: linkToForm.name + this._noValueIndicatorColon + topic.name };
    this._forms.push(form);
  }


  // FILTER INTERNAL LINK
  private visitInternal(internal: SiteApi.TopicLink) {

    if (this._searchString) {
      const foundLink = internal.name.toLocaleLowerCase().indexOf(this._searchString) > -1;
      const found = foundLink;

      if (!found) {
        return;
      }
    }

    // ENABLE/DISABLE
    const enabled = this._searchOptionType === "LINKS" || this._searchOptionType === "ALL";
    if (!enabled) {
      return;
    }
    if (this._internal.some(existingLink => existingLink.value === internal.value)) {
      return;
    }
    this._internal.push(internal);
  }

  // FILTER EXT LINK
  private visitExternal(external: SiteApi.TopicLink) {

    if (this._searchString) {
      const foundLink = external.name.toLocaleLowerCase().indexOf(this._searchString) > -1;
      const found = foundLink;

      if (!found) {
        return;
      }
    }


    // ENABLE/DISABLE
    const enabled = this._searchOptionType === "LINKS" || this._searchOptionType === "ALL";
    if (!enabled) {
      return;
    }

    if (this._external.some(existingLink => existingLink.value === external.value)) {
      return;
    }
    this._external.push(external);
  }


  // FILTER PHONE NUMBER LINK
  private visitPhone(phone: SiteApi.TopicLink) {

    if (this._searchString) {
      const foundPhone = phone.value.toLocaleLowerCase().indexOf(this._searchString) > -1;
      const found = foundPhone;

      if (!found) {
        return;
      }
    }

    // ENABLE/DISABLE
    const enabled = this._searchOptionType === "PHONE_LINKS" || this._searchOptionType === "ALL";
    if (!enabled) {
      return;
    }

    if (this._phones.some(existingLink => existingLink.value === phone.value)) {
      return;
    }
    this._phones.push(phone);
  }
}


export class SearchStateImpl implements SearchApi.SearchState {
  private _source: readonly SiteApi.TopicView[];
  private _searchString: string | undefined;
  private _searchOptionType: SearchApi.FilterMode;
  private _noValueIndicatorColon: string;

  private _forms: readonly SearchApi.LinkToForm[];
  private _internal: readonly SiteApi.TopicLink[];
  private _external: readonly SiteApi.TopicLink[];
  private _phones: readonly SiteApi.TopicLink[];
  private _topics: readonly SiteApi.TopicView[];

  constructor(props: {
    source: readonly SiteApi.TopicView[],
    noValueIndicatorColon: string,
    searchString?: string | undefined,
    searchOptionType?: SearchApi.FilterMode | undefined,
  }
  ) {
    this._source = props.source;
    this._searchString = props.searchString;
    this._searchOptionType = props.searchOptionType ?? 'ALL';
    this._noValueIndicatorColon = props.noValueIndicatorColon;
    const search = new SearchReducer(this._source, this._searchString, this.searchOptionType, this._noValueIndicatorColon).accept();
    this._forms = search.forms;
    this._external = search.external;
    this._internal = search.internal;
    this._phones = search.phones;
    this._topics = search.topics;

  }
  find(newSearchString: string): SearchApi.SearchState {
    return new SearchStateImpl({ source: this._source, searchString: newSearchString, searchOptionType: this._searchOptionType, noValueIndicatorColon: this._noValueIndicatorColon });
  }
  filterMode(type: SearchApi.FilterMode): SearchApi.SearchState {
    return new SearchStateImpl({ source: this._source, searchString: this.searchString, searchOptionType: type, noValueIndicatorColon: this._noValueIndicatorColon });
  }
  get searchString() { return this._searchString }
  get searchOptionType() { return this._searchOptionType }
  get forms() { return this._forms }
  get internal() { return this._internal }
  get external() { return this._external }
  get phones() { return this._phones }
  get topics() { return this._topics }


}