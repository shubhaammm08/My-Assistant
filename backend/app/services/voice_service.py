import re
from typing import Tuple, Dict, Any
from app.models.schemas import VoiceSettings, VoiceProfileEnum

class VoiceService:
    """
    Multilingual voice configuration and personality management.
    Handles Japanese female voice styling and language matching across
    English, Hindi, Marathi, Japanese, and mixed colloquial forms.
    """

    # Devanagari Unicode Range: \u0900-\u097F
    # Japanese Hiragana/Katakana/Kanji: \u3040-\u309F, \u30A0-\u30FF, \u4E00-\u9FAF
    DEV_REGEX = re.compile(r'[\u0900-\u097F]')
    JA_REGEX = re.compile(r'[\u3040-\u309F\u30A0-\u30FF\u4E00-\u9FAF]')

    HINGLISH_KEYWORDS = {
        "kal", "aaj", "parso", "mujhe", "mera", "meri", "hum", "uthana", "baje",
        "karo", "karna", "kya", "hai", "nahi", "batao", "kaise", "samay", "yaad",
        "dila", "dilana", "karein", "shukriya", "dhanyawad", "namaste", "bhai"
    }

    MARATHI_KEYWORDS = {
        "udya", "aaj", "parva", "mala", "majha", "majhi", "amhi", "uthav", "vasta",
        "kar", "karaycha", "kay", "ahe", "nahi", "sanga", "kasa", "vel", "aathvan",
        "karun", "dya", "dhanyavad", "namaskar", "shubh", "prabhat"
    }

    JAPANESE_ROMANIZED = {
        "ohayo", "konnichiwa", "arigato", "hai", "onegai", "shimasu", "wakarimashita",
        "tasukete", "sayonara", "daijoubu", "anata", "watashi"
    }

    def detect_language(self, text: str) -> str:
        """
        Detects the dominant language or mixed variant:
        'ja', 'hi', 'mr', 'hinglish', 'marathi_mixed', 'en'
        """
        clean_text = text.strip().lower()
        if not clean_text:
            return "en"

        # Check for Japanese script
        if self.JA_REGEX.search(clean_text):
            return "ja"

        # Check for Devanagari script (Hindi or Marathi)
        if self.DEV_REGEX.search(clean_text):
            # Check for Marathi-specific grammatical markers (e.g. आहे, करा, मला, उद्या)
            marathi_dev = ["आहे", "उद्या", "मला", "करा", "वाजता", "आठवण", "उठव", "सांगा"]
            if any(word in clean_text for word in marathi_dev):
                return "mr"
            return "hi"

        # Latin script tokenization for mixed speech analysis
        tokens = set(re.findall(r'\b[a-zA-Z]+\b', clean_text))
        
        # Check Japanese Romanized
        if tokens.intersection(self.JAPANESE_ROMANIZED):
            return "ja"

        # Check Marathi transliteration
        if tokens.intersection(self.MARATHI_KEYWORDS):
            return "mr"

        # Check Hinglish
        if tokens.intersection(self.HINGLISH_KEYWORDS):
            return "hi"

        return "en"

    def get_voice_settings(
        self,
        detected_lang: str,
        profile: VoiceProfileEnum = VoiceProfileEnum.JAPANESE_FEMALE
    ) -> VoiceSettings:
        """
        Returns tuned TTS parameters for the Japanese female assistant persona
        adapted to the detected language.
        """
        if profile == VoiceProfileEnum.JAPANESE_FEMALE:
            if detected_lang == "ja":
                return VoiceSettings(
                    pitch=1.22,
                    speed=1.0,
                    volume=1.0,
                    locale="ja-JP",
                    voice_name="ja-jp-x-female-soft",
                    accent_influence="japanese_native"
                )
            elif detected_lang == "hi":
                return VoiceSettings(
                    pitch=1.20,
                    speed=1.0,
                    volume=1.0,
                    locale="hi-IN",
                    voice_name="hi-in-x-female-soft",
                    accent_influence="japanese_polite_inflection"
                )
            elif detected_lang == "mr":
                return VoiceSettings(
                    pitch=1.20,
                    speed=1.0,
                    volume=1.0,
                    locale="mr-IN",
                    voice_name="mr-in-x-female-soft",
                    accent_influence="japanese_polite_inflection"
                )
            else:
                # English with soft Japanese female inflection
                return VoiceSettings(
                    pitch=1.24,
                    speed=0.98,
                    volume=1.0,
                    locale="en-US",
                    voice_name="en-us-x-japanese-accent-female",
                    accent_influence="japanese_accented_english"
                )

        # Fallback default
        return VoiceSettings(
            pitch=1.0,
            speed=1.0,
            volume=1.0,
            locale="en-US",
            voice_name="default",
            accent_influence="standard"
        )

voice_service = VoiceService()

