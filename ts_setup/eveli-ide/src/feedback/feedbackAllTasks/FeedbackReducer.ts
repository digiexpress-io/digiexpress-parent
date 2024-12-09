import { FeedbackApi } from '../feedback-api';

export class FeedbackReducer {
  private _data: FeedbackApi.Feedback[];


  private _visibleRows: FeedbackApi.Feedback[];

  private _filterByCategory: string | undefined;
  private _filterBySubCategory: string | undefined;


  private _categories: string[];
  private _subcategories: string[];
  private _searchString: string | undefined;


  constructor(props: {
    data: FeedbackApi.Feedback[],
    filterByCategory?: string | undefined,
    filterBySubCategory?: string | undefined,
    searchString?: string | undefined
  }) {

    this._data = props.data;
    this._categories = Array.from(new Set(props.data.map(item => item.labelValue))); // uniqueCategories
    this._subcategories = Array.from(new Set(props.data
      .filter(item => props.filterByCategory ? item.labelValue === props.filterByCategory : true)
      .filter(item => item.subLabelValue)
      .map(item => item.subLabelValue!))); // uniqueSubCategories

    this._filterByCategory = props.filterByCategory;
    this._filterBySubCategory = props.filterBySubCategory;
    this._searchString = props.searchString

    this._visibleRows = this._data
      .filter(item => this._filterByCategory ? item.labelValue === this._filterByCategory : true) //by category
      .filter(item => this._filterBySubCategory ? item.subLabelValue === this._filterBySubCategory : true) //by sub category
      .filter(item => {

        if (!this._searchString) {
          return true;
        }

        if (item.id === this._searchString) {
          return true;
        }

        if (item.content.toLocaleLowerCase().indexOf(this._searchString) > -1) {
          return true;
        }

        return false;
      })
  }

  get searchBy() { return this._searchString }
  get visibleRows() { return this._visibleRows }
  get categories() { return this._categories }
  get subcategories() { return this._subcategories }

  get filterByCategory() { return this._filterByCategory }
  get filterBySubCategory() { return this._filterBySubCategory }

  withFilterByCategory(filterByCategory: string | undefined): FeedbackReducer {
    return new FeedbackReducer({
      data: this._data,
      filterByCategory: filterByCategory,
      filterBySubCategory: this._filterBySubCategory,
      searchString: this._searchString
    });
  }
  withFilterBySubCategory(filterBySubCategory: string | undefined): FeedbackReducer {
    return new FeedbackReducer({
      data: this._data,
      filterByCategory: this._filterByCategory,
      filterBySubCategory: filterBySubCategory,
      searchString: this._searchString
    });
  }
  withSearchBy(searchString: string | undefined): FeedbackReducer {
    return new FeedbackReducer({
      data: this._data,
      filterByCategory: this._filterByCategory,
      filterBySubCategory: this._filterBySubCategory,
      searchString: searchString
    });
  }

  withData(data: FeedbackApi.Feedback[]): FeedbackReducer {
    return new FeedbackReducer({
      data: data,
      filterByCategory: this._filterByCategory,
      filterBySubCategory: this._filterBySubCategory,
      searchString: this._searchString
    });
  }
}