package com.keling.app.notification

/**
 * =========================
 * 任务提醒通知管理器
 * =========================
 *
 * 使用WorkManager实现定时通知
 * - 任务开始前提醒
 * - 每日学习提醒
 * - 课程开始前提醒
 */

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.keling.app.R
import java.util.concurrent.TimeUnit

/**
 * 通知渠道ID
 */
object NotificationChannels {
    const val TASK_REMINDER = "task_reminder"
    const val COURSE_REMINDER = "course_reminder"
    const val DAILY_REMINDER = "daily_reminder"
    const val ACHIEVEMENT = "achievement"
}

/**
 * 通知ID
 */
object NotificationIds {
    const val TASK_BASE = 1000
    const val COURSE_BASE = 2000
    const val DAILY = 3000
    const val ACHIEVEMENT_BASE = 4000
}

/**
 * 任务提醒Worker
 */
class TaskReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getString("taskId") ?: return Result.failure()
        val taskTitle = inputData.getString("taskTitle") ?: "学习任务"
        val reminderType = inputData.getString("reminderType") ?: "before"

        // 发送通知
        showTaskNotification(
            context = applicationContext,
            taskId = taskId,
            title = when (reminderType) {
                "before" -> "任务即将开始"
                "start" -> "任务开始"
                "overdue" -> "任务已超时"
                else -> "任务提醒"
            },
            message = taskTitle
        )

        return Result.success()
    }

    private fun showTaskNotification(
        context: Context,
        taskId: String,
        title: String,
        message: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 创建通知渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NotificationChannels.TASK_REMINDER,
                "任务提醒",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "任务开始前的提醒通知"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 构建通知
        val notification = NotificationCompat.Builder(context, NotificationChannels.TASK_REMINDER)
            .setSmallIcon(R.drawable.ic_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        // 显示通知
        notificationManager.notify(
            NotificationIds.TASK_BASE + taskId.hashCode(),
            notification
        )
    }
}

/**
 * 课程提醒Worker
 */
class CourseReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val courseId = inputData.getString("courseId") ?: return Result.failure()
        val courseName = inputData.getString("courseName") ?: "课程"
        val location = inputData.getString("location") ?: ""

        showCourseNotification(
            context = applicationContext,
            courseId = courseId,
            title = "即将上课",
            message = "$courseName${if (location.isNotBlank()) " @ $location" else ""}"
        )

        return Result.success()
    }

    private fun showCourseNotification(
        context: Context,
        courseId: String,
        title: String,
        message: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NotificationChannels.COURSE_REMINDER,
                "课程提醒",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "课程开始前的提醒通知"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, NotificationChannels.COURSE_REMINDER)
            .setSmallIcon(R.drawable.ic_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(
            NotificationIds.COURSE_BASE + courseId.hashCode(),
            notification
        )
    }
}

/**
 * 每日学习提醒Worker
 */
class DailyReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        showDailyNotification(
            context = applicationContext,
            title = "学习时间到啦",
            message = "今天还没有完成学习任务，快来打卡吧！"
        )

        return Result.success()
    }

    private fun showDailyNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NotificationChannels.DAILY_REMINDER,
                "每日提醒",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "每日学习提醒"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, NotificationChannels.DAILY_REMINDER)
            .setSmallIcon(R.drawable.ic_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NotificationIds.DAILY, notification)
    }
}

/**
 * 通知管理器
 */
class TaskNotificationManager(private val context: Context) {

    private val workManager = WorkManager.getInstance(context)

    /**
     * 安排任务提醒
     * @param taskId 任务ID
     * @param taskTitle 任务标题
     * @param scheduledTime 计划时间（时间戳毫秒）
     * @param minutesBefore 提前多少分钟提醒
     */
    fun scheduleTaskReminder(
        taskId: String,
        taskTitle: String,
        scheduledTime: Long,
        minutesBefore: Int = 15
    ) {
        val reminderTime = scheduledTime - (minutesBefore * 60 * 1000L)
        val delay = reminderTime - System.currentTimeMillis()

        if (delay <= 0) return // 已经过期，不安排

        val inputData = workDataOf(
            "taskId" to taskId,
            "taskTitle" to taskTitle,
            "reminderType" to "before"
        )

        val workRequest = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInputData(inputData)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag("task_$taskId")
            .build()

        workManager.enqueue(workRequest)
    }

    /**
     * 安排课程提醒
     * @param courseId 课程ID
     * @param courseName 课程名称
     * @param location 上课地点
     * @param classTime 上课时间（时间戳毫秒）
     * @param minutesBefore 提前多少分钟提醒
     */
    fun scheduleCourseReminder(
        courseId: String,
        courseName: String,
        location: String,
        classTime: Long,
        minutesBefore: Int = 15
    ) {
        val reminderTime = classTime - (minutesBefore * 60 * 1000L)
        val delay = reminderTime - System.currentTimeMillis()

        if (delay <= 0) return

        val inputData = workDataOf(
            "courseId" to courseId,
            "courseName" to courseName,
            "location" to location
        )

        val workRequest = OneTimeWorkRequestBuilder<CourseReminderWorker>()
            .setInputData(inputData)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag("course_$courseId")
            .build()

        workManager.enqueue(workRequest)
    }

    /**
     * 安排每日学习提醒
     * @param hour 小时（24小时制）
     * @param minute 分钟
     */
    fun scheduleDailyReminder(hour: Int, minute: Int) {
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)

            // 如果时间已过，安排到明天
            if (timeInMillis <= System.currentTimeMillis()) {
                add(java.util.Calendar.DAY_OF_MONTH, 1)
            }
        }

        val delay = calendar.timeInMillis - System.currentTimeMillis()

        // 使用PeriodicWorkRequest实现每日重复提醒
        val dailyRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag("daily_reminder")
            .build()

        workManager.enqueueUniquePeriodicWork(
            "daily_study_reminder",
            ExistingPeriodicWorkPolicy.REPLACE,
            dailyRequest
        )
    }

    /**
     * 取消任务提醒
     */
    fun cancelTaskReminder(taskId: String) {
        workManager.cancelAllWorkByTag("task_$taskId")
    }

    /**
     * 取消课程提醒
     */
    fun cancelCourseReminder(courseId: String) {
        workManager.cancelAllWorkByTag("course_$courseId")
    }

    /**
     * 取消每日提醒
     */
    fun cancelDailyReminder() {
        workManager.cancelUniqueWork("daily_study_reminder")
    }

    /**
     * 取消所有提醒
     */
    fun cancelAllReminders() {
        workManager.cancelAllWork()
    }

    /**
     * 显示成就通知
     */
    fun showAchievementNotification(
        achievementName: String,
        achievementDescription: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NotificationChannels.ACHIEVEMENT,
                "成就通知",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "成就解锁通知"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, NotificationChannels.ACHIEVEMENT)
            .setSmallIcon(R.drawable.ic_logo)
            .setContentTitle("🎉 成就解锁：$achievementName")
            .setContentText(achievementDescription)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(
            NotificationIds.ACHIEVEMENT_BASE + achievementName.hashCode(),
            notification
        )
    }
}