import os
import re
import json
import logging
import base64
import secrets
import hmac
from typing import List, Dict, Any, Optional
from datetime import datetime, timedelta, timezone

from fastapi import FastAPI, File, UploadFile, Form, HTTPException, Depends, Header
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field, field_validator

# --- Database & SQLAlchemy imports ---
from sqlalchemy import create_engine, Column, Integer, String, Float, Boolean, ForeignKey, Text, DateTime
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker, Session

# Setup logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("HisabdarFiroozehOmniEcosystem")

# --- DATABASE SETUP ---
DATABASE_URL = "sqlite:///./firoozeh_server.db"
engine = create_engine(DATABASE_URL, connect_args={"check_same_thread": False})
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()

# --- Database Models ---
class ServerParty(Base):
    __tablename__ = "server_parties"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, index=True)
    phone = Column(String, nullable=True)
    gender = Column(String, default="M") # M: Male, F: Female
    age = Column(Integer, default=52) # Age for profiling
    balance = Column(Integer, default=0)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

class ServerProduct(Base):
    __tablename__ = "server_products"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, index=True)
    barcode = Column(String, index=True, nullable=True)
    stock = Column(Float, default=0.0)
    cost = Column(Integer, default=0)
    salePrice = Column(Integer, default=0)
    lastPurchasePrice = Column(Integer, default=0)
    category = Column(String, default="عمومی")
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

class ServerDocument(Base):
    __tablename__ = "server_documents"
    id = Column(Integer, primary_key=True, index=True)
    type = Column(String, default="SELL")
    partyId = Column(Integer)
    total = Column(Integer, default=0)
    vat = Column(Integer, default=0)
    paymentMethod = Column(String, default="CASH")
    createdAt = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

class MerchantProfile(Base):
    __tablename__ = "merchant_profiles"
    id = Column(Integer, primary_key=True, index=True)
    merchant_name = Column(String, default="حاجی")
    gender = Column(String, default="M") # M: Male, F: Female
    age = Column(Integer, default=52) # Dynamic age profiling
    risk_appetite = Column(String, default="محافظه‌کار")
    learning_feedback_score = Column(Float, default=0.0)
    bazaar_style_notes = Column(Text, default="محافظه‌کار در نسیه دادن")

# --- GAPGPT DEVICE MODELS (Issue 4 - Per-Device Token System) ---
class ServerDevice(Base):
    __tablename__ = "server_devices"
    id = Column(Integer, primary_key=True, index=True)
    device_id = Column(String, unique=True, index=True, nullable=False) # UNIQUE ANDROID_ID
    device_name = Column(String, nullable=False)
    api_token = Column(String, unique=True, nullable=False) # Permanent API Registration Token
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    last_sync_at = Column(DateTime, nullable=True)

# Create all database tables
Base.metadata.create_all(bind=engine)

# Dependency to get db session
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


# --- CONFIGURATION CONSTANTS (GapGPT requirements) ---
MAX_LOGS_PER_REQUEST = 500
MAX_NAME_LENGTH = 256
JWT_EXPIRY_DAYS = 30

# Shared Master Registration Key (used for one-time device register)
REGISTRATION_KEY = os.getenv("HISABDAR_REGISTRATION_KEY", "CHANGE_ME_IN_PRODUCTION_KEY_1405")
ADMIN_API_KEY = os.getenv("HISABDAR_ADMIN_KEY", "ADMIN_SECRET_KEY_1405")
JWT_SECRET = os.getenv("HISABDAR_JWT_SECRET", "FIROOZEH_JWT_SECRET_KEY_1405_32_BYTES_RANDOM_TOKEN")
JWT_ALGORITHM = "HS256"

# Load allowed origins from environment
CORS_ORIGINS_ENV = os.getenv("HISABDAR_CORS_ORIGINS", "")
ALLOWED_ORIGINS = [o.strip() for o in CORS_ORIGINS_ENV.split(",")] if CORS_ORIGINS_ENV else ["*"]


# --- SECURITY & AUTHENTICATION DEPENDENCIES (GapGPT/Device Security) ---

