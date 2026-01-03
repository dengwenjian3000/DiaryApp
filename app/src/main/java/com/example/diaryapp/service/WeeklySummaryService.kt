package com.example.diaryapp.service

import com.example.diaryapp.data.database.entities.DiaryEntry
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * 每周总结生成服务
 * 智能分析本周日记，生成精选图文总结
 */
class WeeklySummaryService {

    /**
     * 每周总结数据类
     */
    data class WeeklySummary(
        val weekStart: LocalDateTime,
        val weekEnd: LocalDateTime,
        val year: Int,
        val weekNumber: Int,
        val totalEntries: Int,
        val featuredEntries: List<FeaturedEntry>,
        val topTags: List<TagFrequency>,
        val moodDistribution: MoodDistribution,
        val summary: String
    )

    /**
     * 精选日记条目
     */
    data class FeaturedEntry(
        val diary: DiaryEntry,
        val highlightReason: HighlightReason,
        val excerpt: String
    )

    /**
     * 精选原因
     */
    enum class HighlightReason {
        MOST_PHOTOS,      // 图片最多
        LONGEST_CONTENT,  // 内容最长
        HIGHLIGHTED,      // 手动标记为精选
        MOST_TAGS,        // 标签最多
        MOOD_EXTREME      // 情绪强烈
    }

    /**
     * 标签频率
     */
    data class TagFrequency(
        val tag: String,
        val count: Int
    )

    /**
     * 心情分布
     */
    data class MoodDistribution(
        val average: Double?,
        val mostCommon: Int?,
        val distribution: Map<Int, Int> // 1-5分各自的数量
    )

    /**
     * 生成本周总结
     */
    fun generateWeeklySummary(diaries: List<DiaryEntry>): WeeklySummary {
        if (diaries.isEmpty()) {
            return createEmptySummary()
        }

        val (weekStart, weekEnd) = getCurrentWeekRange()
        val year = weekStart.year
        val weekNumber = getWeekNumber(weekStart)

        // 统计标签频率
        val topTags = analyzeTopTags(diaries)

        // 分析心情分布
        val moodDistribution = analyzeMoodDistribution(diaries)

        // 生成精选内容
        val featuredEntries = selectFeaturedEntries(diaries)

        // 生成文字总结
        val summary = generateTextSummary(
            diaries.size,
            topTags,
            moodDistribution,
            featuredEntries
        )

        return WeeklySummary(
            weekStart = weekStart,
            weekEnd = weekEnd,
            year = year,
            weekNumber = weekNumber,
            totalEntries = diaries.size,
            featuredEntries = featuredEntries,
            topTags = topTags,
            moodDistribution = moodDistribution,
            summary = summary
        )
    }

    /**
     * 获取当前周的起止时间
     */
    fun getCurrentWeekRange(): Pair<LocalDateTime, LocalDateTime> {
        val now = LocalDateTime.now()
        val dayOfWeek = now.dayOfWeek.value
        val weekStart = now.minusDays((dayOfWeek - 1).toLong()).toLocalDate().atStartOfDay()
        val weekEnd = weekStart.plusDays(6).withHour(23).withMinute(59).withSecond(59)
        return Pair(weekStart, weekEnd)
    }

    /**
     * 获取周数
     */
    private fun getWeekNumber(date: LocalDateTime): Int {
        // 简化的周数计算
        val dayOfYear = date.dayOfYear
        return (dayOfYear / 7) + 1
    }

    /**
     * 分析高频标签
     */
    private fun analyzeTopTags(diaries: List<DiaryEntry>): List<TagFrequency> {
        val tagCount = mutableMapOf<String, Int>()

        diaries.forEach { diary ->
            diary.autoTags.forEach { tag ->
                tagCount[tag] = tagCount.getOrDefault(tag, 0) + 1
            }
            diary.manualTags.forEach { tag ->
                tagCount[tag] = tagCount.getOrDefault(tag, 0) + 1
            }
        }

        return tagCount.entries
            .sortedByDescending { it.value }
            .take(10)
            .map { TagFrequency(it.key, it.value) }
    }

    /**
     * 分析心情分布
     */
    private fun analyzeMoodDistribution(diaries: List<DiaryEntry>): MoodDistribution {
        val moodsWithValue = diaries.mapNotNull { it.mood }
        val distribution = (1..5).associateWith { mood ->
            moodsWithValue.count { it == mood }
        }

        val average = if (moodsWithValue.isNotEmpty()) {
            moodsWithValue.average()
        } else null

        val mostCommon = if (moodsWithValue.isNotEmpty()) {
            distribution.maxByOrNull { it.value }?.key
        } else null

        return MoodDistribution(
            average = average,
            mostCommon = mostCommon,
            distribution = distribution
        )
    }

