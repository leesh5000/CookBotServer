# CookBot 서버 도메인 설계 문서

## 1. 도메인 개요

### 1.1 프로젝트 목표
CookBot은 사용자의 자연어 요청에 따라 AI를 이용하여 레시피를 추천하고, 해당 레시피의 재료 구매 링크를 제공하는 서비스입니다.

### 1.2 핵심 기능
- 자연어 기반 레시피 추천
- 영양 정보 및 가격 기반 필터링
- 개인화된 추천 시스템
- 구매 링크 제공 및 이력 관리
- 대화형 AI 인터페이스
- **냉장고 기능**: 구매한 재료 관리 및 냉장고 기반 레시피 추천

## 2. 도메인 분석

### 2.1 바운디드 컨텍스트

#### User Context (사용자 컨텍스트)
- **책임**: 사용자 정보, 선호도, 구매 이력 관리
- **핵심 엔티티**: User, UserPreferences
- **주요 기능**: 사용자 등록, 선호도 학습, 프로필 관리

#### Recipe Context (레시피 컨텍스트)
- **책임**: 레시피 정보, 재료, 영양 정보 관리
- **핵심 엔티티**: Recipe, RecipeIngredient
- **주요 기능**: 레시피 저장, 검색, 영양 정보 계산

#### Conversation Context (대화 컨텍스트)
- **책임**: AI 대화, 요청 분석, 세션 관리
- **핵심 엔티티**: ChatSession, ChatMessage
- **주요 기능**: 대화 세션 관리, 자연어 분석

#### Recommendation Context (추천 컨텍스트)
- **책임**: 추천 로직, 개인화 알고리즘
- **핵심 서비스**: RecipeRecommendationService
- **주요 기능**: 요구사항 기반 추천, 개인화 추천

#### Purchase Context (구매 컨텍스트)
- **책임**: 구매 링크, 가격 정보, 구매 이력
- **핵심 엔티티**: Purchase, PurchaseLink
- **주요 기능**: 가격 추정, 구매 링크 생성, 이력 추적

#### Refrigerator Context (냉장고 컨텍스트)
- **책임**: 냉장고 아이템 관리, 저장/소비 이력, 유통기한 관리
- **핵심 엔티티**: Refrigerator, RefrigeratorItem
- **주요 기능**: 아이템 저장, 조회, 소비 추적, 유통기한 알림, 냉장고 기반 추천

### 2.2 유비쿼터스 언어

| 용어                     | 정의                            |
|------------------------|-------------------------------|
| Recipe                 | 조리법, 재료 목록, 영양 정보를 포함한 요리 레시피 |
| Ingredient             | 레시피에 사용되는 재료                  |
| NutritionInfo          | 칼로리, 단백질, 탄수화물 등의 영양 정보       |
| PurchaseLink           | 재료 구매를 위한 외부 쇼핑몰 링크           |
| UserPreference         | 사용자의 음식 선호도, 알레르기 정보 등        |
| ChatSession            | 사용자와 AI 간의 대화 세션              |
| RecipeRequirement      | 사용자가 요청한 레시피 조건 (가격, 영양소 등)   |
| RecommendationCriteria | 추천 알고리즘에 사용되는 기준              |
| Refrigerator           | 사용자의 냉장고 (구매한 재료들을 저장)        |
| RefrigeratorItem       | 냉장고에 저장된 개별 재료 아이템            |
| StorageConfirmation    | 구매 후 냉장고 저장 확인 요청             |
| RecipeContent          | 레시피 콘텐츠 (영상, 웹 기사 등)          |
| ConversationFilter     | 대화 검열 및 필터링 규칙                |
| ModerationResult       | 메시지 검열 결과                     |
| ViolationRecord        | 사용자 위반 기록                     |
| ContentType            | 콘텐츠 유형 (영상, 웹 기사 등)           |

## 3. 도메인 모델 설계

### 3.1 애그리게이트 설계

#### User Aggregate
```kotlin
// 애그리게이트 루트
data class User(
    val id: UserId,
    val email: Email,
    val nickname: String,
    val preferences: UserPreferences,
    val purchaseHistory: List<PurchaseId>,
    val createdAt: Instant,
    val updatedAt: Instant
)

// 값 객체
@JvmInline
value class UserId(val value: String)

@JvmInline
value class Email(val value: String) {
    init {
        require(isValidEmail(value)) { "유효하지 않은 이메일 형식입니다." }
    }
}

data class UserPreferences(
    val favoriteIngredients: List<String>,
    val allergies: List<String>,
    val dietaryRestrictions: List<DietaryRestriction>,
    val preferredCuisineTypes: List<CuisineType>,
    val budgetRange: PriceRange
)
```

