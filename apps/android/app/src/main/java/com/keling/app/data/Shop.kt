package com.keling.app.data

/**
 * =========================
 * 商城系统
 * =========================
 *
 * 虚拟商城与道具系统
 * - 商品展示与购买
 * - 主题皮肤
 * - 道具效果
 * - VIP特权
 * - 限时折扣
 */

import kotlinx.serialization.Serializable

// ==================== 商品模型 ====================

/**
 * 商品分类
 */
enum class ShopCategory(val displayName: String, val icon: String) {
    THEME("主题皮肤", "🎨"),
    PROP("道具", "🧪"),
    PRIVILEGE("特权", "👑"),
    AVATAR("头像框", "🖼️"),
    SPECIAL("限时特惠", "⭐")
}

/**
 * 商品稀有度
 */
enum class ItemRarity(val displayName: String, val color: Long) {
    COMMON("普通", 0xFF9E9E9E),
    RARE("稀有", 0xFF42A5F5),
    EPIC("史诗", 0xFFAB47BC),
    LEGENDARY("传说", 0xFFFFA726),
    MYTHIC("神话", 0xFFE91E63)
}

/**
 * 商品
 */
@Serializable
data class ShopItem(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String? = null,
    val category: ShopCategory,
    val rarity: ItemRarity = ItemRarity.COMMON,
    val price: Int,                          // 结晶价格
    val originalPrice: Int? = null,          // 原价（打折时显示）
    val discount: Float? = null,             // 折扣 0.8 = 8折
    val stock: Int? = null,                  // 库存（null为无限）
    val soldCount: Int = 0,                  // 已售数量
    val limitedTime: Long? = null,           // 限时结束时间
    val effect: ItemEffect? = null,          // 道具效果
    val preview: ItemPreview? = null,        // 预览信息
    val isNew: Boolean = false,
    val isHot: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 道具效果
 */
@Serializable
data class ItemEffect(
    val type: EffectType,
    val value: Float,
    val duration: Int? = null,               // 持续时间(小时)，null为永久
    val description: String
)

/**
 * 效果类型
 */
enum class EffectType {
    ENERGY_BOOST,      // 能量加成
    EXP_BOOST,         // 经验加成
    CRYSTAL_BOOST,     // 结晶加成
    STREAK_PROTECT,    // 断签保护
    FOCUS_BOOST,       // 专注加成
    MASTERY_BOOST,     // 掌握度加成
    UNLOCK_THEME,      // 解锁主题
    UNLOCK_AVATAR,     // 解锁头像框
    VIP_STATUS         // VIP状态
}

/**
 * 商品预览
 */
@Serializable
data class ItemPreview(
    val previewType: PreviewType,
    val previewData: String                  // 预览数据（图片URL、颜色值等）
)

enum class PreviewType {
    IMAGE,
    COLOR,
    ANIMATION,
    THEME_CONFIG
}

/**
 * 用户背包物品
 */
@Serializable
data class InventoryItem(
    val itemId: String,
    val itemName: String,
    val category: ShopCategory,
    val quantity: Int,
    val purchasedAt: Long,
    val expiresAt: Long? = null,             // 有效期
    val isEquipped: Boolean = false,         // 是否装备中
    val effect: ItemEffect? = null
)

/**
 * 购买记录
 */
@Serializable
data class PurchaseRecord(
    val id: String,
    val userId: String,
    val itemId: String,
    val itemName: String,
    val quantity: Int,
    val totalPrice: Int,
    val purchasedAt: Long,
    val status: PurchaseStatus = PurchaseStatus.COMPLETED
)

enum class PurchaseStatus {
    PENDING,
    COMPLETED,
    CANCELLED,
    REFUNDED
}

// ==================== 商城管理器 ====================

/**
 * 商城管理器
 */
class ShopManager {
    private val shopItems = mutableListOf<ShopItem>()
    private val userInventory = mutableMapOf<String, MutableList<InventoryItem>>()
    private val purchaseHistory = mutableListOf<PurchaseRecord>()

    init {
        initDefaultItems()
    }

    /**
     * 初始化默认商品
     */
    private fun initDefaultItems() {
        shopItems.addAll(DEFAULT_SHOP_ITEMS)
    }

    /**
     * 获取商城商品
     */
    fun getShopItems(category: ShopCategory? = null): List<ShopItem> {
        val now = System.currentTimeMillis()

        return shopItems.filter { item ->
            // 过滤已过期的限时商品
            item.limitedTime == null || item.limitedTime > now
        }.filter { item ->
            // 按分类过滤
            category == null || item.category == category
        }.sortedWith(
            compareBy<ShopItem> {
                // 限时商品优先
                if (it.limitedTime != null && it.limitedTime > now) 0 else 1
            }.thenByDescending {
                // 热门优先
                if (it.isHot) 1 else 0
            }.thenByDescending {
                // 新品优先
                if (it.isNew) 1 else 0
            }.thenBy {
                // 按稀有度排序
                it.rarity.ordinal
            }
        )
    }

    /**
     * 获取限时特惠
     */
    fun getLimitedTimeOffers(): List<ShopItem> {
        val now = System.currentTimeMillis()
        return shopItems.filter {
            it.limitedTime != null && it.limitedTime > now
        }.sortedBy { it.limitedTime }
    }

    /**
     * 购买商品
     */
    fun purchaseItem(
        userId: String,
        itemId: String,
        quantity: Int = 1,
        userCrystals: Int
    ): PurchaseResult {
        val item = shopItems.find { it.id == itemId }
            ?: return PurchaseResult.Failed("商品不存在")

        // 检查库存
        if (item.stock != null && item.stock < quantity) {
            return PurchaseResult.Failed("库存不足")
        }

        // 检查限时
        if (item.limitedTime != null && item.limitedTime < System.currentTimeMillis()) {
            return PurchaseResult.Failed("活动已结束")
        }

        // 计算价格
        val price = if (item.discount != null) {
            (item.price * item.discount * quantity).toInt()
        } else {
            item.price * quantity
        }

        // 检查余额
        if (userCrystals < price) {
            return PurchaseResult.InsufficientFunds(price - userCrystals)
        }

        // 创建背包物品
        val inventoryItem = InventoryItem(
            itemId = item.id,
            itemName = item.name,
            category = item.category,
            quantity = quantity,
            purchasedAt = System.currentTimeMillis(),
            expiresAt = if (item.effect?.duration != null) {
                System.currentTimeMillis() + item.effect.duration * 60 * 60 * 1000L
            } else null,
            effect = item.effect
        )

        // 添加到背包
        userInventory.getOrPut(userId) { mutableListOf() }.add(inventoryItem)

        // 记录购买
        purchaseHistory.add(
            PurchaseRecord(
                id = "purchase_${System.currentTimeMillis()}",
                userId = userId,
                itemId = item.id,
                itemName = item.name,
                quantity = quantity,
                totalPrice = price,
                purchasedAt = System.currentTimeMillis()
            )
        )

        // 更新库存
        if (item.stock != null) {
            val index = shopItems.indexOf(item)
            if (index >= 0) {
                shopItems[index] = item.copy(
                    stock = item.stock - quantity,
                    soldCount = item.soldCount + quantity
                )
            }
        }

        return PurchaseResult.Success(
            remainingCrystals = userCrystals - price,
            item = inventoryItem,
            totalPaid = price
        )
    }

    /**
     * 获取用户背包
     */
    fun getUserInventory(userId: String): List<InventoryItem> {
        val now = System.currentTimeMillis()
        return userInventory[userId]?.filter {
            // 过滤已过期的物品
            it.expiresAt == null || it.expiresAt > now
        } ?: emptyList()
    }

    /**
     * 使用/装备物品
     */
    fun equipItem(userId: String, itemId: String): Boolean {
        val inventory = userInventory[userId] ?: return false
        val item = inventory.find { it.itemId == itemId } ?: return false

        // 先取消同类型其他物品的装备
        inventory.forEachIndexed { index, invItem ->
            if (invItem.category == item.category) {
                inventory[index] = invItem.copy(isEquipped = false)
            }
        }

        // 装备新物品
        val index = inventory.indexOf(item)
        if (index >= 0) {
            inventory[index] = item.copy(isEquipped = true)
            return true
        }

        return false
    }

    /**
     * 取消装备
     */
    fun unequipItem(userId: String, itemId: String): Boolean {
        val inventory = userInventory[userId] ?: return false
        val index = inventory.indexOfFirst { it.itemId == itemId }
        if (index >= 0) {
            inventory[index] = inventory[index].copy(isEquipped = false)
            return true
        }
        return false
    }

    /**
     * 获取已装备物品
     */
    fun getEquippedItems(userId: String): List<InventoryItem> {
        return getUserInventory(userId).filter { it.isEquipped }
    }

    /**
     * 获取购买历史
     */
    fun getPurchaseHistory(userId: String): List<PurchaseRecord> {
        return purchaseHistory.filter { it.userId == userId }
            .sortedByDescending { it.purchasedAt }
    }
}

/**
 * 购买结果
 */
sealed class PurchaseResult {
    data class Success(
        val remainingCrystals: Int,
        val item: InventoryItem,
        val totalPaid: Int
    ) : PurchaseResult()

    data class Failed(val message: String) : PurchaseResult()
    data class InsufficientFunds(val needed: Int) : PurchaseResult()
}

// ==================== 默认商品配置 ====================

val DEFAULT_SHOP_ITEMS = listOf(
    // 主题皮肤
    ShopItem(
        id = "theme_dark",
        name = "深空主题",
        description = "深邃的星空主题，护眼舒适",
        category = ShopCategory.THEME,
        rarity = ItemRarity.RARE,
        price = 200,
        effect = ItemEffect(EffectType.UNLOCK_THEME, 1f, null, "解锁深空主题"),
        isNew = true
    ),
    ShopItem(
        id = "theme_forest",
        name = "森林主题",
        description = "清新的森林绿色主题",
        category = ShopCategory.THEME,
        rarity = ItemRarity.RARE,
        price = 200,
        effect = ItemEffect(EffectType.UNLOCK_THEME, 1f, null, "解锁森林主题")
    ),
    ShopItem(
        id = "theme_ocean",
        name = "海洋主题",
        description = "宁静的海洋蓝色主题",
        category = ShopCategory.THEME,
        rarity = ItemRarity.EPIC,
        price = 350,
        effect = ItemEffect(EffectType.UNLOCK_THEME, 1f, null, "解锁海洋主题"),
        isHot = true
    ),
    ShopItem(
        id = "theme_sunset",
        name = "日落主题",
        description = "温暖的日落橙色主题",
        category = ShopCategory.THEME,
        rarity = ItemRarity.EPIC,
        price = 350,
        effect = ItemEffect(EffectType.UNLOCK_THEME, 1f, null, "解锁日落主题")
    ),

    // 道具
    ShopItem(
        id = "prop_streak_protect",
        name = "断签保护卡",
        description = "保护连续签到不被中断",
        category = ShopCategory.PROP,
        rarity = ItemRarity.RARE,
        price = 50,
        stock = null,
        effect = ItemEffect(EffectType.STREAK_PROTECT, 1f, null, "断签保护一次"),
        isHot = true
    ),
    ShopItem(
        id = "prop_energy_boost_small",
        name = "能量药剂(小)",
        description = "恢复30点能量",
        category = ShopCategory.PROP,
        rarity = ItemRarity.COMMON,
        price = 20,
        effect = ItemEffect(EffectType.ENERGY_BOOST, 30f, null, "恢复30能量")
    ),
    ShopItem(
        id = "prop_energy_boost_large",
        name = "能量药剂(大)",
        description = "恢复100点能量",
        category = ShopCategory.PROP,
        rarity = ItemRarity.RARE,
        price = 60,
        effect = ItemEffect(EffectType.ENERGY_BOOST, 100f, null, "恢复100能量")
    ),
    ShopItem(
        id = "prop_exp_boost",
        name = "经验加成卡",
        description = "学习经验+50%，持续24小时",
        category = ShopCategory.PROP,
        rarity = ItemRarity.EPIC,
        price = 100,
        effect = ItemEffect(EffectType.EXP_BOOST, 1.5f, 24, "经验+50%持续24小时"),
        isHot = true
    ),
    ShopItem(
        id = "prop_crystal_boost",
        name = "结晶加成卡",
        description = "结晶获取+30%，持续24小时",
        category = ShopCategory.PROP,
        rarity = ItemRarity.EPIC,
        price = 120,
        effect = ItemEffect(EffectType.CRYSTAL_BOOST, 1.3f, 24, "结晶+30%持续24小时")
    ),
    ShopItem(
        id = "prop_focus_boost",
        name = "专注药剂",
        description = "专注学习，减少干扰提示",
        category = ShopCategory.PROP,
        rarity = ItemRarity.RARE,
        price = 80,
        effect = ItemEffect(EffectType.FOCUS_BOOST, 1f, 2, "专注模式2小时")
    ),

    // VIP特权
    ShopItem(
        id = "vip_weekly",
        name = "周卡会员",
        description = "解锁全部主题，经验+20%，结晶+10%",
        category = ShopCategory.PRIVILEGE,
        rarity = ItemRarity.EPIC,
        price = 180,
        effect = ItemEffect(EffectType.VIP_STATUS, 1.2f, 168, "VIP特权7天")
    ),
    ShopItem(
        id = "vip_monthly",
        name = "月卡会员",
        description = "解锁全部主题，经验+30%，结晶+20%",
        category = ShopCategory.PRIVILEGE,
        rarity = ItemRarity.LEGENDARY,
        price = 500,
        originalPrice = 720,
        discount = 0.7f,
        effect = ItemEffect(EffectType.VIP_STATUS, 1.3f, 720, "VIP特权30天"),
        isHot = true
    ),

    // 头像框
    ShopItem(
        id = "avatar_gold",
        name = "金色光环",
        description = "闪耀的金色头像框",
        category = ShopCategory.AVATAR,
        rarity = ItemRarity.EPIC,
        price = 150,
        effect = ItemEffect(EffectType.UNLOCK_AVATAR, 1f, null, "解锁金色光环")
    ),
    ShopItem(
        id = "avatar_scholar",
        name = "学者光环",
        description = "优雅的学者风格头像框",
        category = ShopCategory.AVATAR,
        rarity = ItemRarity.RARE,
        price = 100,
        effect = ItemEffect(EffectType.UNLOCK_AVATAR, 1f, null, "解锁学者光环")
    ),

    // 限时特惠
    ShopItem(
        id = "special_starter_pack",
        name = "新手特惠包",
        description = "包含：能量药剂x3、断签保护x2、经验加成卡x1",
        category = ShopCategory.SPECIAL,
        rarity = ItemRarity.LEGENDARY,
        price = 99,
        originalPrice = 250,
        discount = 0.4f,
        limitedTime = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000,
        stock = 100,
        isHot = true
    )
)