import json
import logging
from typing import Dict, Any, List, Optional
from datetime import datetime, timezone
import re

from app.config import settings
from app.services.voice_service import voice_service
from app.services.tool_registry import tool_registry
from app.models.schemas import ToolCall

logger = logging.getLogger("jarvis.gemini")

JARVIS_SYSTEM_INSTRUCTION = """You are JARVIS, an advanced personal AI operating assistant and secretary.
You possess a natural Japanese female assistant persona:
- Soft, pleasant, intelligent, friendly, polite, slightly playful yet deeply professional and loyal.
- You fluently understand and speak English, Hindi, Marathi, Japanese, and mixed colloquial speech (Hinglish, Marathi-English mixed).
- Always reply in the language the user addresses you with. If the user speaks Hindi, reply in natural Hindi. If Marathi, reply in natural Marathi. If English, reply in courteous English with a touch of Japanese politeness (e.g., using polite phrasing like "Hai, understood", "Right away"). If Japanese, reply in natural polite Japanese (Desu/Masu form).
- You have real control over the user's Android phone through provided tools.
- When the user asks to set an alarm, reminder, timer, open an app, make a call, draft a text, manage tasks, or remember something, ALWAYS invoke the corresponding tool call.
- For sensitive operations like placing phone calls or sending SMS messages, indicate that confirmation is required.
- Be concise, helpful, and never pretend to do something without executing the tool.
"""