#### Recipe Aggregate
```kotlin
// 애그리게이트 루트
data class Recipe(
    val id: RecipeId,
    val title: String,
    val description: String,
    val ingredients: List<RecipeIngredient>,
    val instructions: List<String>,
    val nutritionInfo: NutritionInfo,
    val estimatedPrice: Price,
    val cookingTime: CookingTime,
    val difficulty: Difficulty,
    val tags: List<RecipeTag>,
    val contents: List<RecipeContent>,  // 새로 추가: 다양한 콘텐츠 형식
    val createdAt: Instant
) {
    fun getContentByType(type: ContentType): List<RecipeContent> {
        return contents.filter { it.type == type }
    }

    fun hasVideoContent(): Boolean = contents.any { it.type == ContentType.VIDEO }
    fun hasWebContent(): Boolean = contents.any { it.type == ContentType.WEB_ARTICLE }

    fun getPrimaryContent(): RecipeContent? {
        return contents.find { it.isPrimary } ?: contents.firstOrNull()
    }
}

// 새로운 값 객체: 레시피 콘텐츠
data class RecipeContent(
    val id: RecipeContentId,
    val type: ContentType,
    val url: String,
    val title: String,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val duration: Duration? = null,  // 영상의 경우
    val author: String? = null,
    val isPrimary: Boolean = false,
    val isVerified: Boolean = false,
    val createdAt: Instant
) {
    init {
        require(isValidUrl(url)) { "유효하지 않은 URL입니다: $url" }
        require(title.isNotBlank()) { "콘텐츠 제목은 필수입니다." }
        if (type == ContentType.VIDEO) {
            require(duration != null) { "영상 콘텐츠는 재생 시간이 필요합니다." }
        }
    }
}

@JvmInline
value class RecipeContentId(val value: String)

// 콘텐츠 타입 열거형
enum class ContentType(val displayName: String) {
    VIDEO("영상"),
    WEB_ARTICLE("웹 기사"),
    BLOG_POST("블로그 포스트"),
    PDF_GUIDE("PDF 가이드"),
    IMAGE_GALLERY("이미지 갤러리"),
    PODCAST("팟캐스트"),
    LIVE_STREAM("라이브 스트림")
}

// 엔티티
data class RecipeIngredient(
    val name: String,
    val quantity: Quantity,
    val unit: Unit,
    val isOptional: Boolean = false
)

// 값 객체
@JvmInline
value class RecipeId(val value: String)

data class NutritionInfo(
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double,
    val sodium: Double
)

data class Price(
    val amount: BigDecimal,
    val currency: Currency = Currency.KRW
)

@JvmInline
value class CookingTime(val minutes: Int)

enum class Difficulty(val level: Int) {
    EASY(1),
    MEDIUM(2),
    HARD(3)
}
```