    /**
     * 选择精选日记
     */
    private fun selectFeaturedEntries(diaries: List<DiaryEntry>): List<FeaturedEntry> {
        if (diaries.isEmpty()) return emptyList()

        val featured = mutableListOf<FeaturedEntry>()

        // 1. 优先选择手动标记为精选的
        val highlighted = diaries.filter { it.isWeeklyHighlight }
        highlighted.forEach { diary ->
            featured.add(
                FeaturedEntry(
                    diary = diary,
                    highlightReason = HighlightReason.HIGHLIGHTED,
                    excerpt = extractExcerpt(diary.plainContent, 100)
                )
            )
        }

        // 2. 图片最多的
        val mostPhotos = diaries
            .filterNot { it.isWeeklyHighlight }
            .maxByOrNull { it.imagePaths.size + it.videoPaths.size }
        mostPhotos?.let {
            if (it.imagePaths.isNotEmpty() || it.videoPaths.isNotEmpty()) {
                featured.add(
                    FeaturedEntry(
                        diary = it,
                        highlightReason = HighlightReason.MOST_PHOTOS,
                        excerpt = extractExcerpt(it.plainContent, 100)
                    )
                )
            }
        }

        // 3. 内容最长的
        val longestContent = diaries
            .filterNot { d -> d.isWeeklyHighlight || d == mostPhotos }
            .maxByOrNull { it.plainContent.length }
        longestContent?.let {
            featured.add(
                FeaturedEntry(
                    diary = it,
                    highlightReason = HighlightReason.LONGEST_CONTENT,
                    excerpt = extractExcerpt(it.plainContent, 100)
                )
            )
        }

        // 4. 标签最多的
        val mostTags = diaries
            .filterNot { d -> d.isWeeklyHighlight || d == mostPhotos || d == longestContent }
            .maxByOrNull { d -> d.autoTags.size + d.manualTags.size }
        mostTags?.let {
            if (it.autoTags.isNotEmpty() || it.manualTags.isNotEmpty()) {
                featured.add(
                    FeaturedEntry(
                        diary = it,
                        highlightReason = HighlightReason.MOST_TAGS,
                        excerpt = extractExcerpt(it.plainContent, 100)
                    )
                )
            }
        }

        // 5. 极端心情的（1分或5分）
        val extremeMood = diaries
            .filterNot { d ->
                d.isWeeklyHighlight ||
                d == mostPhotos ||
                d == longestContent ||
                d == mostTags
            }
            .find { it.mood == 1 || it.mood == 5 }
        extremeMood?.let {
            featured.add(
                FeaturedEntry(
                    diary = it,
                    highlightReason = HighlightReason.MOOD_EXTREME,
                    excerpt = extractExcerpt(it.plainContent, 100)
                )
            )
        }

        return featured.take(5) // 最多5篇精选
    }

    /**
     * 提取摘要
     */
    private fun extractExcerpt(content: String, maxLength: Int): String {
        val cleanContent = content.replace(Regex("<[^>]+>"), "")
        return if (cleanContent.length <= maxLength) {
            cleanContent
        } else {
            cleanContent.substring(0, maxLength) + "…"
        }
    }

    /**
     * 生成文字总结
     */
    private fun generateTextSummary(
        entryCount: Int,
        topTags: List<TagFrequency>,
        moodDistribution: MoodDistribution,
        featuredEntries: List<FeaturedEntry>
    ): String {
        val summaryParts = mutableListOf<String>()

        // 开头
        when {
            entryCount == 0 -> summaryParts.add("本周还没有记录任何日记哦。")
            entryCount <= 3 -> summaryParts.add("本周记录了 $entryCount 篇日记，继续保持！")
            entryCount <= 7 -> summaryParts.add("本周记录了 $entryCount 篇日记，记录生活的美好瞬间。")
            else -> summaryParts.add("本周很充实哦，记录了 $entryCount 篇日记！")
        }

        // 标签总结
        if (topTags.isNotEmpty()) {
            val topTagNames = topTags.take(3).map { it.tag }.joinToString("、")
            summaryParts.add("本周主要围绕「$topTagNames」展开。")
        }

        // 心情总结
        if (moodDistribution.average != null) {
            val avg = moodDistribution.average
            val moodDesc = when {
                avg >= 4.5 -> "心情很不错"
                avg >= 3.5 -> "心情还不错"
                avg >= 2.5 -> "心情平稳"
                avg >= 1.5 -> "有些低落"
                else -> "心情比较低落"
            }
            summaryParts.add("整体来看，本周$moodDesc。")
        }

        // 精选内容提示
        if (featuredEntries.isNotEmpty()) {
            summaryParts.add("精选了几篇特别的日记，快去看看吧！")
        }

        return summaryParts.joinToString("\n\n")
    }

    /**
     * 创建空总结
     */
    private fun createEmptySummary(): WeeklySummary {
        val (weekStart, weekEnd) = getCurrentWeekRange()
        return WeeklySummary(
            weekStart = weekStart,
            weekEnd = weekEnd,
            year = weekStart.year,
            weekNumber = getWeekNumber(weekStart),
            totalEntries = 0,
            featuredEntries = emptyList(),
            topTags = emptyList(),
            moodDistribution = MoodDistribution(null, null, emptyMap()),
            summary = "本周还没有记录任何日记哦。\n\n开始写下第一篇日记吧，记录生活的点滴！"
        )
    }

    /**
     * 检查是否应该重新生成总结
     */
    fun shouldRegenerate(lastGeneratedTime: LocalDateTime): Boolean {
        val now = LocalDateTime.now()
        val hoursSinceLastUpdate = ChronoUnit.HOURS.between(lastGeneratedTime, now)

        // 如果超过24小时，检查是否跨周
        if (hoursSinceLastUpdate >= 24) {
            val lastWeek = getWeekNumber(lastGeneratedTime)
            val currentWeek = getWeekNumber(now)
            return lastWeek != currentWeek
        }

        return false
    }
}

/**
 * 获取本周的显示文本
 */
fun WeeklySummaryService.WeeklySummary.getWeekDisplayText(): String {
    return "${year}年第${weekNumber}周"
}