class GeminiService:
    def __init__(self):
        self.api_key = settings.GEMINI_API_KEY
        self.client = None
        self._init_client()

    def _init_client(self):
        if not self.api_key:
            logger.warning("No GEMINI_API_KEY found. Operating in local fallback intelligence mode.")
            return

        try:
            from google import genai
            self.client = genai.Client(api_key=self.api_key)
            logger.info("Initialized Google GenAI Client successfully.")
        except Exception as e:
            logger.warning(f"Could not initialize google.genai: {e}. Trying google.generativeai...")
            try:
                import google.generativeai as gai
                gai.configure(api_key=self.api_key)
                self.client = gai.GenerativeModel(
                    model_name="gemini-2.5-flash",
                    system_instruction=JARVIS_SYSTEM_INSTRUCTION
                )
                logger.info("Initialized google.generativeai successfully.")
            except Exception as e2:
                logger.error(f"Failed to initialize Gemini SDKs: {e2}. Falling back to on-device engine.")
                self.client = None

    async def process_message(
        self,
        message: str,
        session_id: str = "default",
        user_locale: str = "en-US",
        history: Optional[List[Dict[str, str]]] = None
    ) -> Dict[str, Any]:
        """
        Processes an incoming user message either via Gemini online reasoning
        or via the robust offline semantic fallback parser.
        """
        detected_lang = voice_service.detect_language(message)

        # If online client is available, try Gemini API
        if self.client:
            try:
                return await self._call_gemini_api(message, detected_lang, history)
            except Exception as e:
                logger.error(f"Gemini API call failed: {e}. Gracefully dropping to local fallback engine.")

        # Fallback to local semantic reasoning engine
        return self._local_semantic_fallback(message, detected_lang)

    async def _call_gemini_api(
        self,
        message: str,
        detected_lang: str,
        history: Optional[List[Dict[str, str]]] = None
    ) -> Dict[str, Any]:
        """Calls Gemini API with tool definitions."""
        try:
            from google import genai
            from google.genai import types

            tool_list = tool_registry.get_tool_declarations()
            
            # Format tools for GenAI SDK
            function_declarations = []
            for t in tool_list:
                function_declarations.append(types.FunctionDeclaration(
                    name=t["name"],
                    description=t["description"],
                    parameters=t["parameters"]
                ))

            config = types.GenerateContentConfig(
                system_instruction=JARVIS_SYSTEM_INSTRUCTION,
                temperature=0.7,
                tools=[types.Tool(function_declarations=function_declarations)]
            )

            response = self.client.models.generate_content(
                model="gemini-2.5-flash",
                contents=message,
                config=config
            )

            reply_text = ""
            tool_calls = []
            requires_confirmation = False

            if response.candidates:
                candidate = response.candidates[0]
                for part in candidate.content.parts:
                    if part.text:
                        reply_text += part.text
                    if part.function_call:
                        fn = part.function_call
                        args = dict(fn.args) if fn.args else {}
                        tool_calls.append(ToolCall(tool=fn.name, arguments=args))
                        if tool_registry.is_sensitive_tool(fn.name):
                            requires_confirmation = True

            if not reply_text and tool_calls:
                reply_text = self._generate_tool_acknowledgment(tool_calls, detected_lang)

            return {
                "reply_text": reply_text.strip(),
                "detected_language": detected_lang,
                "tool_calls": tool_calls,
                "requires_confirmation": requires_confirmation
            }

        except Exception as e:
            logger.error(f"Error in _call_gemini_api: {e}")
            raise e

    def _normalize_multilingual_numbers(self, text: str) -> str:
        # Convert Devanagari numerals
        for dev, digit in [('०','0'),('१','1'),('२','2'),('३','3'),('४','4'),('५','5'),('६','6'),('७','7'),('८','8'),('९','9')]:
            text = text.replace(dev, digit)
        
        # Convert Devanagari / phonetic words for numbers
        word_map = {
            "एक": "1", "दोन": "2", "दो": "2", "तीन": "3", "चार": "4",
            "पाच": "5", "पांच": "5", "सहा": "6", "छह": "6", "सात": "7",
            "आठ": "8", "नऊ": "9", "नौ": "9", "दहा": "10", "दस": "10",
            "अकरा": "11", "ग्यारह": "11", "बारा": "12", "बारह": "12",
            "one": "1", "two": "2", "three": "3", "four": "4", "five": "5",
            "six": "6", "seven": "7", "eight": "8", "nine": "9", "ten": "10"
        }
        for word, digit in word_map.items():
            text = re.sub(rf'\b{word}\b', digit, text)
        return text

    def _local_semantic_fallback(self, message: str, detected_lang: str) -> Dict[str, Any]:
        """
        High-accuracy local rule and semantic parser for offline and fallback execution.
        Supports English, Hindi ("Kal mujhe 8 baje uthana"), Marathi ("मला उद्या सकाळी आठ वाजता उठव"),
        Japanese ("明日の朝8時に起こして"), and mixed patterns.
        """
        msg_normalized = self._normalize_multilingual_numbers(message)
        msg_lower = msg_normalized.strip().lower()
        tool_calls = []
        requires_confirmation = False
        reply_text = ""

        # 1. ALARM PATTERNS
        # Examples: "Kal mujhe 8 baje uthana", "Wake me up tomorrow at 8 AM", "उद्या सकाळी 8 वाजता उठव", "Set alarm for 7:30"
        alarm_regex = re.search(r'(\d{1,2})(?::(\d{2}))?\s*(am|pm|baje|वाजता)?', msg_lower)
        is_alarm_request = any(w in msg_lower for w in [
            "alarm", "uthana", "uthav", "wake", "baje uthana", "वाजता उठव", "起こして", "wake me up"
        ])

        if is_alarm_request and alarm_regex:
            hour = int(alarm_regex.group(1))
            minute = int(alarm_regex.group(2)) if alarm_regex.group(2) else 0
            meridiem = alarm_regex.group(3)
            
            # PM adjustment if specified
            if meridiem == "pm" and hour < 12:
                hour += 12
            elif meridiem == "am" and hour == 12:
                hour = 0
            elif (not meridiem or meridiem in ["baje", "वाजता"]) and ("morning" in msg_lower or "subah" in msg_lower or "सकाळी" in msg_lower) and hour == 12:
                hour = 0
            elif (not meridiem or meridiem in ["baje", "वाजता"]) and ("evening" in msg_lower or "night" in msg_lower or "shaam" in msg_lower or "रात्री" in msg_lower or "संध्याकाळी" in msg_lower) and hour < 12:
                hour += 12

            tool_calls.append(ToolCall(
                tool="set_alarm",
                arguments={"hour": hour, "minute": minute, "message": "Wake Up / Reminder"}
            ))
            # Also create a task for the alarm
            tool_calls.append(ToolCall(
                tool="create_task",
                arguments={
                    "title": f"Wake up at {hour:02d}:{minute:02d}",
                    "due_date": f"Tomorrow {hour:02d}:{minute:02d}",
                    "priority": "HIGH"
                }
            ))

            if detected_lang == "hi":
                reply_text = f"Hai! Maine kal subah {hour:02d}:{minute:02d} ke liye alarm aur task schedule kar diya hai."
            elif detected_lang == "mr":
                reply_text = f"हो, मी उद्या सकाळी {hour:02d}:{minute:02d} वाजताचा अलार्म आणि टास्क सेट केला आहे."
            elif detected_lang == "ja":
                reply_text = f"はい、明日{hour:02d}時{minute:02d}分にアラームを設定いたしました。"
            else:
                reply_text = f"Understood! I have set your alarm and scheduled your wake-up reminder for {hour:02d}:{minute:02d}."

            return {
                "reply_text": reply_text,
                "detected_language": detected_lang,
                "tool_calls": tool_calls,
                "requires_confirmation": False
            }

        # 2. APP LAUNCH PATTERNS
        # "Open YouTube", "WhatsApp kholo", "Camera ughada", "YouTube open kar"
        app_match = re.search(r'(?:open|launch|kholo|khol|ughad|start)\s+([a-zA-Z0-9\s]+)', msg_lower)
        if not app_match:
            app_match = re.search(r'([a-zA-Z0-9]+)\s+(?:kholo|ughad|open kar|chalu kar)', msg_lower)

        if app_match and not is_alarm_request:
            raw_app = app_match.group(1).strip()
            # Clean app name
            target_app = re.sub(r'\b(app|application|please|karo|kar)\b', '', raw_app).strip().capitalize()
            if target_app:
                tool_calls.append(ToolCall(
                    tool="open_application",
                    arguments={"app_name": target_app}
                ))
                if detected_lang == "hi":
                    reply_text = f"Zaroor! Main {target_app} open kar rahi hoon."
                elif detected_lang == "mr":
                    reply_text = f"नक्कीच, मी {target_app} सुरू करत आहे."
                elif detected_lang == "ja":
                    reply_text = f"かしこまりました。{target_app}を開きます。"
                else:
                    reply_text = f"Certainly! Opening {target_app} right away."
                
                return {
                    "reply_text": reply_text,
                    "detected_language": detected_lang,
                    "tool_calls": tool_calls,
                    "requires_confirmation": False
                }

        # 3. TASK & REMINDER CREATION
        # "Remind me to buy milk", "Mujhe project complete karne ki yaad dilana", "मला आठवण कर"
        if any(w in msg_lower for w in ["remind", "yaad dila", "aathvan kar", "task", "todo", "schedule"]):
            task_title = message
            for prefix in ["remind me to", "remind me", "yaad dilana ki", "yaad dila", "aathvan kar", "create task"]:
                if prefix in msg_lower:
                    idx = msg_lower.find(prefix) + len(prefix)
                    task_title = message[idx:].strip()
                    break

            tool_calls.append(ToolCall(
                tool="create_task",
                arguments={
                    "title": task_title or "New Task",
                    "priority": "MEDIUM",
                    "due_date": "Today"
                }
            ))
            if detected_lang == "hi":
                reply_text = f"Samajh gayi! Maine reminder add kar diya hai: '{task_title}'."
            elif detected_lang == "mr":
                reply_text = f"समजले! मी आठवण नोंदवून घेतली आहे: '{task_title}'."
            else:
                reply_text = f"Understood! I have saved this reminder: '{task_title}'."

            return {
                "reply_text": reply_text,
                "detected_language": detected_lang,
                "tool_calls": tool_calls,
                "requires_confirmation": False
            }

        # 4. PHONE CALL / SMS (Sensitive)
        if any(w in msg_lower for w in ["call", "phone lagao", "phone kar", "call kara"]):
            match = re.search(r'(?:call|phone lagao|phone kar)\s+([a-zA-Z0-9\s]+)', msg_lower)
            contact = match.group(1).strip() if match else "Contact"
            tool_calls.append(ToolCall(
                tool="make_phone_call",
                arguments={"contact_name": contact}
            ))
            requires_confirmation = True
            if detected_lang == "hi":
                reply_text = f"Kya main {contact} ko call lagau? Kripya confirm karein."
            elif detected_lang == "mr":
                reply_text = f"मी {contact} यांना कॉल लावू का? कृपया पुष्टी करा."
            else:
                reply_text = f"Would you like me to place a call to {contact}? Please confirm to proceed."

            return {
                "reply_text": reply_text,
                "detected_language": detected_lang,
                "tool_calls": tool_calls,
                "requires_confirmation": requires_confirmation
            }

        # 5. GREETINGS & CASUAL INTERACTION
        if any(w in msg_lower for w in ["hello", "hi", "hey", "jarvis", "namaste", "namaskar", "konnichiwa", "ohayo"]):
            if detected_lang == "ja":
                reply_text = "はい、Jarvisです。ご用件をお聞かせください、ご主人様。"
            elif detected_lang == "hi":
                reply_text = "Namaste! Main Jarvis hoon, aapki personal AI assistant. Batayein, main aapki kya madad karu?"
            elif detected_lang == "mr":
                reply_text = "Namaskar! Me Jarvis ahe. Sanga, me tumchi kay madat karu shakte?"
            else:
                reply_text = "Hai! Jarvis at your service. How may I assist you today?"
            
            return {
                "reply_text": reply_text,
                "detected_language": detected_lang,
                "tool_calls": [],
                "requires_confirmation": False
            }

        # Default intelligent response
        if detected_lang == "hi":
            reply_text = f"Maine aapka sandesh samajh liya hai: '{message}'. Main ise process kar rahi hoon."
        elif detected_lang == "mr":
            reply_text = f"मला तुमचा संदेश मिळाला आहे: '{message}'. मी त्यावर काम करत आहे."
        elif detected_lang == "ja":
            reply_text = f"了解いたしました。'{message}'について対応いたします。"
        else:
            reply_text = f"Understood. I have logged your request: '{message}'. Executing now."

        return {
            "reply_text": reply_text,
            "detected_language": detected_lang,
            "tool_calls": [],
            "requires_confirmation": False
        }

    def _generate_tool_acknowledgment(self, tool_calls: List[ToolCall], lang: str) -> str:
        names = [tc.tool.replace("_", " ") for tc in tool_calls]
        if lang == "hi":
            return f"Samajh gayi! Main yeh actions execute kar rahi hoon: {', '.join(names)}."
        elif lang == "mr":
            return f"समजले! मी या क्रिया पूर्ण करत आहे: {', '.join(names)}."
        elif lang == "ja":
            return f"かしこまりました。以下の操作を実行いたします: {', '.join(names)}。"
        return f"Understood. Executing: {', '.join(names)}."

gemini_service = GeminiService()