#### ChatSession Aggregate
```kotlin
// 애그리게이트 루트
data class ChatSession(
    val id: ChatSessionId,
    val userId: UserId,
    val messages: List<ChatMessage>,
    val context: ConversationContext,
    val status: SessionStatus,
    val moderationSettings: ConversationFilter,  // 새로 추가
    val violationCount: Int = 0,  // 새로 추가
    val createdAt: Instant,
    val updatedAt: Instant
) {
    fun addMessage(message: ChatMessage, moderationResult: ModerationResult): ChatSession {
        return when (moderationResult) {
            is ModerationResult.Approved -> {
                copy(
                    messages = messages + message,
                    updatedAt = Instant.now()
                )
            }
            is ModerationResult.Rejected -> {
                copy(
                    violationCount = violationCount + 1,
                    status = if (violationCount + 1 >= 3) SessionStatus.SUSPENDED else status,
                    updatedAt = Instant.now()
                )
            }
        }
    }

    fun isSuspended(): Boolean = status == SessionStatus.SUSPENDED
    fun canAcceptMessages(): Boolean = status == SessionStatus.ACTIVE && violationCount < 3
}

// 엔티티
data class ChatMessage(
    val id: ChatMessageId,
    val content: String,
    val sender: MessageSender,
    val timestamp: Instant,
    val messageType: MessageType,
    val intent: ConversationIntent? = null,  // 새로 추가
    val moderationResult: ModerationResult? = null  // 새로 추가
)

// 값 객체
@JvmInline
value class ChatSessionId(val value: String)

data class ConversationContext(
    val lastRecipeRequirement: RecipeRequirement?,
    val recommendedRecipes: List<RecipeId>,
    val currentStep: ConversationStep
)

enum class SessionStatus {
    ACTIVE,
    COMPLETED,
    EXPIRED,
    SUSPENDED  // 새로 추가
}

enum class MessageSender {
    USER,
    AI
}

enum class MessageType {
    TEXT,
    RECIPE_RECOMMENDATION,
    INGREDIENT_LIST,
    PURCHASE_LINK,
    MODERATION_WARNING,  // 새로 추가
    SYSTEM_MESSAGE       // 새로 추가
}

// 대화 검열 관련 값 객체
data class ConversationFilter(
    val allowedTopics: List<String> = listOf("recipe", "cooking", "food", "nutrition", "ingredient"),
    val blockedKeywords: List<String> = listOf("weather", "sports", "politics", "personal"),
    val strictMode: Boolean = true
) {
    fun isTopicAllowed(message: String): Boolean {
        val lowercaseMessage = message.lowercase()

        // 블로킹된 키워드 체크
        if (blockedKeywords.any { lowercaseMessage.contains(it) }) {
            return false
        }

        // 허용된 주제 체크 (엄격 모드에서만)
        if (strictMode) {
            return allowedTopics.any { lowercaseMessage.contains(it) }
        }

        return true
    }
}

sealed class ModerationResult {
    object Approved : ModerationResult()
    data class Rejected(
        val reason: ModerationReason,
        val suggestedAlternative: String? = null
    ) : ModerationResult()

    fun isApproved(): Boolean = this is Approved
    fun isRejected(): Boolean = this is Rejected
}

enum class ModerationReason(val message: String) {
    OFF_TOPIC("레시피와 관련된 질문만 가능합니다."),
    INAPPROPRIATE_CONTENT("부적절한 내용이 포함되어 있습니다."),
    SPAM_DETECTED("스퓸으로 감지되었습니다."),
    TOO_GENERAL("더 구체적인 레시피 관련 질문을 해주세요.")
}

enum class ConversationIntent {
    RECIPE_REQUEST,        // 레시피 요청
    INGREDIENT_INQUIRY,    // 재료 문의
    NUTRITION_QUESTION,    // 영양 정보 질문
    COOKING_HELP,         // 요리 도움
    REFRIGERATOR_CHECK,   // 냉장고 확인
    PURCHASE_INQUIRY,     // 구매 관련 문의
    OFF_TOPIC,           // 주제 벗어남
    UNCLEAR              // 의도 불명확
}
```

#### Purchase Aggregate
```kotlin
// 애그리게이트 루트
data class Purchase(
    val id: PurchaseId,
    val userId: UserId,
    val recipeId: RecipeId,
    val purchasedItems: List<PurchasedItem>,
    val totalPrice: Price,
    val purchaseDate: Instant,
    val status: PurchaseStatus
)

// 엔티티
data class PurchasedItem(
    val ingredientName: String,
    val quantity: Quantity,
    val unitPrice: Price,
    val purchaseLink: PurchaseLink
)

// 값 객체
@JvmInline
value class PurchaseId(val value: String)

data class PurchaseLink(
    val url: String,
    val retailer: String,
    val isActive: Boolean,
    val expiresAt: Instant?
)

enum class PurchaseStatus {
    PENDING,
    COMPLETED,
    CANCELLED
}
```