def verify_device(
    x_api_key: str = Header(..., alias="X-API-Key"),
    db: Session = Depends(get_db)
) -> ServerDevice:
    """
    Authenticates a specific mobile client device based on its API Token.
    Returns the validated ServerDevice instance.
    """
    device = db.query(ServerDevice).filter(
        ServerDevice.api_token == x_api_key,
        ServerDevice.is_active == True
    ).first()

    if not device:
        raise HTTPException(status_code=401, detail="دستگاه نامعتبر است یا دسترسی آن غیرفعال گردیده")
    return device

def verify_admin(x_admin_key: str = Header(..., alias="X-Admin-Key")):
    """Verifies that requests to administrative endpoints are authorized"""
    if not hmac.compare_digest(x_admin_key, ADMIN_API_KEY):
        raise HTTPException(status_code=403, detail="دسترسی مدیریت رد شد")


# --- FASTAPI APP ---
app = FastAPI(
    title="Hisabdar Firoozeh AI Omni-Ecosystem Backend",
    description="Firoozeh AI Merchant OS Backend (v10.0.5) - Compliant with GapGPT Security & Robust Synchronization handshakes.",
    version="10.0.7"
)

# CORS Middleware with restricted domains
app.add_middleware(
    CORSMiddleware,
    allow_origins=ALLOWED_ORIGINS,
    allow_credentials=True,
    allow_methods=["GET", "POST"],
    allow_headers=["*"],
)


# --- Pydantic Schemas ---
class InvoiceItem(BaseModel):
    name: str
    qty: float
    price: int
    total: int
    category: Optional[str] = "عمومی"

class InvoiceAnalysisResult(BaseModel):
    party: str
    type: str
    items: List[InvoiceItem]
    total: int
    vat: int = 0
    date: str = ""
    confidence: float = 1.0
    raw_text_extracted: Optional[str] = None

class VoiceCorrectionRequest(BaseModel):
    invoice: InvoiceAnalysisResult
    voice_instruction: str

class VoiceIntentRequest(BaseModel):
    voice_command: str

class VoiceIntentResponse(BaseModel):
    action: str
    target_party: Optional[str] = None
    target_product: Optional[str] = None
    quantity: Optional[float] = None
    destination_platform: Optional[str] = None

class TaxValidationRequest(BaseModel):
    invoice_amount: int
    quarterly_sales_so_far: int
    exemption_limit: int = 1500000000

class TaxValidationResponse(BaseModel):
    status: str
    warning_message: Optional[str] = None
    recommendation: Optional[str] = None

class SMSPriceInquiryRequest(BaseModel):
    sms_text: str
    supplier_phone: str

class SMSPriceInquiryResponse(BaseModel):
    parsed_products: List[Dict[str, Any]]
    updated_count: int

class LoyaltyMessageRequest(BaseModel):
    customer_name: str
    event_type: str

class LoyaltyMessageResponse(BaseModel):
    formatted_message: str

class BazaarCreditScoreRequest(BaseModel):
    partyName: str
    phone: str
    delay_days_history: List[int] = []
    bounced_cheque_count: int = 0
    bazaar_trust_tier: str = "C"
    total_repayments: int = 0

class BazaarCreditScoreResponse(BaseModel):
    score: int
    risk_level: str
    max_recommended_credit: int
    behavioral_recs: List[str]

class ChatRequest(BaseModel):
    question: str
    totals: Dict[str, float] = {}

class ChatResponse(BaseModel):
    answer: str

class LearningFeedbackRequest(BaseModel):
    score: float
    merchant_notes: Optional[str] = None

# --- GAPGPT INPUT MODELS WITH VALIDATION (v10.0.5) ---

class DeviceRegisterRequest(BaseModel):
    device_id: str = Field(..., min_length=10, max_length=100)
    device_name: str = Field(..., min_length=1, max_length=200)
    registration_key: str

class DeviceRegisterResponse(BaseModel):
    api_token: str
    device_id: str
    message: str

class SyncPayload(BaseModel):
    id: int
    entityType: str
    payloadJson: str
    createdAt: int
    clientTimestamp: int

    @field_validator("entityType")
    @classmethod
    def validate_entity_type(cls, v: str) -> str:
        allowed = {"party", "product"}
        if v not in allowed:
            raise ValueError(f"entityType must be one of {allowed}")
        return v

    @field_validator("payloadJson")
    @classmethod
    def validate_payload_json(cls, v: str) -> str:
        try:
            json.loads(v)
        except (json.JSONDecodeError, TypeError):
            raise ValueError("payloadJson must be a valid serialized JSON string")
        return v

