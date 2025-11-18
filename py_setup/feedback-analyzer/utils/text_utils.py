import re
import logging
from langdetect import detect
import traceback
from config import SUPPORTED_LANGUAGES

logger = logging.getLogger(__name__)

def detect_language(text):
    # Combines rule-based and statistical approaches; handles Finnish, Swedish, and English with special attention to Nordic languages
    
    try:
        text = fix_encoding(text)
        text_lower = text.lower()
        
        # Swedish-specific words that strongly indicate Swedish
        swedish_indicators = ["är", "och", "att", "det", "som", "på", "för", "inte", "har", "till"]
        # Finnish-specific words that strongly indicate Finnish  
        finnish_indicators = ["on", "ja", "että", "ei", "hän", "se", "mutta", "kun", "niin", "kuin"]
        
        # Count indicators
        swedish_count = sum(1 for word in swedish_indicators if f" {word} " in f" {text_lower} ")
        finnish_count = sum(1 for word in finnish_indicators if f" {text_lower} " in f" {text_lower} ")
        
        # Differentiate between Finnish and Swedish
        if 'ä' in text_lower or 'ö' in text_lower or 'å' in text_lower:
            # If more Swedish indicators, return Swedish
            if swedish_count > finnish_count:
                return 'sv'
            # If more Finnish indicators, return Finnish
            elif finnish_count > swedish_count:
                return 'fi'
            # Try langdetect if indicator counts are tied
            try:
                detected = detect(text)
                if detected in ['fi', 'sv']:
                    return detected
            except:
                pass
            # Default to Finnish if contains Nordic characters but detection failed
            return 'fi'
        
        # For non-Nordic text, use langdetect
        try:
            detected = detect(text)
            if detected in SUPPORTED_LANGUAGES:
                return detected
        except:
            pass
            
        # Default to English for undetected text
        return 'en'
        
    except Exception as e:
        logger.error(f"Language detection error: {str(e)} for text: {text}")
        return 'en'

def fix_encoding(text):
    if not text:
        return text
        
    # Replace common encoding issues
    replacements = {
        'Ã¤': 'ä', 'Ã¶': 'ö', 'Ã¥': 'å',
        'Ã„': 'Ä', 'Ã–': 'Ö', 'Ã…': 'Å',
        'Ã©': 'é', 'Ã¡': 'á', 'Ã³': 'ó'
    }
    
    for old, new in replacements.items():
        text = text.replace(old, new)
    
    return text

def normalize_text(text):
    if not text:
        return text

    text = fix_encoding(text)
    text = re.sub(r'\s+', ' ', text.strip())
    
    return text

def preprocess_text(text, language='en', remove_stopwords=True):
    import nltk
    from nltk.corpus import stopwords
    from nltk.stem.snowball import SnowballStemmer
    
    # Download required NLTK data
    try:
        nltk.data.find('corpora/stopwords')
    except LookupError:
        nltk.download('stopwords', quiet=True)
    
    if not text:
        return ""
    
    text = normalize_text(text)
    text = text.lower()
    text = re.sub(r'[^\w\säöåÄÖÅ]', ' ', text)
    text = re.sub(r'\s+', ' ', text.strip())
    if remove_stopwords:
        try:
            language_map = {'fi': 'finnish', 'sv': 'swedish', 'en': 'english'}
            stop_words = set(stopwords.words(language_map.get(language, 'english')))
            words = text.split()
            text = ' '.join([word for word in words if word not in stop_words])
        except:
            pass  # Continue without stopword removal if NLTK resources unavailable
    
    return text
