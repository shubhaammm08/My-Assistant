from typing import Dict, Any, List

class ToolRegistry:
    """
    Central registry for JARVIS phone control tools, task actions, and assistant abilities.
    Exposes tool schemas for Gemini function calling and provides client dispatch metadata.
    """

    TOOLS: List[Dict[str, Any]] = [
        {
            "name": "set_alarm",
            "description": "Set an alarm on the user's Android phone for a specific time.",
            "parameters": {
                "type": "OBJECT",
                "properties": {
                    "hour": {
                        "type": "INTEGER",
                        "description": "Hour in 24-hour format (0-23)."
                    },
                    "minute": {
                        "type": "INTEGER",
                        "description": "Minute (0-59)."
                    },
                    "message": {
                        "type": "STRING",
                        "description": "Label or purpose of the alarm."
                    }
                },
                "required": ["hour", "minute"]
            }
        },
        {
            "name": "set_timer",
            "description": "Set a countdown timer on the Android phone.",
            "parameters": {
                "type": "OBJECT",
                "properties": {
                    "seconds": {
                        "type": "INTEGER",
                        "description": "Duration in seconds."
                    },
                    "label": {
                        "type": "STRING",
                        "description": "Optional label for the timer."
                    }
                },
                "required": ["seconds"]
            }
        },
        {
            "name": "open_application",
            "description": "Open or launch an application on the user's Android device (e.g. YouTube, WhatsApp, Settings, Camera, Spotify, Chrome, Maps).",
            "parameters": {
                "type": "OBJECT",
                "properties": {
                    "app_name": {
                        "type": "STRING",
                        "description": "Common name of the app to launch (e.g. 'YouTube', 'WhatsApp', 'Camera')."
                    },
                    "package_name": {
                        "type": "STRING",
                        "description": "Optional Android package name if known (e.g. 'com.google.android.youtube')."
                    }
                },
                "required": ["app_name"]
            }
        },
        {
            "name": "make_phone_call",
            "description": "Initiate a phone call to a contact or phone number. Requires user confirmation before placing the call.",
            "parameters": {
                "type": "OBJECT",
                "properties": {
                    "contact_name": {
                        "type": "STRING",
                        "description": "Name of the contact."
                    },
                    "phone_number": {
                        "type": "STRING",
                        "description": "Direct phone number if known."
                    }
                },
                "required": ["contact_name"]
            }
        },
        {
            "name": "draft_sms",
            "description": "Draft an SMS or text message to a contact. User must confirm before sending.",
            "parameters": {
                "type": "OBJECT",
                "properties": {
                    "contact_name": {
                        "type": "STRING",
                        "description": "Name of recipient."
                    },
                    "phone_number": {
                        "type": "STRING",
                        "description": "Recipient phone number if available."
                    },
                    "message": {
                        "type": "STRING",
                        "description": "Body of the message."
                    }
                },
                "required": ["contact_name", "message"]
            }
        },
        {
            "name": "create_task",
            "description": "Create a task or reminder in the personal task manager.",
            "parameters": {
                "type": "OBJECT",
                "properties": {
                    "title": {
                        "type": "STRING",
                        "description": "Task title or reminder message."
                    },
                    "due_date": {
                        "type": "STRING",
                        "description": "Due date and time in ISO 8601 string or readable format (e.g. 'tomorrow at 9 AM')."
                    },
                    "priority": {
                        "type": "STRING",
                        "enum": ["LOW", "MEDIUM", "HIGH", "URGENT"],
                        "description": "Priority level."
                    },
                    "recurrence": {
                        "type": "STRING",
                        "description": "Recurrence rule if applicable (e.g. 'daily', 'weekly')."
                    }
                },
                "required": ["title"]
            }
        },
        {
            "name": "list_tasks",
            "description": "Retrieve tasks, reminders, and daily agenda.",
            "parameters": {
                "type": "OBJECT",
                "properties": {
                    "filter_status": {
                        "type": "STRING",
                        "enum": ["ALL", "PENDING", "COMPLETED"],
                        "description": "Filter by task status."
                    }
                }
            }
        },
        {
            "name": "adjust_volume",
            "description": "Adjust Android volume levels (ringtone, media, or alarm).",
            "parameters": {
                "type": "OBJECT",
                "properties": {
                    "stream_type": {
                        "type": "STRING",
                        "enum": ["media", "ring", "alarm", "notification"],
                        "description": "Audio stream to adjust."
                    },
                    "action": {
                        "type": "STRING",
                        "enum": ["up", "down", "mute", "unmute", "set"],
                        "description": "Action to take."
                    },
                    "level_percent": {
                        "type": "INTEGER",
                        "description": "Volume percentage (0-100) if action is 'set'."
                    }
                },
                "required": ["action"]
            }
        },
        {
            "name": "save_memory",
            "description": "Remember a user preference, personal detail, contact relationship, or important note.",
            "parameters": {
                "type": "OBJECT",
                "properties": {
                    "key": {
                        "type": "STRING",
                        "description": "Short memory key (e.g. 'favorite_coffee', 'wifes_birthday')."
                    },
                    "value": {
                        "type": "STRING",
                        "description": "Fact or preference to remember."
                    },
                    "category": {
                        "type": "STRING",
                        "description": "Category (e.g. 'personal', 'work', 'preference')."
                    }
                },
                "required": ["key", "value"]
            }
        },
        {
            "name": "search_web",
            "description": "Search the internet for real-time information, research, news, or answers.",
            "parameters": {
                "type": "OBJECT",
                "properties": {
                    "query": {
                        "type": "STRING",
                        "description": "The search query."
                    }
                },
                "required": ["query"]
            }
        }
    ]

    @classmethod
    def get_tool_declarations(cls) -> List[Dict[str, Any]]:
        return cls.TOOLS

    @classmethod
    def is_sensitive_tool(cls, tool_name: str) -> bool:
        """Determines if a tool requires user approval before execution."""
        return tool_name in {"make_phone_call", "draft_sms"}

tool_registry = ToolRegistry()