#### Refrigerator Aggregate
```kotlin
// 냉장고 애그리게이트 루트
data class Refrigerator(
    val id: RefrigeratorId,
    val userId: UserId,
    val items: List<RefrigeratorItem>,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    fun addItem(item: RefrigeratorItem): Refrigerator {
        val existingItem = items.find { it.ingredientName == item.ingredientName }
        val updatedItems = if (existingItem != null) {
            items.map {
                if (it.ingredientName == item.ingredientName) {
                    it.addQuantity(item.quantity)
                } else it
            }
        } else {
            items + item
        }

        return copy(
            items = updatedItems,
            updatedAt = Instant.now()
        )
    }

    fun getAvailableItems(): List<RefrigeratorItem> {
        return items.filter { !it.isExpired() && it.quantity.amount > 0 }
    }

    fun getExpiringItems(withinDays: Int): List<RefrigeratorItem> {
        val threshold = Instant.now().plus(Duration.ofDays(withinDays.toLong()))
        return items.filter {
            it.expiryDate != null &&
            it.expiryDate.isBefore(threshold) &&
            !it.isExpired()
        }
    }
}

// 냉장고 아이템 엔티티
data class RefrigeratorItem(
    val id: RefrigeratorItemId,
    val ingredientName: String,
    val quantity: Quantity,
    val unit: Unit,
    val expiryDate: Instant?,
    val purchaseDate: Instant,
    val purchaseLink: PurchaseLink?,
    val storageType: StorageType,
    val isConsumed: Boolean = false
) {
    fun isExpired(): Boolean {
        return expiryDate?.isBefore(Instant.now()) ?: false
    }

    fun addQuantity(additionalQuantity: Quantity): RefrigeratorItem {
        require(unit == additionalQuantity.unit) { "단위가 다릅니다." }
        return copy(
            quantity = quantity.copy(amount = quantity.amount + additionalQuantity.amount)
        )
    }
}

// 값 객체들
@JvmInline
value class RefrigeratorId(val value: String)

@JvmInline
value class RefrigeratorItemId(val value: String)

data class Quantity(
    val amount: Double,
    val unit: Unit
) {
    init {
        require(amount >= 0) { "수량은 음수일 수 없습니다." }
    }
}

enum class Unit(val symbol: String) {
    GRAM("g"),
    KILOGRAM("kg"),
    PIECE("개"),
    LITER("L"),
    MILLILITER("ml"),
    TABLESPOON("큰술"),
    TEASPOON("작은술"),
    CUP("컵")
}

enum class StorageType {
    REFRIGERATOR,    // 냉장
    FREEZER,         // 냉동
    PANTRY,          // 실온
    FRESH            // 신선식품
}

data class StorageConfirmation(
    val purchaseId: PurchaseId,
    val ingredientName: String,
    val quantity: Quantity,
    val userConfirmed: Boolean,
    val confirmedAt: Instant?
)
```

### 3.2 도메인 이벤트

```kotlin
sealed class DomainEvent {
    abstract val occurredAt: Instant
    abstract val aggregateId: String
}

data class UserRegistered(
    override val aggregateId: String,
    val userId: UserId,
    val email: Email,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent()

data class RecipeRecommended(
    override val aggregateId: String,
    val userId: UserId,
    val recipeId: RecipeId,
    val sessionId: ChatSessionId,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent()

data class PurchaseCompleted(
    override val aggregateId: String,
    val userId: UserId,
    val purchaseId: PurchaseId,
    val totalPrice: Price,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent()

data class UserPreferenceUpdated(
    override val aggregateId: String,
    val userId: UserId,
    val previousPreferences: UserPreferences,
    val newPreferences: UserPreferences,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent()

data class ItemAddedToRefrigerator(
    override val aggregateId: String,
    val userId: UserId,
    val refrigeratorId: RefrigeratorId,
    val itemId: RefrigeratorItemId,
    val ingredientName: String,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent()

data class RefrigeratorItemExpiring(
    override val aggregateId: String,
    val userId: UserId,
    val refrigeratorId: RefrigeratorId,
    val itemId: RefrigeratorItemId,
    val expiryDate: Instant,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent()

data class RefrigeratorBasedRecommendationRequested(
    override val aggregateId: String,
    val userId: UserId,
    val availableItems: List<RefrigeratorItemId>,
    val requirement: RecipeRequirement,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent()

data class PurchaseStorageConfirmationRequested(
    override val aggregateId: String,
    val userId: UserId,
    val purchaseId: PurchaseId,
    val ingredientName: String,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent()

// 검열 관련 이벤트
data class InappropriateRequestBlocked(
    override val aggregateId: String,
    val userId: UserId,
    val sessionId: ChatSessionId,
    val message: String,
    val reason: ModerationReason,
    val severity: ViolationSeverity,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent()

data class UserSessionSuspended(
    override val aggregateId: String,
    val userId: UserId,
    val sessionId: ChatSessionId,
    val violationCount: Int,
    val suspensionDuration: Duration?,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent()

// 콘텐츠 관련 이벤트
data class RecipeContentAccessed(
    override val aggregateId: String,
    val userId: UserId,
    val recipeId: RecipeId,
    val contentId: RecipeContentId,
    val contentType: ContentType,
    val accessDuration: Duration?,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent()

data class RecipeContentAdded(
    override val aggregateId: String,
    val recipeId: RecipeId,
    val contentId: RecipeContentId,
    val contentType: ContentType,
    val addedBy: UserId?,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent()

// 사용자 행동 분석 이벤트
data class ConversationIntentAnalyzed(
    override val aggregateId: String,
    val userId: UserId,
    val sessionId: ChatSessionId,
    val message: String,
    val detectedIntent: ConversationIntent,
    val confidence: Double,
    val relevanceScore: Double,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent()

enum class ViolationSeverity(val level: Int) {
    LOW(1),      // 경고
    MEDIUM(2),   // 임시 제한
    HIGH(3),     // 세션 정지
    CRITICAL(4)  // 계정 정지
}
```

