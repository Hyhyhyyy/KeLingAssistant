package com.keling.app.data

/**
 * =========================
 * 社交功能 - 学习小组
 * =========================
 *
 * 学习小组与社交功能
 * - 小组创建与管理
 * - 成员互动
 * - 小组目标与挑战
 * - 小组聊天
 * - 学习PK
 */

import kotlinx.serialization.Serializable

// ==================== 小组模型 ====================

/**
 * 学习小组
 */
@Serializable
data class StudyGroup(
    val id: String,
    val name: String,
    val description: String,
    val creatorId: String,
    val creatorName: String,
    val targetCourse: String? = null,       // 目标课程
    val groupGoal: String? = null,          // 小组目标
    val maxMembers: Int = 10,
    val members: List<GroupMember>,
    val weeklyChallenge: GroupChallenge? = null,
    val chatMessages: List<GroupMessage> = emptyList(),
    val status: GroupStatus = GroupStatus.ACTIVE,
    val inviteCode: String? = null,         // 邀请码
    val isPublic: Boolean = true,
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 小组成员
 */
@Serializable
data class GroupMember(
    val userId: String,
    val userName: String,
    val avatar: String? = null,
    val role: GroupRole = GroupRole.MEMBER,
    val joinedAt: Long,
    val contribution: Int = 0,              // 贡献值
    val weeklyStudyMinutes: Int = 0,
    val completedTasks: Int = 0,
    val streakDays: Int = 0,
    val title: String? = null               // 小组内称号
)

/**
 * 成员角色
 */
enum class GroupRole(val displayName: String) {
    CREATOR("创建者"),
    ADMIN("管理员"),
    MEMBER("成员")
}

/**
 * 小组状态
 */
enum class GroupStatus {
    ACTIVE,
    INACTIVE,
    DISBANDED
}

/**
 * 小组消息
 */
@Serializable
data class GroupMessage(
    val id: String,
    val groupId: String,
    val senderId: String,
    val senderName: String,
    val senderAvatar: String? = null,
    val content: String,
    val type: MessageType = MessageType.TEXT,
    val replyTo: String? = null,            // 回复的消息ID
    val reactions: List<MessageReaction> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 消息类型
 */
enum class MessageType {
    TEXT,
    IMAGE,
    SYSTEM,          // 系统消息
    TASK_SHARE,      // 任务分享
    ACHIEVEMENT_SHARE, // 成就分享
    CHALLENGE_START, // 挑战开始
    CHALLENGE_COMPLETE // 挑战完成
}

/**
 * 消息反应
 */
@Serializable
data class MessageReaction(
    val userId: String,
    val userName: String,
    val emoji: String
)

/**
 * 小组挑战
 */
@Serializable
data class GroupChallenge(
    val id: String,
    val groupId: String,
    val title: String,
    val description: String,
    val type: ChallengeType,
    val target: Int,                        // 目标值
    val unit: String,                       // "分钟"、"任务"、"知识点"
    val progress: Int = 0,                  // 当前进度
    val startDate: Long,
    val endDate: Long,
    val rewards: GroupRewards,
    val memberProgress: Map<String, Int> = emptyMap(), // 成员进度
    val isCompleted: Boolean = false,
    val completedAt: Long? = null
)

/**
 * 小组奖励
 */
@Serializable
data class GroupRewards(
    val energyPerMember: Int,
    val crystalsPerMember: Int,
    val expPerMember: Int,
    val bonusTitle: String? = null          // 奖励称号
)

// ==================== 学习PK ====================

/**
 * 学习PK
 */
@Serializable
data class StudyPK(
    val id: String,
    val challengerId: String,
    val challengerName: String,
    val challengerAvatar: String? = null,
    val opponentId: String,
    val opponentName: String,
    val opponentAvatar: String? = null,
    val pkType: PKType,
    val duration: Int,                      // PK持续时间(小时)
    val startTime: Long,
    val endTime: Long,
    val challengerScore: Int = 0,
    val opponentScore: Int = 0,
    val status: PKStatus = PKStatus.PENDING,
    val winnerId: String? = null,
    val rewards: PKRewards,
    val aiCommentary: String? = null,       // AI解说
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * PK类型
 */
enum class PKType(val displayName: String, val description: String) {
    STUDY_TIME("学习时长PK", "比拼学习时长（分钟）"),
    TASK_COUNT("任务完成PK", "比拼完成任务数量"),
    QUIZ_SCORE("答题PK", "比拼答题正确率"),
    FOCUS_TIME("专注时长PK", "比拼专注学习时长")
}

/**
 * PK状态
 */
enum class PKStatus {
    PENDING,      // 等待接受
    ONGOING,      // 进行中
    COMPLETED,    // 已完成
    CANCELLED,    // 已取消
    REJECTED      // 已拒绝
}

/**
 * PK奖励
 */
@Serializable
data class PKRewards(
    val winnerCrystals: Int,
    val winnerExp: Int,
    val loserCrystals: Int,
    val loserExp: Int
)

// ==================== 学习动态 ====================

/**
 * 学习动态
 */
@Serializable
data class StudyMoment(
    val id: String,
    val userId: String,
    val userName: String,
    val userAvatar: String? = null,
    val type: MomentType,
    val content: String,
    val imageUrl: String? = null,
    val relatedData: String? = null,        // JSON格式的关联数据
    val likes: Int = 0,
    val likedByUser: Boolean = false,
    val comments: List<MomentComment> = emptyList(),
    val visibility: MomentVisibility = MomentVisibility.PUBLIC,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 动态类型
 */
enum class MomentType(val displayName: String) {
    CHECK_IN("签到打卡"),
    ACHIEVEMENT("成就解锁"),
    TASK_DONE("任务完成"),
    LEVEL_UP("等级提升"),
    STUDY_SESSION("学习记录"),
    PK_WIN("PK胜利"),
    CHALLENGE_COMPLETE("挑战完成")
}

/**
 * 动态可见性
 */
enum class MomentVisibility {
    PUBLIC,       // 公开
    FRIENDS,      // 好友可见
    PRIVATE       // 私密
}

/**
 * 动态评论
 */
@Serializable
data class MomentComment(
    val id: String,
    val momentId: String,
    val userId: String,
    val userName: String,
    val userAvatar: String? = null,
    val content: String,
    val replyToUserId: String? = null,      // 回复的用户
    val replyToUserName: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

// ==================== 小组管理器 ====================

/**
 * 学习小组管理器
 */
class StudyGroupManager {
    private val groups = mutableListOf<StudyGroup>()
    private val userGroups = mutableMapOf<String, MutableList<String>>() // userId -> groupIds
    private val pendingPKs = mutableListOf<StudyPK>()
    private val moments = mutableListOf<StudyMoment>()

    /**
     * 创建小组
     */
    fun createGroup(
        name: String,
        description: String,
        creatorId: String,
        creatorName: String,
        targetCourse: String? = null,
        groupGoal: String? = null,
        maxMembers: Int = 10,
        isPublic: Boolean = true,
        tags: List<String> = emptyList()
    ): StudyGroup {
        val inviteCode = generateInviteCode()

        val group = StudyGroup(
            id = "group_${System.currentTimeMillis()}_${(0..9999).random()}",
            name = name,
            description = description,
            creatorId = creatorId,
            creatorName = creatorName,
            targetCourse = targetCourse,
            groupGoal = groupGoal,
            maxMembers = maxMembers,
            members = listOf(
                GroupMember(
                    userId = creatorId,
                    userName = creatorName,
                    role = GroupRole.CREATOR,
                    joinedAt = System.currentTimeMillis()
                )
            ),
            inviteCode = inviteCode,
            isPublic = isPublic,
            tags = tags
        )

        groups.add(group)
        userGroups.getOrPut(creatorId) { mutableListOf() }.add(group.id)

        return group
    }

    /**
     * 加入小组
     */
    fun joinGroup(
        groupId: String,
        userId: String,
        userName: String,
        inviteCode: String? = null
    ): JoinResult {
        val group = groups.find { it.id == groupId }
            ?: return JoinResult.Failed("小组不存在")

        // 检查是否已是成员
        if (group.members.any { it.userId == userId }) {
            return JoinResult.Failed("已是小组成员")
        }

        // 检查人数
        if (group.members.size >= group.maxMembers) {
            return JoinResult.Failed("小组人数已满")
        }

        // 检查邀请码（私有小组）
        if (!group.isPublic && group.inviteCode != inviteCode) {
            return JoinResult.Failed("邀请码无效")
        }

        // 添加成员
        val newMember = GroupMember(
            userId = userId,
            userName = userName,
            role = GroupRole.MEMBER,
            joinedAt = System.currentTimeMillis()
        )

        val updatedGroup = group.copy(
            members = group.members + newMember,
            updatedAt = System.currentTimeMillis()
        )

        val index = groups.indexOf(group)
        groups[index] = updatedGroup

        userGroups.getOrPut(userId) { mutableListOf() }.add(groupId)

        // 发送系统消息
        addSystemMessage(groupId, "${userName} 加入了小组")

        return JoinResult.Success(updatedGroup)
    }

    /**
     * 离开小组
     */
    fun leaveGroup(groupId: String, userId: String): Boolean {
        val group = groups.find { it.id == groupId } ?: return false
        val member = group.members.find { it.userId == userId } ?: return false

        // 创建者不能离开，只能解散
        if (member.role == GroupRole.CREATOR) {
            return false
        }

        val updatedGroup = group.copy(
            members = group.members.filter { it.userId != userId },
            updatedAt = System.currentTimeMillis()
        )

        val index = groups.indexOf(group)
        groups[index] = updatedGroup

        userGroups[userId]?.remove(groupId)

        // 发送系统消息
        addSystemMessage(groupId, "${member.userName} 离开了小组")

        return true
    }

    /**
     * 发送消息
     */
    fun sendMessage(
        groupId: String,
        senderId: String,
        senderName: String,
        content: String,
        type: MessageType = MessageType.TEXT
    ): GroupMessage? {
        val group = groups.find { it.id == groupId } ?: return null
        val member = group.members.find { it.userId == senderId } ?: return null

        val message = GroupMessage(
            id = "msg_${System.currentTimeMillis()}_${(0..9999).random()}",
            groupId = groupId,
            senderId = senderId,
            senderName = senderName,
            senderAvatar = member.avatar,
            content = content,
            type = type
        )

        val updatedGroup = group.copy(
            chatMessages = group.chatMessages + message,
            updatedAt = System.currentTimeMillis()
        )

        val index = groups.indexOf(group)
        groups[index] = updatedGroup

        return message
    }

    /**
     * 添加系统消息
     */
    private fun addSystemMessage(groupId: String, content: String) {
        val group = groups.find { it.id == groupId } ?: return

        val message = GroupMessage(
            id = "sys_${System.currentTimeMillis()}",
            groupId = groupId,
            senderId = "system",
            senderName = "系统",
            content = content,
            type = MessageType.SYSTEM
        )

        val index = groups.indexOf(group)
        groups[index] = group.copy(
            chatMessages = group.chatMessages + message
        )
    }

    /**
     * 创建小组挑战
     */
    fun createChallenge(
        groupId: String,
        title: String,
        description: String,
        type: ChallengeType,
        target: Int,
        unit: String,
        durationDays: Int,
        rewards: GroupRewards
    ): GroupChallenge? {
        val group = groups.find { it.id == groupId } ?: return null

        val now = System.currentTimeMillis()
        val challenge = GroupChallenge(
            id = "challenge_${System.currentTimeMillis()}",
            groupId = groupId,
            title = title,
            description = description,
            type = type,
            target = target,
            unit = unit,
            startDate = now,
            endDate = now + durationDays * 24 * 60 * 60 * 1000L,
            rewards = rewards,
            memberProgress = group.members.associate { it.userId to 0 }
        )

        val updatedGroup = group.copy(
            weeklyChallenge = challenge,
            updatedAt = System.currentTimeMillis()
        )

        val index = groups.indexOf(group)
        groups[index] = updatedGroup

        // 发送系统消息
        addSystemMessage(groupId, "新挑战「${title}」已开始！目标：$target$unit")

        return challenge
    }

    /**
     * 更新挑战进度
     */
    fun updateChallengeProgress(
        groupId: String,
        userId: String,
        progress: Int
    ): Boolean {
        val group = groups.find { it.id == groupId } ?: return false
        val challenge = group.weeklyChallenge ?: return false

        if (challenge.isCompleted) return false

        val updatedMemberProgress = challenge.memberProgress.toMutableMap()
        updatedMemberProgress[userId] = progress

        val totalProgress = updatedMemberProgress.values.sum()

        val updatedChallenge = challenge.copy(
            memberProgress = updatedMemberProgress,
            progress = totalProgress,
            isCompleted = totalProgress >= challenge.target
        )

        if (updatedChallenge.isCompleted && !challenge.isCompleted) {
            addSystemMessage(groupId, "恭喜！挑战「${challenge.title}」已完成！")
        }

        val updatedGroup = group.copy(
            weeklyChallenge = updatedChallenge,
            updatedAt = System.currentTimeMillis()
        )

        val index = groups.indexOf(group)
        groups[index] = updatedGroup

        return true
    }

    /**
     * 获取用户的小组
     */
    fun getUserGroups(userId: String): List<StudyGroup> {
        val groupIds = userGroups[userId] ?: return emptyList()
        return groups.filter { it.id in groupIds }
    }

    /**
     * 获取公开小组列表
     */
    fun getPublicGroups(tags: List<String> = emptyList()): List<StudyGroup> {
        return groups.filter { group ->
            group.isPublic &&
            group.status == GroupStatus.ACTIVE &&
            group.members.size < group.maxMembers &&
            (tags.isEmpty() || group.tags.any { it in tags })
        }.sortedByDescending { it.members.size }
    }

    /**
     * 获取小组详情
     */
    fun getGroup(groupId: String): StudyGroup? {
        return groups.find { it.id == groupId }
    }

    // ==================== PK功能 ====================

    /**
     * 发起PK
     */
    fun initiatePK(
        challengerId: String,
        challengerName: String,
        opponentId: String,
        opponentName: String,
        pkType: PKType,
        durationHours: Int
    ): StudyPK {
        val now = System.currentTimeMillis()
        val pk = StudyPK(
            id = "pk_${System.currentTimeMillis()}_${(0..9999).random()}",
            challengerId = challengerId,
            challengerName = challengerName,
            opponentId = opponentId,
            opponentName = opponentName,
            pkType = pkType,
            duration = durationHours,
            startTime = now,
            endTime = now + durationHours * 60 * 60 * 1000L,
            status = PKStatus.PENDING,
            rewards = PKRewards(
                winnerCrystals = 50,
                winnerExp = 100,
                loserCrystals = 20,
                loserExp = 40
            )
        )

        pendingPKs.add(pk)
        return pk
    }

    /**
     * 接受PK
     */
    fun acceptPK(pkId: String): StudyPK? {
        val pk = pendingPKs.find { it.id == pkId } ?: return null
        if (pk.status != PKStatus.PENDING) return null

        val index = pendingPKs.indexOf(pk)
        val updated = pk.copy(status = PKStatus.ONGOING)
        pendingPKs[index] = updated

        return updated
    }

    /**
     * 拒绝PK
     */
    fun rejectPK(pkId: String): StudyPK? {
        val pk = pendingPKs.find { it.id == pkId } ?: return null

        val index = pendingPKs.indexOf(pk)
        val updated = pk.copy(status = PKStatus.REJECTED)
        pendingPKs[index] = updated

        return updated
    }

    /**
     * 更新PK分数
     */
    fun updatePKScore(pkId: String, userId: String, score: Int): StudyPK? {
        val pk = pendingPKs.find { it.id == pkId } ?: return null
        if (pk.status != PKStatus.ONGOING) return null

        val updated = when (userId) {
            pk.challengerId -> pk.copy(challengerScore = score)
            pk.opponentId -> pk.copy(opponentScore = score)
            else -> return null
        }

        // 检查是否结束
        if (System.currentTimeMillis() >= pk.endTime) {
            val winner = when {
                pk.challengerScore > pk.opponentScore -> pk.challengerId
                pk.opponentScore > pk.challengerScore -> pk.opponentId
                else -> null // 平局
            }

            val completed = updated.copy(
                status = PKStatus.COMPLETED,
                winnerId = winner,
                aiCommentary = generatePKCommentary(pk, winner)
            )

            val index = pendingPKs.indexOf(pk)
            pendingPKs[index] = completed

            return completed
        }

        val index = pendingPKs.indexOf(pk)
        pendingPKs[index] = updated

        return updated
    }

    /**
     * 获取用户的PK列表
     */
    fun getUserPKs(userId: String): List<StudyPK> {
        return pendingPKs.filter {
            it.challengerId == userId || it.opponentId == userId
        }.sortedByDescending { it.createdAt }
    }

    /**
     * 生成PK解说
     */
    private fun generatePKCommentary(pk: StudyPK, winnerId: String?): String {
        val winnerName = if (winnerId == pk.challengerId) pk.challengerName else pk.opponentName
        val loserName = if (winnerId == pk.challengerId) pk.opponentName else pk.challengerName

        return when {
            winnerId == null -> {
                "势均力敌！${pk.challengerName} 和 ${pk.opponentName} 战成平局！"
            }
            pk.pkType == PKType.STUDY_TIME -> {
                "${winnerName} 以 ${pk.challengerScore} vs ${pk.opponentScore} 分钟的成绩赢得胜利！"
            }
            else -> {
                "${winnerName} 凭借出色的表现赢得了这场PK！"
            }
        }
    }

    // ==================== 学习动态 ====================

    /**
     * 发布动态
     */
    fun postMoment(
        userId: String,
        userName: String,
        type: MomentType,
        content: String,
        imageUrl: String? = null,
        relatedData: String? = null
    ): StudyMoment {
        val moment = StudyMoment(
            id = "moment_${System.currentTimeMillis()}_${(0..9999).random()}",
            userId = userId,
            userName = userName,
            type = type,
            content = content,
            imageUrl = imageUrl,
            relatedData = relatedData
        )

        moments.add(0, moment) // 新动态在前
        return moment
    }

    /**
     * 点赞动态
     */
    fun likeMoment(momentId: String, userId: String): Boolean {
        val moment = moments.find { it.id == momentId } ?: return false

        val index = moments.indexOf(moment)
        moments[index] = moment.copy(
            likes = moment.likes + 1,
            likedByUser = true // 简化处理
        )

        return true
    }

    /**
     * 评论动态
     */
    fun commentMoment(
        momentId: String,
        userId: String,
        userName: String,
        content: String,
        replyToUserId: String? = null,
        replyToUserName: String? = null
    ): MomentComment? {
        val moment = moments.find { it.id == momentId } ?: return null

        val comment = MomentComment(
            id = "comment_${System.currentTimeMillis()}",
            momentId = momentId,
            userId = userId,
            userName = userName,
            content = content,
            replyToUserId = replyToUserId,
            replyToUserName = replyToUserName
        )

        val index = moments.indexOf(moment)
        moments[index] = moment.copy(
            comments = moment.comments + comment
        )

        return comment
    }

    /**
     * 获取动态流
     */
    fun getMomentsFeed(limit: Int = 20): List<StudyMoment> {
        return moments.filter {
            it.visibility == MomentVisibility.PUBLIC
        }.take(limit)
    }

    /**
     * 获取用户动态
     */
    fun getUserMoments(userId: String, limit: Int = 10): List<StudyMoment> {
        return moments.filter { it.userId == userId }.take(limit)
    }

    // ==================== 工具方法 ====================

    /**
     * 生成邀请码
     */
    private fun generateInviteCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6).map { chars.random() }.joinToString("")
    }
}

/**
 * 加入结果
 */
sealed class JoinResult {
    data class Success(val group: StudyGroup) : JoinResult()
    data class Failed(val message: String) : JoinResult()
}