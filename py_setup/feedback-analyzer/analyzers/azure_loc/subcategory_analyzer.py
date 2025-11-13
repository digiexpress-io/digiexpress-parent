import logging
from typing import Dict
from azure.ai.textanalytics import TextAnalyticsClient
from azure.core.credentials import AzureKeyCredential
from azure.identity import DefaultAzureCredential
from analyzers.base.subcategory_analyzer import BaseSubcategoryAnalyzer, SubcategoryRequest, SubcategoryResponse
from utils.text_utils import detect_language, preprocess_text
import config
from datetime import datetime, timezone
from analyzers.azure_loc.versions import subcategory_version

class AzureSubcategoryAnalyzer(BaseSubcategoryAnalyzer):
    
    def __init__(self):
        credential = AzureKeyCredential(config.AZURE_API_KEY) if config.AZURE_API_KEY else DefaultAzureCredential()
        self.client = TextAnalyticsClient(endpoint=config.AZURE_ENDPOINT, credential=credential)
        self.logger = logging.getLogger(__name__)
    
    def _extract_azure_features(self, text: str, language: str) -> Dict[str, set]:
        features = {"key_phrases": set(), "entities": set()}
        
        try:
            # Key phrases
            response = self.client.extract_key_phrases(documents=[{"id": "1", "language": language, "text": text}])[0]
            if not response.is_error:
                key_phrases = [preprocess_text(phrase, language) for phrase in response.key_phrases]
                features["key_phrases"] = set(phrase for phrase in key_phrases if phrase)
        except Exception as e:
            self.logger.warning(f"Key phrase extraction failed: {e}")
        
        try:
            response = self.client.recognize_entities(documents=[{"id": "1", "language": language, "text": text}])[0]
            if not response.is_error:
                entities = [preprocess_text(entity.text, language) for entity in response.entities]
                features["entities"] = set(entity for entity in entities if entity)
        except Exception as e:
            self.logger.warning(f"Entity extraction failed: {e}")
        
        return features
    
    def determine_subcategory(self, request: SubcategoryRequest) -> SubcategoryResponse:
        id = request.id
        language = request.language
        main_category = request.mainCategory
        text = request.text
        categories = request.categories

        try:
            if language is None:
                language = detect_language(text)
            
            # Get categories for language
            language_categories = categories.get(language, {})
            
            # Find matching main category
            matching_category = None
            for category_name in language_categories.keys():
                if (main_category.lower() in category_name.lower() or 
                    category_name.lower() in main_category.lower()):
                    matching_category = category_name
                    break
            
            if not matching_category:
                self.logger.error(f"No matching main category for '{main_category}' in language '{language}'")
                return SubcategoryResponse(
                    id=id,
                    subcategory="unclassified",
                    confidence=0.0,
                    matches=[],
                    scores={},
                    timestamp=datetime.now(timezone.utc).isoformat().replace("+00:00", "Z"),
                    modelVersion=subcategory_version,
                    modelId="azure_subcategory_analyzer"
                )
            
            subcategories = language_categories[matching_category]

            if not subcategories:
                self.logger.error(f"No subcategories found for main category '{main_category}' in language '{language}'")
                return SubcategoryResponse(
                    id=id,
                    subcategory="unclassified",
                    confidence=0.0,
                    matches=[],
                    scores={},
                    timestamp=datetime.now(timezone.utc).isoformat().replace("+00:00", "Z"),
                    modelVersion=subcategory_version,
                    modelId="azure_subcategory_analyzer"
                )
            
            # Extract features from text
            text_features = self._extract_azure_features(text, language)
            text_lower = text.lower()
            processed_text = preprocess_text(text, language)
            
            # Score each subcategory
            subcategory_scores = {}
            
            for subcategory, keywords in subcategories.items():
                score = 0.0
                matches = []
                
                # Direct keyword matching
                for keyword in keywords:
                    if keyword.lower() in text_lower:
                        score += 3.0
                        matches.append(f"direct:{keyword}")
                
                # Feature matching
                for feature_set in text_features.values():
                    for feature in feature_set:
                        for keyword in keywords:
                            if feature.lower() in keyword.lower() or keyword.lower() in feature.lower():
                                score += 1.0
                                matches.append(f"feature:{feature}")
                
                # Word overlap
                for keyword in keywords:
                    keyword_words = set(preprocess_text(keyword, language).split())
                    text_words = set(processed_text.split())
                    overlap = len(keyword_words & text_words)
                    if overlap > 0:
                        score += overlap * 0.5
                        matches.extend([f"word:{word}" for word in keyword_words & text_words])
                
                if score > 0:
                    subcategory_scores[subcategory] = {"score": score, "matches": matches}
            
            if not subcategory_scores:
                self.logger.error(f"Subcategory scoring yielded no results for request with id: {id}")
                return SubcategoryResponse(
                    id=id,
                    subcategory="unclassified",
                    confidence=0.0,
                    matches=[],
                    scores={},
                    timestamp=datetime.now(timezone.utc).isoformat().replace("+00:00", "Z"),
                    modelVersion=subcategory_version,
                    modelId="azure_subcategory_analyzer"
                )
            
            # Find best match
            best_subcategory = max(subcategory_scores.keys(), key=lambda x: subcategory_scores[x]["score"])
            best_score = subcategory_scores[best_subcategory]["score"]
            
            max_possible = len(subcategories[best_subcategory]) * 3.0
            confidence = min(best_score / max_possible, 1.0)

            return SubcategoryResponse(
                    id=id,
                    subcategory=best_subcategory,
                    confidence=confidence,
                    matches=subcategory_scores[best_subcategory]["matches"],
                    scores={k: v["score"] for k, v in subcategory_scores.items()},
                    timestamp=datetime.now(timezone.utc).isoformat().replace("+00:00", "Z"),
                    modelVersion=subcategory_version,
                    modelId="azure_subcategory_analyzer"
                )
            
        except Exception as e:
            self.logger.error(f"Subcategory analysis error: {e} for request with id: {request.id}")
            return SubcategoryResponse(
                    id=id,
                    subcategory="unclassified",
                    confidence=0.0,
                    matches=[],
                    scores={},
                    timestamp=datetime.now(timezone.utc).isoformat().replace("+00:00", "Z"),
                    modelVersion=subcategory_version,
                    modelId="azure_subcategory_analyzer"
                )