## 4. 도메인 서비스

### 4.1 Recipe 도메인 서비스

#### RecipeRecommendationService (냉장고 기능 추가)
```kotlin
interface RecipeRecommendationService {
    /**
     * 기본 레시피 추천
     */
    suspend fun recommendRecipes(
        userId: UserId,
        requirement: RecipeRequirement,
        limit: Int = 10
    ): List<Recipe>

    /**
     * 개인화된 추천 (냉장고 고려)
     */
    suspend fun getPersonalizedRecommendations(
        userId: UserId,
        limit: Int = 10,
        considerRefrigerator: Boolean = true
    ): List<Recipe>

    /**
     * 냉장고 재료 우선 추천
     */
    suspend fun recommendRecipesWithRefrigeratorPriority(
        userId: UserId,
        requirement: RecipeRequirement,
        minimumRefrigeratorMatch: Double = 0.5,
        limit: Int = 10
    ): List<RecipeWithRefrigeratorInfo>

    /**
     * 완전 냉장고 기반 추천
     */
    suspend fun recommendFromRefrigeratorOnly(
        userId: UserId,
        requirement: RecipeRequirement? = null
    ): List<RecipeMatch>

    suspend fun findSimilarRecipes(
        recipeId: RecipeId,
        limit: Int = 5
    ): List<Recipe>
}
```

#### NutritionCalculatorService
```kotlin
interface NutritionCalculatorService {
    fun calculateNutrition(ingredients: List<RecipeIngredient>): NutritionInfo

    fun meetsNutritionRequirements(
        nutrition: NutritionInfo,
        requirements: NutritionRequirement
    ): Boolean

    fun calculateDailyNutritionRatio(
        nutrition: NutritionInfo,
        userProfile: UserNutritionProfile
    ): NutritionRatio
}
```

### 4.2 Content 도메인 서비스

#### RecipeContentService
```kotlin
interface RecipeContentService {
    suspend fun addContentToRecipe(
        recipeId: RecipeId,
        content: RecipeContent
    ): Recipe

    suspend fun validateContent(content: RecipeContent): ContentQuality

    suspend fun filterContentByPreference(
        userId: UserId,
        contents: List<RecipeContent>
    ): List<RecipeContent>

    suspend fun checkContentAccessibility(
        content: RecipeContent
    ): ContentAccessibilityStatus
}

enum class ContentAccessibilityStatus {
    ACCESSIBLE,    // 접근 가능
    BROKEN_LINK,   // 링크 깨짐
    PREMIUM_ONLY,  // 유료 콘텐츠
    REGION_BLOCKED // 지역 차단
}
```

### 4.3 Moderation 도메인 서비스

#### ConversationModerationService
```kotlin
interface ConversationModerationService {
    suspend fun moderateMessage(
        message: String,
        userId: UserId,
        sessionId: ChatSessionId
    ): ModerationResult

    suspend fun analyzeConversationIntent(
        message: String,
        conversationContext: ConversationContext
    ): ConversationIntent

    suspend fun recordViolation(
        userId: UserId,
        sessionId: ChatSessionId,
        reason: ModerationReason
    ): ViolationRecord

    suspend fun updateSessionStatus(
        sessionId: ChatSessionId,
        violationCount: Int
    ): SessionStatus
}

data class ViolationRecord(
    val id: ViolationRecordId,
    val userId: UserId,
    val sessionId: ChatSessionId,
    val message: String,
    val reason: ModerationReason,
    val severity: ViolationSeverity,
    val timestamp: Instant,
    val resolved: Boolean = false
)

@JvmInline
value class ViolationRecordId(val value: String)
```

### 4.4 Refrigerator 도메인 서비스

#### RefrigeratorService
```kotlin
interface RefrigeratorService {
    suspend fun getRefrigerator(userId: UserId): Refrigerator?
    suspend fun createRefrigerator(userId: UserId): Refrigerator
    suspend fun addItemToRefrigerator(
        userId: UserId,
        item: RefrigeratorItem
    ): Refrigerator
    suspend fun removeItemFromRefrigerator(
        userId: UserId,
        itemId: RefrigeratorItemId
    ): Refrigerator
    suspend fun updateItemQuantity(
        userId: UserId,
        itemId: RefrigeratorItemId,
        quantity: Quantity
    ): Refrigerator
}
```

