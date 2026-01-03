package com.example.diaryapp.service

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 标题生成服务
 * 根据日记内容自动生成标题
 */
class TitleGeneratorService {

    companion object {
        private val TIME_PATTERNS = listOf(
            Regex("今[天日]"),
            Regex("昨[天日]"),
            Regex("前天"),
            Regex("明[天日]"),
            Regex("早上|上午|中午|下午|晚上|夜里|深夜"),
            Regex("周[一二三四五六七八日]|星期[一二三四五六七八日天]|周末")
        )

        private val MOOD_KEYWORDS = mapOf(
            "开心" to "开心的一天",
            "快乐" to "快乐时光",
            "难过" to "有些难过",
            "伤心" to "伤心的日子",
            "生气" to "有些生气",
            "疲惫" to "疲惫的一天",
            "累" to "累了",
            "兴奋" to "兴奋时刻",
            "期待" to "充满期待",
            "紧张" to "有些紧张",
            "平静" to "平静的一天",
            "满足" to "满足的一天"
        )
    }

    /**
     * 根据内容生成标题
     * @param content 日记内容
     * @param tags 自动标签
     * @return 生成的标题
     */
    fun generateTitle(content: String, tags: List<String>): String {
        // 1. 尝试从内容提取第一句话作为标题
        val firstSentence = extractFirstSentence(content)
        if (firstSentence.isNotBlank() && firstSentence.length <= 20) {
            return firstSentence
        }

        // 2. 检查是否有特定心情关键词
        MOOD_KEYWORDS.forEach { (keyword, title) ->
            if (content.contains(keyword)) {
                return title
            }
        }

        // 3. 基于标签生成标题
        if (tags.isNotEmpty()) {
            val mainTag = tags.first()
            return "关于${mainTag}的日记"
        }

        // 4. 检查是否有特定事件关键词
        val eventTitle = detectEvent(content)
        if (eventTitle != null) {
            return eventTitle
        }

        // 5. 使用日期作为后备
        return "日记 - ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("M月d日"))}"
    }

    /**
     * 提取第一句话
     */
    private fun extractFirstSentence(content: String): String {
        // 按句子分隔符分割
        val sentences = content.split(Regex("[。！？!?\\n]"))
        val firstSentence = sentences.firstOrNull { it.trim().isNotBlank() } ?: return ""

        // 清理并返回
        return firstSentence.trim()
            .replace(Regex("^[\"'「『（]|[\"'」』）]$"), "")
            .take(30)
    }

    /**
     * 检测特定事件
     */
    private fun detectEvent(content: String): String? {
        val events = mapOf(
            "旅行|出游|游玩|景点|景点" to "旅行日记",
            "工作|上班|会议|项目" to "工作相关",
            "学习|读书|上课|考试" to "学习笔记",
            "美食|吃饭|餐厅|烹饪" to "美食日记",
            "运动|跑步|健身|锻炼" to "运动记录",
            "电影|影片|观影" to "观影笔记",
            "购物|买|逛街" to "购物日记",
            "生日|聚会|派对" to "特别聚会",
            "家庭|父母|孩子|家人" to "家庭时光",
            "朋友|同事|聚会" to "朋友相聚"
        )

        events.forEach { (pattern, title) ->
            if (content.contains(Regex(pattern))) {
                return title
            }
        }

        return null
    }

    /**
     * 为现有内容生成建议标题列表
     */
    fun generateTitleSuggestions(content: String, tags: List<String>): List<String> {
        val suggestions = mutableListOf<String>()

        // 1. 第一句话
        val firstSentence = extractFirstSentence(content)
        if (firstSentence.isNotBlank()) {
            suggestions.add(firstSentence)
        }

        // 2. 基于标签
        tags.take(3).forEach { tag ->
            suggestions.add("关于${tag}的日记")
        }

        // 3. 基于心情
        MOOD_KEYWORDS.values.take(3).forEach { title ->
            if (!suggestions.contains(title)) {
                suggestions.add(title)
            }
        }

        // 4. 基于事件
        val eventTitle = detectEvent(content)
        if (eventTitle != null && !suggestions.contains(eventTitle)) {
            suggestions.add(eventTitle)
        }

        // 5. 日期标题
        suggestions.add("日记 - ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("M月d日 HH:mm"))}")

        return suggestions.take(6).distinct()
    }
}