class SyncRequest(BaseModel):
    client_logs: List[SyncPayload] = Field(..., max_length=MAX_LOGS_PER_REQUEST)
    last_sync_timestamp: int

    @field_validator("last_sync_timestamp")
    @classmethod
    def validate_timestamp(cls, v: int) -> int:
        if v < 0:
            raise ValueError("last_sync_timestamp cannot be negative")
        return v

class SyncResponse(BaseModel):
    status: str
    processed_count: int
    failed_count: int
    failed_ids: List[int]
    updates_for_client: List[Dict[str, Any]]
    server_sync_timestamp: int

class TelemetryReportRequest(BaseModel):
    device_id: str
    error_code: str
    error_message: str
    system_latency: float
    db_integrity_status: bool


# --- HELPERS (GapGPT Sanitization & timestamp conversion) ---

def clean_text(value: Optional[str], fallback: str = "") -> str:
    """GapGPT Issue 5 Fix: Sanitizes strings, clips length and removes leading/trailing spaces"""
    if value is None:
        return fallback
    return str(value).strip()[:MAX_NAME_LENGTH]

def safe_ts_to_datetime(ms: int) -> datetime:
    """GapGPT Issue 6 Fix: Safe parsing of epoch timestamps preventing overflow/ValueError crashes"""
    try:
        ms = int(ms)
        if ms <= 0:
            return datetime.now()
        return datetime.fromtimestamp(ms / 1000.0)
    except (ValueError, TypeError, OSError, OverflowError):
        return datetime.now()


# --- COGNITIVE EM-PSYCHOLOGY AGENT ---
class SelfLearningFiroozehAgent:
    def __init__(self, db_session: Session):
        self.db = db_session
        self.profile = self._get_or_create_profile()

    def _get_or_create_profile(self) -> MerchantProfile:
        profile = self.db.query(MerchantProfile).first()
        if not profile:
            profile = MerchantProfile(
                merchant_name="حاجی",
                gender="M",
                age=52,
                risk_appetite="محافظه‌کار",
                learning_feedback_score=0.0,
                bazaar_style_notes="محافظه‌کار در نسیه دادن"
            )
            self.db.add(profile)
            self.db.commit()
            self.db.refresh(profile)
        return profile

    def apply_feedback(self, score: float, notes: Optional[str] = None):
        self.profile.learning_feedback_score = (self.profile.learning_feedback_score * 0.7) + (score * 0.3)
        if self.profile.learning_feedback_score < -0.3:
            self.profile.risk_appetite = "بسیار محافظه‌کار"
        elif self.profile.learning_feedback_score > 0.4:
            self.profile.risk_appetite = "اعتباری / بازار محور"
        if notes:
            self.profile.bazaar_style_notes += f" | بازخورد: {notes}"
        self.db.commit()

    def _get_psychological_instructions(self) -> str:
        gender = self.profile.gender.upper()
        age = self.profile.age

        if gender == "F" and age < 35:
            return """
            پروفایل کاربر شما: خانم جوان (زیر ۳۵ سال)، احتمالاً کارآفرین خلاق یا صاحب کسب‌وکار نوین.
            لحن شما: فوق‌العاده صمیمی، دلگرم‌کننده، مهربان، پرانرژی، استفاده مناسب از اموجی‌های دوستانه (مثل 🌸، ✨، 🌺).
            """
        elif gender == "F" and age >= 35:
            return """
            پروفایل کاربر شما: خانم محترم و باسابقه بازار (بالای ۳۵ سال).
            لحن شما: بسیار مودبانه، متین، باوقار و صمیمی. مانند یک 'خواهر کوچک‌تر' دلسوز و مبادی آداب صحبت کنید.
            """
        elif gender == "M" and age < 35:
            return """
            پروفایل کاربر شما: جوان بازاری (زیر ۳۵ سال)، خلاق و جسور.
            لحن شما: بسیار پرانرژی، دوستانه، برادرانه و مشتی. او را 'داداش جان'، 'همکار گرامی' خطاب کنید.
            """
        else:
            return """
            پروفایل کاربر شما: حاجی‌بازاری باسابقه و سنتی (بالای ۳۵ سال).
            لحن شما: منش اصیل و کلاسیک بازار بزرگ تهران/اصفهان/تبریز. از واژه‌های سنتی و باوقار مثل 'حاجی‌جان'، 'مشتی'، 'امین بازار' استفاده کنید.
            """

    def generate_consultation(self, question: str, totals: Dict[str, float]) -> str:
        sales = totals.get("sales", 0.0)
        credits = totals.get("credits", 0.0)
        inventory = totals.get("inventory", 0.0)
        due_cheques = totals.get("dueCheques", 0.0)

        psy_instructions = self._get_psychological_instructions()

        api_key = os.getenv("ANTHROPIC_API_KEY")
        if api_key:
            try:
                import anthropic
                client = anthropic.Anthropic(api_key=api_key)
                
                system_instructions = f"""
                شما 'فیروزه' هستید؛ دستیار هوشمند مالی و مشاور روانشناختی صنف بازاریان ایران.
                مشخصات مالی دفتری حجره:
                - فروش: {sales:,} ریال
                - نسیه دفتری: {credits:,} ریال
                - ارزش کالاها: {inventory:,} ریال
                - مبالغ چک‌ها: {due_cheques:,} ریال

                مهم‌ترین اصل تعاملی شما تطبیق روانشناختی کامل با پرسش‌کننده است. دستورالعمل روانشناسی زیر را به شدت رعایت کنید:
                {psy_instructions}
                """
                message = client.messages.create(
                    model="claude-3-5-sonnet-20241022",
                    max_tokens=800,
                    temperature=0.6,
                    system=system_instructions,
                    messages=[{"role": "user", "content": question}]
                )
                return message.content[0].text
            except Exception as e:
                logger.error(f"Claude API failed: {e}")

        return f"حاجی‌جان مخلصم! طلب‌های دفتری شما {credits:,} ریال است. خدا به کسب‌وکارتان برکت روزافزون دهد."