#### RefrigeratorRecommendationService
```kotlin
interface RefrigeratorRecommendationService {
    suspend fun recommendRecipesFromRefrigerator(
        userId: UserId,
        requirement: RecipeRequirement? = null
    ): List<Recipe>

    suspend fun findRecipesWithAvailableIngredients(
        userId: UserId,
        minimumMatchRatio: Double = 0.7
    ): List<RecipeMatch>

    suspend fun calculateMissingIngredients(
        userId: UserId,
        recipeId: RecipeId
    ): List<MissingIngredient>

    suspend fun recommendRecipesForExpiringItems(
        userId: UserId,
        withinDays: Int = 3
    ): List<Recipe>
}
```

#### StorageConfirmationService
```kotlin
interface StorageConfirmationService {
    suspend fun requestStorageConfirmation(
        userId: UserId,
        purchaseId: PurchaseId,
        ingredientName: String
    ): StorageConfirmation

    suspend fun processStorageConfirmation(
        userId: UserId,
        confirmationId: String,
        confirmed: Boolean
    ): RefrigeratorItem?

    suspend fun getPendingStorageConfirmations(
        userId: UserId
    ): List<StorageConfirmation>
}
```

### 4.3 Purchase 도메인 서비스

#### PriceEstimatorService
```kotlin
interface PriceEstimatorService {
    suspend fun estimateRecipePrice(recipeId: RecipeId): Price

    suspend fun findBestPriceLinks(
        ingredients: List<RecipeIngredient>
    ): List<PurchaseLink>

    suspend fun filterByPriceRange(
        recipes: List<Recipe>,
        minPrice: Price,
        maxPrice: Price
    ): List<Recipe>
}
```

### 4.5 Conversation 도메인 서비스

#### ConversationAnalyzerService
```kotlin
interface ConversationAnalyzerService {
    suspend fun analyzeUserRequest(message: String): RecipeRequirement

    suspend fun analyzeConversationContext(
        session: ChatSession
    ): ConversationContext

    suspend fun parseNaturalLanguageQuery(
        query: String
    ): SearchCriteria
}
```

### 4.6 User 도메인 서비스

#### UserPreferenceService
```kotlin
interface UserPreferenceService {
    suspend fun updatePreferencesFromBehavior(
        userId: UserId,
        behavior: UserBehavior
    ): UserPreferences

    suspend fun analyzePreferencesFromPurchases(
        userId: UserId,
        purchases: List<Purchase>
    ): UserPreferences

    suspend fun generateRecommendationCriteria(
        userId: UserId
    ): RecommendationCriteria
}
```

## 5. 리포지토리 인터페이스

### 5.1 핵심 리포지토리

```kotlin
interface UserRepository {
    suspend fun findById(id: UserId): User?
    suspend fun findByEmail(email: Email): User?
    suspend fun save(user: User): User
    suspend fun delete(id: UserId)
}

interface RecipeRepository {
    suspend fun findById(id: RecipeId): Recipe?
    suspend fun findByRequirement(requirement: RecipeRequirement): List<Recipe>
    suspend fun findByTag(tag: RecipeTag): List<Recipe>
    suspend fun save(recipe: Recipe): Recipe
    suspend fun searchByKeyword(keyword: String): List<Recipe>
}

interface ChatSessionRepository {
    suspend fun findById(id: ChatSessionId): ChatSession?
    suspend fun findActiveSessionsByUser(userId: UserId): List<ChatSession>
    suspend fun save(session: ChatSession): ChatSession
    suspend fun findInactiveSessions(threshold: Instant): List<ChatSession>
}

interface PurchaseRepository {
    suspend fun findById(id: PurchaseId): Purchase?
    suspend fun findByUserId(userId: UserId): List<Purchase>
    suspend fun save(purchase: Purchase): Purchase
    suspend fun findByDateRange(startDate: Instant, endDate: Instant): List<Purchase>
}

interface RefrigeratorRepository {
    suspend fun findByUserId(userId: UserId): Refrigerator?
    suspend fun save(refrigerator: Refrigerator): Refrigerator
    suspend fun findItemById(itemId: RefrigeratorItemId): RefrigeratorItem?
    suspend fun findExpiringItems(withinDays: Int): List<RefrigeratorItem>
    suspend fun findItemsByIngredientName(
        userId: UserId,
        ingredientName: String
    ): List<RefrigeratorItem>
}

interface StorageConfirmationRepository {
    suspend fun save(confirmation: StorageConfirmation): StorageConfirmation
    suspend fun findById(confirmationId: String): StorageConfirmation?
    suspend fun findPendingByUserId(userId: UserId): List<StorageConfirmation>
    suspend fun markAsProcessed(confirmationId: String)
}

interface ViolationRecordRepository {
    suspend fun save(record: ViolationRecord): ViolationRecord
    suspend fun findByUserId(userId: UserId): List<ViolationRecord>
    suspend fun findBySessionId(sessionId: ChatSessionId): List<ViolationRecord>
    suspend fun findRecentViolations(
        userId: UserId,
        since: Instant
    ): List<ViolationRecord>
    suspend fun markAsResolved(recordId: ViolationRecordId)
}

interface RecipeContentRepository {
    suspend fun save(content: RecipeContent): RecipeContent
    suspend fun findByRecipeId(recipeId: RecipeId): List<RecipeContent>
    suspend fun findByType(type: ContentType): List<RecipeContent>
    suspend fun findById(contentId: RecipeContentId): RecipeContent?
    suspend fun updateAccessCount(contentId: RecipeContentId)
}
```

