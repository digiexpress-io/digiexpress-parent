from dataclasses import dataclass
from analyzers.base.sentiment_analyzer import SentimentResponse
from analyzers.base.subcategory_analyzer import SubcategoryResponse
from typing import Optional, Dict, List



@dataclass
class SentimentSubcategoryRequest:
  id: str
  language: Optional[str]
  mainCategory: str
  text: str
  categories: Dict[str, Dict[str, Dict[str, List[str]]]]  # language -> main category -> subcategory -> keywords


@dataclass
class SentimentSubcategoryResponse:
  sentiment: SentimentResponse
  subcategory: SubcategoryResponse