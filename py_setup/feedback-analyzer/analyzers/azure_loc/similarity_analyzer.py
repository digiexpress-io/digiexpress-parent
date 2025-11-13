import logging
import re
from typing import List, Set, Dict, Any
from azure.core.credentials import AzureKeyCredential
from azure.ai.textanalytics import TextAnalyticsClient
from analyzers.base.similarity_analyzer import BaseSimilarityAnalyzer
from utils.text_utils import detect_language, preprocess_text
from datetime import datetime, timezone
from analyzers.base.similarity_analyzer import SimilarityRequest, SimilarityResponse, ProcessedEntry, EntrySimilarityResult
from analyzers.azure_loc.versions import similarity_version
import traceback
import config

class AzureSimilarityAnalyzer(BaseSimilarityAnalyzer):
    
    def __init__(self, credential=None, debug_mode=True):
        if credential is None:
            from azure.identity import DefaultAzureCredential
            credential = AzureKeyCredential(config.AZURE_API_KEY) if config.AZURE_API_KEY else DefaultAzureCredential()
        
        self.client = TextAnalyticsClient(
            endpoint=config.AZURE_ENDPOINT,
            credential=credential
        )
        
        # Setup logging
        logging.basicConfig(level=logging.INFO if debug_mode else logging.WARNING,
                           format='%(asctime)s - %(levelname)s - %(message)s')
        self.logger = logging.getLogger(__name__)
    
    def extract_text_features(self, text: str, language: str) -> Dict[str, Any]:
        features = {}
        text_clean = preprocess_text(text, language, remove_stopwords=True)
        
        # Extract key phrases
        try:
            response = self.client.extract_key_phrases(
                documents=[{"id": "1", "language": language, "text": text}]
            )[0]
            
            if not response.is_error:
                key_phrases = [preprocess_text(phrase, language) for phrase in response.key_phrases]
                key_phrases = [phrase for phrase in key_phrases if phrase]
                features["key_phrases"] = set(key_phrases)
            else:
                features["key_phrases"] = set()
                self.logger.warning(f"Error extracting key phrases: {response.error}")
                
        except Exception as e:
            self.logger.error(f"Exception extracting key phrases: {str(e)} for request with text: {text}")
            features["key_phrases"] = set()
        
        try:
            response = self.client.recognize_entities(
                documents=[{"id": "1", "language": language, "text": text}]
            )[0]
            
            if not response.is_error:
                entities = [preprocess_text(entity.text, language) for entity in response.entities]
                entities = [entity for entity in entities if entity]
                features["entities"] = set(entities)
            else:
                features["entities"] = set()
                self.logger.warning(f"Error extracting entities: {response.error}")
                
        except Exception as e:
            self.logger.error(f"Exception extracting entities: {str(e)} for request with text: {text}")
            features["entities"] = set()
        
        # Add processed text for word-level comparison
        features["processed_text"] = set(text_clean.split())
        
        return features
    
    def find_similar_batch(self,request: SimilarityRequest,threshold: float = 0.2) -> SimilarityResponse:
        try:
            processed_entries: List[ProcessedEntry] = []
            features_map = {}

            for entry in request.entries:
                language = entry.language or detect_language(entry.text)
                entry_features = self.extract_text_features(entry.text, language)

                processed_entry = ProcessedEntry(
                    id=entry.id,
                    language=language,
                    text=entry.text,
                    similarities=[]
                )

                processed_entries.append(processed_entry)
                features_map[entry.id] = entry_features

            for i, entry in enumerate(processed_entries):
                input_features = features_map[entry.id]

                for j in range(i + 1, len(processed_entries)):
                    other_entry = processed_entries[j]
                    comparison_features = features_map[other_entry.id]
                    similarity_score = self._calculate_feature_similarity(input_features, comparison_features)

                    if similarity_score >= threshold:
                        similarity_result = EntrySimilarityResult(
                            id=other_entry.id,
                            similarityScore=similarity_score,
                            text=other_entry.text,
                            language=other_entry.language 
                        )
                        entry.similarities.append(similarity_result)
                        
                        reverse_similarity_result = EntrySimilarityResult(
                            id=entry.id,
                            similarityScore=similarity_score,
                            text=entry.text,
                            language=entry.language 
                        )
                        other_entry.similarities.append(reverse_similarity_result)

                entry.similarities.sort(key=lambda x: x.similarityScore, reverse=True)

            response = SimilarityResponse(
                id=request.id,
                timestamp=datetime.now(timezone.utc).isoformat().replace("+00:00", "Z"),
                modelVersion=similarity_version,
                modelId="azure_similarity_analyzer",
                entries=processed_entries
            )

            return response

        except Exception as e:
            self.logger.error(f"Error in similarity analysis: {str(e)} for request with id: {request.id}")
            self.logger.error(traceback.format_exc())
            return SimilarityResponse(
                id=request.id,
                timestamp=datetime.now(timezone.utc).isoformat().replace("+00:00", "Z"),
                modelVersion=similarity_version,
                modelId="azure_similarity_analyzer",
                entries=[]
            )
    
    def find_similar(self, text: str, feedback_database: List[str], 
                    threshold: float = 0.35, language: str = None) -> List[Dict[str, Any]]:
        if language is None:
            language = detect_language(text)
        
        # Extract features from input text
        input_features = self.extract_text_features(text, language)
        
        similar_items = []
        
        for feedback_text in feedback_database:
            try:
                # Extract features from comparison text
                comparison_features = self.extract_text_features(feedback_text, language)
                
                # Calculate similarity based on multiple features
                similarity_score = self._calculate_feature_similarity(input_features, comparison_features)
                
                if similarity_score >= threshold:
                    similar_items.append({
                        "text": feedback_text,
                        "similarity": similarity_score,
                        "features_match": {
                            "key_phrases": len(input_features["key_phrases"] & comparison_features["key_phrases"]),
                            "entities": len(input_features["entities"] & comparison_features["entities"]),
                            "words": len(input_features["processed_text"] & comparison_features["processed_text"])
                        }
                    })
                    
            except Exception as e:
                self.logger.error(f"Error comparing with '{feedback_text[:50]}...': {str(e)}")
                continue
        
        # Sort by similarity score (descending)
        similar_items.sort(key=lambda x: x["similarity"], reverse=True)
        
        return similar_items
    
    def _calculate_feature_similarity(self, features1: Dict[str, Set], features2: Dict[str, Set]) -> float:
        weights = {
            "key_phrases": 0.5,
            "entities": 0.3,
            "processed_text": 0.2
        }
        
        total_similarity = 0.0
        
        for feature_type, weight in weights.items():
            set1 = features1.get(feature_type, set())
            set2 = features2.get(feature_type, set())
            
            if set1 or set2:
                intersection = len(set1 & set2) 
                union = len(set1 | set2)
                feature_similarity = intersection / union if union > 0 else 0.0
                total_similarity += feature_similarity * weight                 # Jaccard similarity
        
        return total_similarity