## 6. 도메인 규칙 및 불변성

### 6.1 비즈니스 규칙

#### User 애그리게이트
- 사용자 이메일은 유일해야 함
- 사용자 선호도는 언제든지 업데이트 가능
- 구매 이력은 삭제되지 않고 누적됨

#### Recipe 애그리게이트
- 레시피는 최소 1개 이상의 재료를 포함해야 함
- 조리 시간은 양수여야 함
- 영양 정보의 수치는 음수일 수 없음

#### ChatSession 애그리게이트
- 세션은 30분 동안 비활성 상태이면 자동으로 만료됨
- 하나의 사용자는 동시에 최대 3개의 활성 세션을 가질 수 있음
- 메시지는 시간순으로 정렬되어야 함

#### Purchase 애그리게이트
- 구매 총액은 개별 아이템 가격의 합과 일치해야 함
- 구매 완료 후에는 수정할 수 없음
- 구매 링크는 만료 시간이 있을 수 있음

#### Refrigerator 애그리게이트
- 냉장고는 사용자당 하나만 존재함
- 동일한 재료는 수량을 합산하여 저장함
- 유통기한이 지난 아이템은 자동으로 식별됨
- 아이템 소비 시 수량이 0이 되면 자동으로 소비 완료 상태로 변경됨
- 냉장고 아이템은 구매 링크와 연결되어 추적 가능함

### 6.2 불변성 보장

#### 값 객체 불변성
- 모든 값 객체는 불변(immutable)으로 설계
- 상태 변경 시 새로운 객체 생성
- 유효성 검증은 생성 시점에 수행

#### 엔티티 불변성
- 엔티티 ID는 생성 후 변경 불가
- 도메인 규칙 위반 시 예외 발생
- 상태 변경은 도메인 메서드를 통해서만 가능

## 7. 통합 시나리오

### 7.1 레시피 추천 시나리오

1. **사용자 요청 접수**
   - 사용자가 자연어로 레시피 요청
   - ChatSession에 메시지 저장

2. **요청 분석**
   - ConversationAnalyzerService가 요청 분석
   - RecipeRequirement 객체 생성

3. **레시피 추천**
   - RecipeRecommendationService가 추천 로직 수행
   - 사용자 선호도, 구매 이력, 냉장고 보유 재료 고려

4. **결과 반환**
   - 추천 레시피 목록 반환
   - 가격 정보 및 구매 링크 포함
   - 냉장고 보유 재료 활용도 정보 포함

### 7.2 대화 검열 시나리오

1. **메시지 수신 및 검열**
   - 사용자가 "오늘 날씨 어때?" 같은 비관련 질문 전송
   - ConversationModerationService가 메시지 검열 수행
   - ConversationFilter로 주제 및 키워드 검사

2. **위반 감지 및 처리**
   - OFF_TOPIC 위반으로 분류
   - ViolationRecord 생성 및 저장
   - 사용자에게 경고 메시지 전송

3. **세션 상태 업데이트**
   - violationCount 증가
   - 3회 이상 위반 시 SessionStatus.SUSPENDED로 변경
   - InappropriateRequestBlocked 이벤트 발행

4. **대안 제안**
   - "레시피와 관련된 질문만 가능합니다" 안내
   - 예시 질문 제공 ("단백질이 풍부한 레시피 추천해주세요")

### 7.3 냉장고 기반 추천 시나리오

1. **냉장고 재료 확인**
   - 사용자가 "냉장고에 있는 재료로 만들 수 있는 요리 추천해줘" 요청
   - RefrigeratorService가 사용 가능한 재료 목록 조회

2. **레시피 매칭**
   - RefrigeratorRecommendationService가 냉장고 재료와 매칭되는 레시피 검색
   - 매칭 비율 계산 및 부족 재료 식별

3. **추천 결과 제공**
   - 매칭 비율이 높은 레시피 우선 추천
   - 부족 재료 및 예상 추가 비용 정보 제공