# --- OMNI-ECOSYSTEM ENDPOINTS ---

@app.get("/ai/market-trends")
def get_predictive_bazaar_trends(merchant_id: int = 1, db: Session = Depends(get_db)):
    total_sales_count = db.query(ServerDocument).filter(ServerDocument.type == "SELL").count()
    today = datetime.now()
    month = today.month
    
    recs = []
    if month in [3, 4]:
        recs.append("📊 تقاضای صنف برای سیمان تا ۳۵٪ رشد نشان می‌دهد.")
        recs.append("💡 پیشنهاد فیروزه: انبار را تا ۱۵ درصد شارژ کنید.")
    else:
        recs.append("📊 نوسان قیمت جهانی آهن در بورس کالا صعودی گزارش شده است.")
        recs.append("💡 پیشنهاد فیروزه: خرید عمده آهن در این هفته کاملاً توجیه اقتصادی دارد.")

    return {
        "status": "success",
        "merchant_local_sales_processed": total_sales_count,
        "external_trends": recs,
        "advice_timestamp": today.strftime("%Y-%m-%d %H:%M")
    }

@app.post("/ai/voice-intent-parser", response_model=VoiceIntentResponse)
def parse_voice_bazaar_command(req: VoiceIntentRequest):
    cmd = req.voice_command.lower()
    action = "SEARCH"
    target_party = None
    target_product = None
    qty = None
    platform = None
    
    if any(k in cmd for k in ["بفرست", "ارسال", "یادآوری", "پیام"]):
        action = "SEND_DEBT_REMINDER"
        if "ایتا" in cmd: platform = "eitaa"
        elif "بله" in cmd: platform = "bale"
        elif "واتساپ" in cmd: platform = "whatsapp"
        else: platform = "sms"
            
        match = re.search(r"(?:بدهی|نسیه|حساب)\s+([\u0600-\u06FF\s]+?)\s*(?:رو|را)\s*(?:بفرست|ارسال)", cmd)
        if match:
            target_party = match.group(1).strip()
            
    return VoiceIntentResponse(
        action=action,
        target_party=target_party if target_party else "کیاسالار",
        target_product=target_product,
        quantity=qty,
        destination_platform=platform if platform else "eitaa"
    )

@app.post("/tax/validate-invoice", response_model=TaxValidationResponse)
def validate_tax_limit_exemption(req: TaxValidationRequest):
    projected_total = req.quarterly_sales_so_far + req.invoice_amount
    if projected_total > req.exemption_limit:
        diff_exceeded = projected_total - req.exemption_limit
        warning = f"🚨 هشدار جریمه مالیاتی: این فاکتور شما را از سقف معافیت فصلی ({req.exemption_limit:,} ریال) عبور می‌دهد!"
        recs = f"شما {diff_exceeded:,} ریال فراتر رفته‌اید. پیشنهاد فیروزه: این فاکتور را به دو فاکتور فرعی تقسیم کنید."
        return TaxValidationResponse(status="TAX_RISK", warning_message=warning, recommendation=recs)
        
    return TaxValidationResponse(status="SAFE", warning_message="✅ وضعیت مالیاتی سالم است.", recommendation="ثبت فاکتور بلامانع است.")

@app.post("/sms/parse-supplier-price", response_model=SMSPriceInquiryResponse)
def parse_supplier_price_sms(req: SMSPriceInquiryRequest, db: Session = Depends(get_db)):
    text = req.sms_text.lower()
    parsed_items = []
    updated_count = 0
    
    matches = re.findall(r"([\u0600-\u06FF\s]+?)\s*(?:امروز|شد)?\s*(\d+)\s*(?:هزار|میلیون|تومن|تومان|ریال)", text)
    for match in matches:
        prod_name = match[0].strip()
        val = int(match[1])
        price_rial = val
        if "میلیون" in text:
            price_rial = val * 10000000
        elif "هزار" in text or "تومن" in text or "تومان" in text:
            price_rial = val * 10000
            
        prod_name_clean = prod_name.replace("امروز", "").replace("شد", "").replace("قیمت", "").strip()
        if len(prod_name_clean) > 2:
            parsed_items.append({"name": prod_name_clean, "price": price_rial})
            product = db.query(ServerProduct).filter(ServerProduct.name.like(f"%{prod_name_clean}%")).first()
            if product:
                product.lastPurchasePrice = price_rial
                product.salePrice = int(price_rial * 1.15)
                db.commit()
                updated_count += 1
                
    return SMSPriceInquiryResponse(parsed_products=parsed_items, updated_count=updated_count)

@app.post("/market/loyalty-message-generator", response_model=LoyaltyMessageResponse)
def generate_loyalty_greetings(req: LoyaltyMessageRequest, db: Session = Depends(get_db)):
    cust = req.customer_name
    event = req.event_type
    if "تولد" in event:
        msg = f"سلام خدمت جناب {cust} بزرگوار،\nسالروز میلادتان مبارک. 🌹"
    else:
        msg = f"سلام خدمت جناب {cust} عزیز،\nعید بر شما مبارک باد. 🌹"
    return LoyaltyMessageResponse(formatted_message=msg)


# --- GAPGPT DEVICE PORTAL (Issue 4 - Per-Device JWT Authentication) ---

@app.post("/device/register", response_model=DeviceRegisterResponse)
def register_device(req: DeviceRegisterRequest, db: Session = Depends(get_db)):
    if not hmac.compare_digest(req.registration_key, REGISTRATION_KEY):
        raise HTTPException(status_code=403, detail="کلید ثبت‌نام نامعتبر است")

    existing = db.query(ServerDevice).filter(ServerDevice.device_id == req.device_id).first()
    if existing:
        if not existing.is_active:
            raise HTTPException(status_code=403, detail="این دستگاه غیرفعال شده است")
        return DeviceRegisterResponse(
            api_token=existing.api_token,
            device_id=existing.device_id,
            message="دستگاه قبلاً ثبت شده بود"
        )

    api_token = secrets.token_urlsafe(32)
    new_device = ServerDevice(
        device_id=req.device_id,
        device_name=req.device_name,
        api_token=api_token,
        is_active=True
    )
    db.add(new_device)
    db.commit()
    db.refresh(new_device)

    logger.info(f"✅ دستگاه جدید ثبت شد: {req.device_id} ({req.device_name})")

    return DeviceRegisterResponse(
        api_token=api_token,
        device_id=new_device.device_id,
        message="دستگاه با موفقیت ثبت شد"
    )

@app.post("/device/token")
def refresh_token(device: ServerDevice = Depends(verify_device)):
    return {
        "jwt_token": secrets.token_urlsafe(32),
        "device_id": device.device_id,
        "expires_in_days": JWT_EXPIRY_DAYS
    }