4. **사용 계획 제안**
   - 유통기한 임박 재료 우선 사용 레시피 추천
   - 재료 소비 후 냉장고 상태 업데이트

### 7.3 구매 및 냉장고 저장 시나리오

1. **구매 의향 확인**
   - 사용자가 특정 레시피 선택
   - 구매 링크 클릭

2. **구매 정보 생성**
   - PriceEstimatorService가 가격 계산
   - PurchaseLink 생성

3. **구매 이력 저장**
   - Purchase 애그리게이트 생성
   - 사용자 선호도 업데이트

4. **냉장고 저장 확인**
   - "냉장고에 저장하시겠습니까?" 질문 표시
   - 사용자 응답에 따라 RefrigeratorItem 생성

5. **이벤트 발행**
   - PurchaseCompleted 이벤트 발행
   - ItemAddedToRefrigerator 이벤트 발행 (저장 시)
   - 추천 알고리즘 학습 데이터로 활용

### 7.4 유통기한 관리 시나리오

1. **유통기한 임박 감지**
   - 매일 정해진 시간에 RefrigeratorNotificationService 실행
   - 3일 이내 유통기한 만료 예정 아이템 식별

2. **알림 및 추천**
   - 사용자에게 유통기한 임박 알림 전송
   - 해당 재료를 활용할 수 있는 레시피 추천

3. **자동 정리 제안**
   - 만료된 아이템 자동 제거 제안
   - 냉장고 정리 가이드 제공

## 8. 기술적 고려사항

### 8.1 성능 최적화
- 레시피 검색 시 인덱스 활용
- 추천 결과 캐싱
- 비동기 처리를 통한 응답 시간 최적화

### 8.2 확장성
- 마이크로서비스 아키텍처 준비
- 이벤트 기반 아키텍처 적용
- 수평 확장 가능한 설계

### 8.3 보안
- 사용자 개인정보 보호
- 구매 링크 위변조 방지
- API 인증 및 권한 관리

## 9. 개발 계획

### 9.1 1단계: 핵심 도메인 구현
- User, Recipe 애그리게이트 구현
- 기본 CRUD 리포지토리 구현
- 단위 테스트 작성

### 9.2 2단계: 추천 시스템 구현
- RecipeRecommendationService 구현
- 기본 추천 알고리즘 구현
- 통합 테스트 작성

### 9.3 3단계: 대화 기능 구현
- ChatSession 애그리게이트 구현
- ConversationAnalyzerService 구현
- AI 통합 준비

### 9.4 4단계: 구매 기능 구현
- Purchase 애그리게이트 구현
- PriceEstimatorService 구현
- 외부 쇼핑몰 API 연동

### 9.5 5단계: 냉장고 기능 구현
- Refrigerator 애그리게이트 구현
- RefrigeratorService 및 RefrigeratorRecommendationService 구현
- StorageConfirmationService 구현
- 냉장고 기반 추천 알고리즘 개발
- 유통기한 관리 및 알림 시스템 구현

## 추가된 기능 변경사항

### 주요 변경 내용
1. **대화 검열 기능**: Moderation Context 추가
   - 레시피 관련 대화로만 제한
   - 어뷐징 방지 및 사용자 행동 모니터링
   - ConversationModerationService 및 ViolationRecord 관리

2. **콘텐츠 다양화**: Content Context 추가
   - 레시피에 영상 URL, 웹 URL 등 다양한 콘텐츠 형식 지원
   - RecipeContent 애그리게이트 및 RecipeContentService
   - 콘텐츠 품질 검증 및 접근성 관리

3. **냉장고 기능**: Refrigerator Context 추가
   - 구매한 재료 관리 및 냉장고 기반 레시피 추천
   - RefrigeratorService, RefrigeratorRecommendationService, StorageConfirmationService
   - 유통기한 관리 및 알림 시스템

### 비즈니스 로직 강화
- **대화 제한**: 레시피 관련 주제 이외 차단
- **콘텐츠 큐레이션**: 다양한 형식의 레시피 콘텐츠 제공
- **개인화**: 냉장고 상태 기반 맞춤형 추천
- **품질 관리**: 콘텐츠 품질 검증 및 사용자 행동 추적

### 개발 우선순위 업데이트
- **고우선순위**: 대화 검열 시스템 및 콘텐츠 다양화
- **중우선순위**: 냉장고 기본 기능 및 추천 알고리즘
- **저우선순위**: 고급 기능 (유통기한 관리, 사용자 행동 분석)

이 문서는 CookBot 서버의 도메인 설계 기반이 되며, 개발 과정에서 지속적으로 업데이트될 예정입니다.