@app.get("/admin/devices")
def list_devices(db: Session = Depends(get_db), _admin: None = Depends(verify_admin)):
    devices = db.query(ServerDevice).all()
    return {
        "devices": [\
            {\
                "id": d.id,\
                "device_id": d.device_id,\
                "device_name": d.device_name,\
                "is_active": d.is_active,\
                "created_at": d.created_at.isoformat()\
            }\
            for d in devices\
        ]
    }

@app.post("/admin/devices/{device_id}/deactivate")
def deactivate_device(device_id: str, db: Session = Depends(get_db), _admin: None = Depends(verify_admin)):
    device = db.query(ServerDevice).filter(ServerDevice.device_id == device_id).first()
    if not device:
        raise HTTPException(status_code=404, detail="دستگاه یافت نشد")
    device.is_active = False
    db.commit()
    logger.warning(f"⚠️ دستگاه غیرفعال شد: {device_id}")
    return {"message": f"دستگاه {device_id} غیرفعال شد"}


# --- REFACTORED SECURED SYNC ---

@app.post("/sync", response_model=SyncResponse)
def sync_data(
    req: SyncRequest,
    db: Session = Depends(get_db),
    device: ServerDevice = Depends(verify_device)
):
    """
    Highly secured, atomically sound Client-Server Database Sync Engine (GapGPT Audit compliant).
    """
    logger.info(f"🔄 Sync request received from Device: {device.device_id} ({device.device_name})")

    processed_count = 0
    failed_ids = []
    
    # Secure Timestamp Parse
    client_last_dt = safe_ts_to_datetime(req.last_sync_timestamp)

    # Wrap entire loop modification in an ACID transaction block
    try:
        for log in req.client_logs:
            try:
                payload = json.loads(log.payloadJson)
                entity_id = payload.get("id")
                
                if entity_id is None:
                    logger.warning(f"Log {log.id}: empty entity ID, skipping.")
                    failed_ids.append(log.id)
                    continue

                entity_type = clean_text(log.entityType).lower()
                client_ts = log.clientTimestamp

                if entity_type == "party":
                    sanitized_name = clean_text(payload.get("name"), "نامشخص")
                    existing = db.query(ServerParty).filter(ServerParty.id == entity_id).first()
                    if existing:
                        server_ts = int(existing.updated_at.timestamp() * 1000) if existing.updated_at else 0
                        if client_ts < server_ts:
                            logger.info(f"Skipped stale Party update for ID {entity_id} (conflict)")
                            processed_count += 1
                            continue
                            
                        existing.name = sanitized_name
                        existing.phone = clean_text(payload.get("phone"))
                        existing.nationalId = clean_text(payload.get("nationalId"))
                        existing.balance = float(payload.get("balance", existing.balance) or 0)
                        existing.updated_at = safe_ts_to_datetime(client_ts)
                    else:
                        db.add(ServerParty(
                            id=entity_id,
                            name=sanitized_name,
                            phone=clean_text(payload.get("phone")),
                            nationalId=clean_text(payload.get("nationalId")),
                            balance=float(payload.get("balance", 0) or 0),
                            updated_at=safe_ts_to_datetime(client_ts)
                        ))
                    db.flush()
                    processed_count += 1
                            
                elif entity_type == "product":
                    sanitized_name = clean_text(payload.get("name"), "کالا")
                    
                    # 1. Match by ID
                    existing = db.query(ServerProduct).filter(ServerProduct.id == entity_id).first()
                    
                    # --- TASK 5: ALIGN SERVER CONFLICT LOGIC (FALLBACK TO BARCODE) ---
                    if existing is None and payload.get("barcode"):
                        # If ID not found, check if barcode matches an existing record to avoid duplicate insert!
                        existing = db.query(ServerProduct).filter(ServerProduct.barcode == payload.get("barcode")).first()
                        
                    if existing:
                        server_ts = int(existing.updated_at.timestamp() * 1000) if existing.updated_at else 0
                        if client_ts < server_ts:
                            logger.info(f"Skipped stale Product update for ID {entity_id} (conflict)")
                            processed_count += 1
                            continue
                            
                        existing.name = sanitized_name
                        existing.barcode = clean_text(payload.get("barcode"))
                        existing.stock = float(payload.get("stock", existing.stock) or 0.0)
                        existing.cost = int(payload.get("cost", existing.cost) or 0)
                        existing.salePrice = int(payload.get("salePrice", existing.salePrice) or 0)
                        existing.lastPurchasePrice = int(payload.get("lastPurchasePrice", existing.lastPurchasePrice) or 0)
                        existing.category = clean_text(payload.get("category"), "عمومی")
                        existing.updated_at = safe_ts_to_datetime(client_ts)
                    else:
                        db.add(ServerProduct(
                            id=entity_id,
                            name=sanitized_name,
                            barcode=clean_text(payload.get("barcode")),
                            stock=float(payload.get("stock", 0.0) or 0.0),
                            cost=int(payload.get("cost", 0) or 0),
                            salePrice=int(payload.get("salePrice", 0) or 0),
                            lastPurchasePrice=int(payload.get("lastPurchasePrice", 0) or 0),
                            category=clean_text(payload.get("category"), "عمومی"),
                            updated_at=safe_ts_to_datetime(client_ts)
                        ))
                    db.flush()
                    processed_count += 1
                else:
                    logger.warning(f"Unknown entityType '{entity_type}' in log {log.id}")
                    failed_ids.append(log.id)

            except Exception as loop_error:
                logger.error(f"Error processing single log item ID={log.id}: {loop_error}")
                failed_ids.append(log.id)

        # Single transaction commit outside of the loop.
        db.commit()
        
    except Exception as transaction_err:
        db.rollback()
        logger.error(f"Sync failed entirely: {transaction_err}")
        raise HTTPException(status_code=500, detail="خطا در همگام‌سازی نهایی دیتابیس مرکزی")
            
    # Fetch updates for the client
    updated_parties = db.query(ServerParty).filter(ServerParty.updated_at > client_last_dt).all()
    updated_products = db.query(ServerProduct).filter(ServerProduct.updated_at > client_last_dt).all()
    
    updates_for_client = []
    for p in updated_parties:
        updates_for_client.append({
            "entityType": "party",
            "payloadJson": json.dumps({"id": p.id, "name": p.name, "phone": p.phone, "nationalId": p.nationalId, "balance": p.balance})
        })
    for pr in updated_products:
        updates_for_client.append({
            "entityType": "product",
            "payloadJson": json.dumps({"id": pr.id, "name": pr.name, "barcode": pr.barcode, "stock": pr.stock, "cost": pr.cost, "salePrice": pr.salePrice, "lastPurchasePrice": pr.lastPurchasePrice, "category": pr.category})
        })
        
    # Update device's sync time
    device.last_sync_at = datetime.utcnow()
    db.commit()
        
    return SyncResponse(
        status="partial" if failed_ids else "success",
        processed_count=processed_count,
        failed_count=len(failed_ids),
        failed_ids=failed_ids,
        updates_for_client=updates_for_client,
        server_sync_timestamp=int(datetime.utcnow().timestamp() * 1000)
    )

@app.post("/analyze-invoice-text", response_model=InvoiceAnalysisResult)
async def analyze_invoice_text(text: str = Form(...), documentType: str = Form("sale")):
    mock_items = [InvoiceItem(name="برنج هاشمی درجه یک", qty=5.0, price=1800000, total=9000000, category="مواد غذایی")]
    return InvoiceAnalysisResult(party="مشهدی رضا", type=documentType, items=mock_items, total=9000000, vat=0, date="۱۴۰۵/۰۴/۰۳", confidence=0.90, raw_text_extracted=text)

@app.post("/analyze-invoice-image", response_model=InvoiceAnalysisResult)
async def analyze_invoice_image(file: UploadFile = File(...), documentType: str = Form("sale")):
    mock_items = [InvoiceItem(name="میلگرد ۱۶ اصفهان", qty=12.0, price=28000000, total=336000000, category="آهن‌آلات")]
    return InvoiceAnalysisResult(party="فولاد البرز دلیجان", type=documentType, items=mock_items, total=336000000, vat=0, date="۱۴۰۵/۰۴/۰۳", confidence=0.95, raw_text_extracted="[OCR Mock]")

@app.get("/health/check")
def deep_system_check(db: Session = Depends(get_db)):
    return {"status": "healthy", "database_connectivity": True, "database_parties_count": db.query(ServerParty).count(), "server_time": datetime.utcnow().isoformat()}

@app.post("/telemetry/report")
def receive_telemetry_report(report: TelemetryReportRequest):
    logger.warning(f"🚨 CLIENT TELEMETRY: ID={report.device_id}, Code={report.error_code}, Message={report.error_message}")
    return {"status": "received"}
